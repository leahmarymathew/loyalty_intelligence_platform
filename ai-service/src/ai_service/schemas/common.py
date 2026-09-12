"""Shared request/response building blocks.

Every db-backed or tenant-owned-data endpoint in this service takes a
``tenant_id`` and is expected to scope all reads/writes to it, consistent
with core-api's tenant isolation model (see docs/ARCHITECTURE.md - Postgres
RLS keyed on ``tenant_id`` is core-api's enforcement mechanism; this service
has no database yet, so for now scoping is contract-level only, via these
required fields).
"""

from uuid import UUID

from pydantic import BaseModel, Field


class TenantScopedRequest(BaseModel):
    """Base class for any request that acts on behalf of a specific tenant."""

    tenant_id: UUID = Field(..., description="Identifies the retail brand tenant making the request.")
