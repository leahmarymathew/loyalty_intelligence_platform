CREATE TABLE transactions (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID NOT NULL REFERENCES tenants (id),
    customer_id    UUID NOT NULL REFERENCES customers (id),
    external_ref   VARCHAR(128),
    occurred_at    TIMESTAMPTZ NOT NULL,
    amount_cents   BIGINT NOT NULL CHECK (amount_cents >= 0),
    currency       CHAR(3) NOT NULL DEFAULT 'USD',
    category       VARCHAR(64),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, external_ref)
);

CREATE INDEX idx_transactions_tenant_customer_time
    ON transactions (tenant_id, customer_id, occurred_at);
