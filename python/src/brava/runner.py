"""The test engine: runs a test collection against a BrAPI server.

The collection's first folder reads the server's call list (``/calls`` or ``/serverinfo``).
Items in later folders run only if the server advertises their endpoint and all variables
they require were stored by earlier items. Each item sends one request and then interprets
its test commands (``StatusCode``, ``ContentType``, ``Schema``, ``GetValue``, ``IsEqual``,
``SaveCalls``, ``SearchSchema``; optional ``:breakiffalse`` suffix).
"""

from __future__ import annotations

import json
import logging
import re
from typing import Any
from urllib.parse import urlsplit

import httpx

from brava import validation
from brava.collection import Item, load_collection
from brava.config import get_settings
from brava.reports import (
    MISSING_REQS,
    SKIPPED,
    CollectionResult,
    ExecResult,
    FolderResult,
    ItemResult,
    SuiteReport,
    Variables,
    add_stats,
)

log = logging.getLogger(__name__)

CANT_CONNECT = "can't connect"
CANT_CONNECT_MSG = "Can't connect to tested server or missing parameters. Test cancelled."
BREAK_MSG = "Test failed. Won't continue testing this resource."
_MISSING = object()
_URL_VAR = re.compile(r"(?<!\$)\{([^{}]+)\}")
_JSON_VAR = re.compile(r"\$\{([^{}]+)\}")


# --- variables -----------------------------------------------------------------------------


def _json(value: Any) -> str:
    return json.dumps(value, separators=(",", ":"), ensure_ascii=False)


def _as_string(value: Any) -> str:
    # Same as the Java VariableStorage: JSON text with every double quote removed.
    return _json(value).replace('"', "")


def substitute_url(text: str, variables: dict[str, Any]) -> str:
    """Replace ``{name}`` with the variable's string form; unknown names stay as they are."""
    for _ in range(10):  # values may contain placeholders themselves
        new = _URL_VAR.sub(lambda m: _as_string(variables[m[1]]) if m[1] in variables else m[0], text)
        if new == text:
            break
        text = new
    return text


def substitute_json(text: str, variables: dict[str, Any]) -> str:
    """Replace ``${name}`` with the variable's JSON form; unknown names stay as they are."""
    return _JSON_VAR.sub(lambda m: _json(variables[m[1]]) if m[1] in variables else m[0], text)


def resolve_pointer(doc: Any, pointer: str) -> Any:
    """Resolve a JSON pointer; returns ``_MISSING`` when there is nothing at that path."""
    if pointer == "":
        return doc
    if not pointer.startswith("/"):
        return _MISSING
    node = doc
    for raw in pointer[1:].split("/"):
        key = raw.replace("~1", "/").replace("~0", "~")
        if isinstance(node, dict) and key in node:
            node = node[key]
        elif isinstance(node, list) and key.isdigit() and int(key) < len(node):
            node = node[int(key)]
        else:
            return _MISSING
    return node


def _show(value: Any) -> str:
    if value is _MISSING:
        return ""
    return _json(value)


# --- one item ------------------------------------------------------------------------------


def _invalid(message: str | None = None) -> ExecResult:
    return ExecResult("Invalid exec command", CANT_CONNECT, message=[message] if message else [])


class _Checks:
    """The test commands of one item, evaluated against its response."""

    def __init__(self, response: httpx.Response, variables: dict[str, Any], strict: bool) -> None:
        self.response = response
        self.variables = variables
        self.strict = strict

    def _body(self) -> Any:
        return validation.parse_json(self.response.content)

    def status_code(self, codes: str) -> ExecResult:
        r = ExecResult(f"Status code should be {codes}", "wrong status code")
        try:
            expected = {int(c) for c in codes.split(",")}
        except ValueError:
            return _invalid(f"Invalid status code list: {codes}")
        r.passed = self.response.status_code in expected
        r.message.append(f"Response Status code: {self.response.status_code}")
        return r

    def content_type(self, ct: str) -> ExecResult:
        r = ExecResult(f"ContentType is {ct}", "wrong ContentType")
        header = self.response.headers.get("content-type")
        r.passed = header is not None and ct in header
        r.message.append(f"Response Content Type: {header}")
        return r

    def schema(self, path: str) -> ExecResult:
        schema_path = f"/schemas{path}.json"
        r = ExecResult(f"Json matches schema: {schema_path}", "schema mismatch", schema=schema_path)
        try:
            errors = validation.validate(schema_path, self.response.content)
        except validation.InvalidJson:
            r.message.append("Server response is not valid JSON.")
            return r
        except validation.SchemaNotFound as e:
            r.message.append(str(e))
            return r
        if not self.strict:
            errors = [e for e in errors if e.get("keyword") != "additionalProperties"]
        r.error = errors[:10]
        r.passed = not errors
        r.message.append(
            "Response structure matches schema." if r.passed else "Response structure doesn't match schema."
        )
        return r

    def get_value(self, pointer: str, name: str) -> ExecResult:
        r = ExecResult(f"Save variable at: {pointer} | Key: {name}", "error storing variable")
        try:
            value = resolve_pointer(self._body(), pointer)
        except validation.InvalidJson as e:
            r.message.append(str(e))
            return r
        if value is _MISSING or value is None or value == "" or isinstance(value, (dict, list)):
            r.message.append("Value is empty string, null, or was not found.")
            return r
        self.variables[name] = value
        r.passed = True
        r.message.append(f"Stored value: {_json(value)}")
        return r

    def is_equal(self, pointer: str, name: str) -> ExecResult:
        r = ExecResult(f'Value in path: "{pointer}" equals variable: {name}', "data inconsistency")
        try:
            value = resolve_pointer(self._body(), pointer)
        except validation.InvalidJson as e:
            r.message.append(str(e))
            return r
        stored = self.variables.get(name, _MISSING)
        r.passed = (
            value is not _MISSING
            and stored is not _MISSING
            and json.dumps(value, sort_keys=True) == json.dumps(stored, sort_keys=True)  # type-strict
        )
        r.message.append(
            f"Found: {_show(value)}. Expected: {_show(stored) if stored is not _MISSING else 'null'}"
        )
        return r

    def save_calls(self, version: str) -> ExecResult:
        key = {"v1": ("/result/data", "callResult"), "v2": ("/result/calls", "serviceResult")}.get(
            version.lower()
        )
        if key is None:
            return _invalid("Bad Version Number")
        r = ExecResult("Saving Server Call Info", "error parsing call info")
        try:
            data = resolve_pointer(self._body(), key[0])
        except validation.InvalidJson:
            r.message.append("Error parsing call info")
            return r
        if isinstance(data, list):
            self.variables[key[1]] = data
        r.passed = True
        r.message.append("Saved call info")
        return r

    def search_schema(self, path: str, name: str) -> list[ExecResult]:
        status = self.response.status_code
        results: list[ExecResult] = []
        if status == 200:
            results.append(self.schema(path))
        elif status == 202:
            results.append(self.get_value("/result/searchResultsDbId", name))
            results.append(self.schema(path + "202"))
        results.append(
            ExecResult("Search matches schema", "search schema mismatch", passed=status in (200, 202))
        )
        return results

    def run(self, command: str, args: list[str]) -> ExecResult | list[ExecResult]:
        handlers = {
            "StatusCode": (self.status_code, 1),
            "ContentType": (self.content_type, 1),
            "Schema": (self.schema, 1),
            "GetValue": (self.get_value, 2),
            "IsEqual": (self.is_equal, 2),
            "SaveCalls": (self.save_calls, 1),
            "SearchSchema": (self.search_schema, 2),
        }
        if command not in handlers:
            return _invalid()
        handler, nargs = handlers[command]
        if len(args) < nargs:
            return _invalid("Missing parameters")
        return handler(*args[:nargs])


def _split_command(line: str) -> tuple[str, list[str], bool]:
    parts = line.split(":")
    while len(parts) > 1 and parts[-1] == "":  # like Java's String.split
        parts.pop()
    break_if_false = len(parts) > 2 and parts[-1] == "breakiffalse"
    if break_if_false:
        parts.pop()
    return parts[0], parts[1:], break_if_false


async def _send(
    client: httpx.AsyncClient,
    item: Item,
    url: str,
    variables: dict[str, Any],
    token: str | None,
    scheduled: bool,
) -> httpx.Response | None:
    if _URL_VAR.search(url):
        log.info("Unresolved variable in %s", url)
        return None
    try:
        parts = urlsplit(url)
        if parts.scheme not in ("http", "https") or not parts.hostname:
            return None
        # Scheduled (unattended) runs only talk to the standard web ports.
        if scheduled and parts.port not in (None, 80, 443):
            return None
        method = item.request.method.upper()
        headers = {"Content-Type": "application/json", "Accept": "application/json"}
        if token and token.strip():
            headers["Authorization"] = f"Bearer {token}"
        params: list[tuple[str, str]] | None = None
        content: str | None = None
        if method == "GET":
            params = [(p.param, substitute_url(p.value, variables)) for p in item.parameters]
        elif method in ("POST", "PUT"):
            default = "[{}]" if method == "POST" else "{}"
            content = substitute_json(item.parameters[0].value, variables) if item.parameters else default
        return await client.request(method, url, params=params, content=content, headers=headers)
    except Exception as e:  # any transport problem (DNS, TLS, timeout, protocol) means "can't connect"
        log.info("Connection error for %s: %s: %s", url, type(e).__name__, e)
        return None


async def run_item(
    client: httpx.AsyncClient,
    item: Item,
    variables: dict[str, Any],
    *,
    token: str | None = None,
    strict: bool = False,
    scheduled: bool = False,
) -> ItemResult:
    url = substitute_url(item.request.url, variables)
    result = ItemResult(item.name, endpoint=url, method=item.request.method)
    response = await _send(client, item, url, variables, token, scheduled)
    if response is None:
        result.test.append(ExecResult(CANT_CONNECT_MSG, CANT_CONNECT, message=[CANT_CONNECT_MSG]))
        result.testStatus.append(CANT_CONNECT)
        return result

    checks = _Checks(response, variables, strict)
    for line in item.commands:
        command, args, break_if_false = _split_command(line)
        outcome = checks.run(command, args)
        if isinstance(outcome, list):  # SearchSchema: never breaks
            for r in outcome:
                result.test.append(r)
                if not r.passed:
                    result.testStatus.append(r.type)
            continue
        result.test.append(outcome)
        if not outcome.passed:
            result.testStatus.append(outcome.type)
            if break_if_false:
                outcome.message.append(BREAK_MSG)
                break
    result.responseTime = int(response.elapsed.total_seconds() * 1000)
    return result


# --- the whole collection ------------------------------------------------------------------


def _advertised_calls(variables: dict[str, Any]) -> set[str]:
    calls: set[str] = set()
    for var, field in (("callResult", "call"), ("serviceResult", "service")):
        for entry in variables.get(var) or []:
            value = entry.get(field) if isinstance(entry, dict) else None
            calls.add("/" + (str(value) if value is not None else "null"))
    return calls


def make_client() -> httpx.AsyncClient:
    s = get_settings()
    return httpx.AsyncClient(
        timeout=s.request_timeout, verify=s.verify_tls, proxy=s.http_proxy or None, follow_redirects=True
    )


async def run_suite(
    base_url: str,
    version: str,
    *,
    token: str | None = None,
    strict: bool = False,
    scheduled: bool = False,
    resource_id: str = "",
    client: httpx.AsyncClient | None = None,
) -> SuiteReport:
    """Run the test collection of ``version`` against ``base_url`` (e.g. ``https://host/brapi/v2``)."""
    collection = load_collection(version)
    baseurl = base_url.removesuffix("/")
    variables: dict[str, Any] = {"baseurl": baseurl}
    folders: list[FolderResult] = []
    own_client = client is None
    client = client or make_client()
    try:
        for index, folder in enumerate(collection.item):
            fr = FolderResult(folder.name, url=baseurl, description=folder.description)
            calls = _advertised_calls(variables) if index > 0 else set()
            for item in folder.item:
                if index > 0 and item.endpoint not in calls:
                    fr.testsShort[item.name] = SKIPPED
                elif index > 0 and not all(r in variables for r in item.requires):
                    fr.testsShort[item.name] = MISSING_REQS
                else:
                    ir = await run_item(
                        client, item, variables, token=token, strict=strict, scheduled=scheduled
                    )
                    fr.tests.append(ir)
                    fr.testsShort[item.name] = ir
            folders.append(fr)
    finally:
        if own_client:
            await client.aclose()

    report = SuiteReport(
        [
            CollectionResult(
                name=collection.info.name,
                url=base_url,
                folders=folders,
                variables=Variables(
                    variables=dict(variables),
                    variablesAsString={k: _as_string(v) for k, v in variables.items()},
                    keys=list(variables),
                ),
            )
        ],
        id=resource_id,
        baseUrl=base_url,
    )
    add_stats(report)
    return report
