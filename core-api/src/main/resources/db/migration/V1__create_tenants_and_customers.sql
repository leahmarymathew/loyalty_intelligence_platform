CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tenants (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    slug         VARCHAR(64) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE customers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants (id),
    external_ref     VARCHAR(128),
    email            VARCHAR(255) NOT NULL,
    full_name        VARCHAR(255) NOT NULL,
    birth_date       DATE,
    current_tier_id  UUID,
    enrolled_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, external_ref)
);

CREATE INDEX idx_customers_tenant ON customers (tenant_id);
CREATE INDEX idx_customers_tenant_email ON customers (tenant_id, email);
