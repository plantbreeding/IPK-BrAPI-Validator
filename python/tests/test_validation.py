"""Validation against the real schema trees in src/main/resources/schemas."""

from __future__ import annotations

import json

import pytest

from brava import config, validation


@pytest.fixture(autouse=True)
def _real_resources(monkeypatch: pytest.MonkeyPatch, tmp_path) -> None:
    monkeypatch.chdir(tmp_path)
    monkeypatch.delenv("BRAVA_RESOURCES_DIR", raising=False)
    config.get_settings.cache_clear()
    validation._validator.cache_clear()
    yield
    config.get_settings.cache_clear()
    validation._validator.cache_clear()


def test_relative_refs_resolve_v1_2() -> None:
    body = {"metadata": {}, "result": {"germplasmDbId": 1}}
    errors = validation.validate("/schemas/v1.2/germplasm/germplasmDbId.json", json.dumps(body))
    assert errors, "a wrong type deep inside a $ref'd definition is reported"
    assert all(e["level"] == "error" for e in errors)


def test_fixed_v1_trait_ref_resolves() -> None:
    errors = validation.validate("/schemas/v1/traits/traitDbId.json", json.dumps({"result": {}}))
    assert not any(e["level"] == "fatal" for e in errors)


def test_v2_schema_valid_and_invalid() -> None:
    path = "/schemas/v2.1/metadata.json"
    ok = {"metadata": {"pagination": {"currentPage": 0, "pageSize": 10, "totalCount": 1, "totalPages": 1},
                       "status": [], "datafiles": []}}  # fmt: skip
    assert validation.validate(path, json.dumps(ok)) == []
    assert validation.validate(path, json.dumps({}))[0]["keyword"] == "required"


def test_errors() -> None:
    with pytest.raises(validation.SchemaNotFound):
        validation.validate("/schemas/v2.1/does-not-exist.json", "{}")
    with pytest.raises(validation.InvalidJson):
        validation.validate("/schemas/v2.1/metadata.json", "<html>")
    with pytest.raises(validation.InvalidJson):
        validation.validate("/schemas/v2.1/metadata.json", b"")
    with pytest.raises(validation.SchemaNotFound):
        validation.validate("/schemas/../../config.properties", "{}")
