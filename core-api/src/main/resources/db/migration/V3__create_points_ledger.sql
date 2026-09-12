-- Append-only ledger: customer point balances are always derived by summing this
-- table, never stored as a mutable column. Every credit or debit is a new row;
-- corrections are made by inserting a compensating entry, never by editing history.
CREATE TABLE points_ledger (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL REFERENCES tenants (id),
    customer_id     UUID NOT NULL REFERENCES customers (id),
    transaction_id  UUID REFERENCES transactions (id),
    entry_type      VARCHAR(32) NOT NULL CHECK (entry_type IN
                        ('EARN', 'REDEEM', 'ADJUSTMENT', 'EXPIRY', 'CAMPAIGN_BONUS')),
    points          BIGINT NOT NULL,
    occurred_at     TIMESTAMPTZ NOT NULL,
    reference       VARCHAR(128),
    metadata        JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, reference)
);

CREATE INDEX idx_ledger_tenant_customer_time
    ON points_ledger (tenant_id, customer_id, occurred_at);

-- Enforced in the database, not just application code, so the append-only
-- guarantee holds even if a future query bypasses the service layer.
CREATE OR REPLACE FUNCTION reject_ledger_mutation() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'points_ledger is append-only: % is not permitted (row id=%)',
        TG_OP, COALESCE(OLD.id, NULL);
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_points_ledger_no_update
    BEFORE UPDATE ON points_ledger
    FOR EACH ROW EXECUTE FUNCTION reject_ledger_mutation();

CREATE TRIGGER trg_points_ledger_no_delete
    BEFORE DELETE ON points_ledger
    FOR EACH ROW EXECUTE FUNCTION reject_ledger_mutation();
