-- Optimistic locking on the contended resource (offer inventory), so two
-- concurrent redemptions of the last unit of an offer race on `version` and
-- one loses cleanly instead of both succeeding and overselling.
ALTER TABLE offers ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

CREATE TABLE redemptions (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id              UUID NOT NULL REFERENCES tenants (id),
    customer_id            UUID NOT NULL REFERENCES customers (id),
    offer_id               UUID NOT NULL REFERENCES offers (id),
    points_ledger_entry_id UUID REFERENCES points_ledger (id),
    status                 VARCHAR(16) NOT NULL CHECK (status IN
                                ('PENDING', 'CONFIRMED', 'CANCELLED')),
    idempotency_key        VARCHAR(128) NOT NULL,
    version                BIGINT NOT NULL DEFAULT 0,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, idempotency_key)
);

CREATE INDEX idx_redemptions_tenant_customer ON redemptions (tenant_id, customer_id);
CREATE INDEX idx_redemptions_tenant_offer ON redemptions (tenant_id, offer_id);
