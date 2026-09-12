"""Application settings, sourced from environment variables.

Mirrors the variable names already declared in the repo root ``.env.example``
under the "AI Service (Python / FastAPI)" section, so `docker compose` and
local `.env` files work without renaming anything.
"""

from functools import lru_cache

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

    ai_service_port: int = 8000
    llm_provider: str = "fake"
    llm_api_key: str = ""
    vector_store_url: str = ""

    # Base URL of core-api, the system of record this service reads from
    # (customers, transactions, points ledger). Not wired up yet - the
    # recommender and RAG stubs below don't call it, but real
    # implementations will need it to pull tenant-scoped data.
    core_api_url: str = "http://localhost:8080"


@lru_cache
def get_settings() -> Settings:
    return Settings()
