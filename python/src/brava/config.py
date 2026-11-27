"""Application settings, read from ``BRAVA_*`` environment variables or a ``.env`` file."""

from __future__ import annotations

from functools import lru_cache
from pathlib import Path

from pydantic import field_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

_REPO_ROOT = Path(__file__).resolve().parents[3]


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_prefix="BRAVA_", env_file=".env", extra="ignore")

    # General
    advanced_mode: bool = False
    base_path: str = "/brapivalidator"
    resources_dir: Path = _REPO_ROOT / "resources"
    frontend_dir: Path = _REPO_ROOT / "frontend" / "dist"
    available_versions: list[str] | None = None
    tested_version: str = "v2.1"
    build_date: str = ""
    build_version: str = ""

    # Outgoing HTTP (requests to tested BrAPI servers)
    http_proxy: str | None = None
    verify_tls: bool = True
    request_timeout: float = 300.0

    # Advanced mode
    database_url: str = "sqlite+aiosqlite:///brava.db"
    admin_key: str | None = None
    base_domain: str = ""
    contact_email: str = ""
    sender_name: str = "The BRAVA team"
    resource_json_url: str = "https://brapi.org/json/brapi-resources.json"
    # Load that list and test it on startup while the database holds no public resource.
    auto_import_resources: bool = True
    keep_reports: int = 3
    timezone: str | None = None
    scheduler_concurrency: int = 4

    # Mail
    smtp_host: str | None = None
    smtp_port: int = 25
    smtp_user: str | None = None
    smtp_password: str | None = None
    smtp_starttls: bool = False
    mail_from: str = "brava@localhost"
    mail_from_name: str = "BRAVA"

    @field_validator("base_domain")
    @classmethod
    def _strip_slash(cls, v: str) -> str:
        return v.rstrip("/")

    @field_validator("base_path")
    @classmethod
    def _normalize_base_path(cls, v: str) -> str:
        v = "/" + v.strip("/")
        return "" if v == "/" else v

    @field_validator("available_versions", mode="before")
    @classmethod
    def _split_versions(cls, v: object) -> object:
        if isinstance(v, str):
            return [p.strip() for p in v.split(",") if p.strip()]
        return v

    @property
    def collections_dir(self) -> Path:
        return self.resources_dir / "collections"

    @property
    def schemas_dir(self) -> Path:
        return self.resources_dir / "schemas"


@lru_cache
def get_settings() -> Settings:
    return Settings()
