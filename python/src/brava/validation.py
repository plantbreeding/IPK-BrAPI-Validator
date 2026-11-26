"""JSON Schema (draft-04) validation of server responses.

Schemas are loaded from ``<resources>/schemas`` and identified by the same path the Java
version used (``/schemas/v2.1/metadata.json``). Validation errors are converted to dicts
shaped like java-json-tools ``ProcessingMessage`` objects, which the frontend renders.
"""

from __future__ import annotations

import json
import re
from functools import cache
from typing import Any

import rfc3986_validator
from jsonschema import Draft4Validator, FormatChecker, ValidationError
from referencing import Registry, Resource
from referencing.exceptions import NoSuchResource, Unresolvable
from referencing.jsonschema import DRAFT4

from brava.config import get_settings

# Schemas get http URIs so relative ``$ref``s ("../definitions.json#/...") resolve with urljoin.
_BASE_URI = "http://brava.local"


class SchemaNotFound(Exception):
    pass


class InvalidJson(Exception):
    pass


def _format_checker() -> FormatChecker:
    checker = FormatChecker()
    for name, func in Draft4Validator.FORMAT_CHECKER.checkers.items():
        checker.checkers[name] = func

    # java-json-tools accepts anything java.net.URI can parse, i.e. URI references too.
    @checker.checks("uri")
    def _uri(value: object) -> bool:
        return not isinstance(value, str) or rfc3986_validator.validate_rfc3986(value, rule="URI_reference")

    return checker


_FORMAT_CHECKER = _format_checker()


def _retrieve(uri: str) -> Resource:
    if not uri.startswith(_BASE_URI + "/schemas/"):
        raise NoSuchResource(ref=uri)
    rel = uri.removeprefix(_BASE_URI + "/schemas/")
    path = (get_settings().schemas_dir / rel).resolve()
    if not path.is_relative_to(get_settings().schemas_dir.resolve()) or not path.is_file():
        raise NoSuchResource(ref=uri)
    return Resource.from_contents(json.loads(path.read_bytes()), default_specification=DRAFT4)


_REGISTRY: Registry = Registry(retrieve=_retrieve)  # type: ignore[call-arg]


@cache
def _validator(schema_path: str) -> Draft4Validator:
    uri = _BASE_URI + schema_path
    try:
        resource = _REGISTRY.get_or_retrieve(uri).value
    except (Unresolvable, NoSuchResource) as e:
        raise SchemaNotFound(f"Schema not found: {schema_path}") from e
    schema = dict(resource.contents)
    schema.setdefault("id", uri)  # draft-04 base URI for resolving relative refs
    return Draft4Validator(schema, registry=_REGISTRY, format_checker=_FORMAT_CHECKER)


def parse_json(body: bytes | str) -> Any:
    if isinstance(body, bytes):
        body = body.decode("utf-8", "replace")
    if not body.strip():
        raise InvalidJson("No content to map due to end-of-input")
    try:
        return json.loads(body)
    except ValueError as e:
        raise InvalidJson(str(e)) from e


def _pointer(parts: Any) -> str:
    return "".join("/" + str(p).replace("~", "~0").replace("/", "~1") for p in parts)


def _json_type(value: Any) -> str:
    match value:
        case None:
            return "null"
        case bool():
            return "boolean"
        case int():
            return "integer"
        case float():
            return "number"
        case str():
            return "string"
        case list():
            return "array"
        case _:
            return "object"


def _to_message(err: ValidationError, schema_path: str) -> dict[str, Any]:
    keyword = str(err.validator)
    msg: dict[str, Any] = {
        "level": "error",
        "schema": {
            "loadingURI": f"resource:{schema_path}#",
            "pointer": _pointer(list(err.absolute_schema_path)[:-1]),
        },
        "instance": {"pointer": _pointer(err.absolute_path)},
        "domain": "validation",
        "keyword": keyword,
        "message": err.message,
    }
    value, instance = err.validator_value, err.instance
    match keyword:
        case "type":
            msg["found"] = _json_type(instance)
            msg["expected"] = sorted(value if isinstance(value, list) else [value])
        case "required":
            msg["required"] = list(value)
            msg["missing"] = [k for k in value if isinstance(instance, dict) and k not in instance]
        case "additionalProperties":
            props = err.schema.get("properties", {}) if isinstance(err.schema, dict) else {}
            patterns = err.schema.get("patternProperties", {}) if isinstance(err.schema, dict) else {}
            msg["unwanted"] = sorted(
                k for k in instance if k not in props and not any(re.search(p, k) for p in patterns)
            )
        case "minItems" | "maxItems" | "minLength" | "maxLength" | "minProperties" | "maxProperties":
            msg[keyword] = value
            msg["found"] = len(instance)
        case "enum":
            msg["enum"] = value
            msg["value"] = instance
        case "format":
            msg["attribute"] = value
            msg["value"] = instance
    return msg


def validate(schema_path: str, body: bytes | str) -> list[dict[str, Any]]:
    """Validate ``body`` against ``schema_path`` (e.g. ``/schemas/v2.1/metadata.json``).

    Returns fge-like error messages (empty if valid).
    Raises :class:`SchemaNotFound` or :class:`InvalidJson`.
    """
    validator = _validator(schema_path)
    instance = parse_json(body)
    messages: list[dict[str, Any]] = []
    seen_required: set[str] = set()
    try:
        for err in validator.iter_errors(instance):
            msg = _to_message(err, schema_path)
            if msg["keyword"] == "required":  # jsonschema reports one error per missing property
                key = msg["instance"]["pointer"] + "|" + msg["schema"]["pointer"]
                if key in seen_required:
                    continue
                seen_required.add(key)
                msg["message"] = f"object has missing required properties ({json.dumps(msg['missing'])})"
            messages.append(msg)
    except (Unresolvable, NoSuchResource) as e:
        messages.append({"level": "fatal", "domain": "validation", "message": f"unresolvable reference: {e}"})
    return messages
