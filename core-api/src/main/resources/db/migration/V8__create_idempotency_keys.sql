-- Generic idempotency store for write endpoints (e.g. POST /transactions):
-- a repeated request with the same key returns the original response instead
-- of re-executing the side effect.
CREATE TABLE idempotency_keys (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants (id),
    idempotency_key  VARCHAR(128) NOT NULL,
    endpoint         VARCHAR(128) NOT NULL,
    request_hash     VARCHAR(64) NOT NULL,
    response_status  INT NOT NULL,
    response_body    JSONB NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, endpoint, idempotency_key)
);
