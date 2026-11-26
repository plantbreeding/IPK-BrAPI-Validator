"""Test collections (``collections/CompleteBrapiTest.<version>.json``)."""

from __future__ import annotations

import re
from functools import cache
from pathlib import Path

from pydantic import BaseModel, ConfigDict, Field

from brava.config import get_settings


class _Model(BaseModel):
    model_config = ConfigDict(extra="ignore")


class Param(_Model):
    param: str = ""
    value: str = ""


class Request(_Model):
    method: str = "GET"
    url: str


class Event(_Model):
    exec: list[str] = Field(default_factory=list)


class Item(_Model):
    name: str
    endpoint: str = ""
    description: str = ""
    request: Request
    event: list[Event] = Field(default_factory=list)
    requires: list[str] = Field(default_factory=list)
    parameters: list[Param] = Field(default_factory=list)

    @property
    def commands(self) -> list[str]:
        # Like the Java runner only the first event is executed.
        return self.event[0].exec if self.event else []


class Folder(_Model):
    name: str
    description: str = ""
    item: list[Item] = Field(default_factory=list)


class Info(_Model):
    name: str = ""
    description: str = ""


class Collection(_Model):
    info: Info = Field(default_factory=Info)
    item: list[Folder] = Field(default_factory=list)


_FILE_RE = re.compile(r"^CompleteBrapiTest\.(v[\w.]+)\.json$")


def _version_key(v: str) -> tuple[int, ...]:
    return tuple(int(p) for p in re.findall(r"\d+", v))


def _collections_dir() -> Path:
    return get_settings().collections_dir


def available_versions() -> list[str]:
    """Versions that can be tested: the configured list, or every collection file found."""
    configured = get_settings().available_versions
    if configured:
        return list(configured)
    found = (m.group(1) for p in _collections_dir().glob("*.json") if (m := _FILE_RE.match(p.name)))
    return sorted(found, key=_version_key)


@cache
def load_collection(version: str) -> Collection:
    path = _collections_dir() / f"CompleteBrapiTest.{version}.json"
    if not path.is_file():
        raise FileNotFoundError(f"No test collection for version {version}")
    return Collection.model_validate_json(path.read_bytes())
