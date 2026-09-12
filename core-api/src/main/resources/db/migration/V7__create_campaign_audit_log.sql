-- Every scheduler-triggered action (birthday offer, tier-upgrade congrats,
-- win-back, expiry warning) writes one row here with the rule that fired and
-- the inputs it evaluated, so "why did this customer get this offer" is
-- always answerable after the fact.
CREATE TABLE campaign_audit_log (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants (id),
    campaign_id      UUID NOT NULL REFERENCES campaigns (id),
    customer_id      UUID NOT NULL REFERENCES customers (id),
    triggered_at     TIMESTAMPTZ NOT NULL,
    rule_snapshot    JSONB NOT NULL,
    inputs_snapshot  JSONB NOT NULL,
    action_taken     VARCHAR(64) NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_campaign_audit_tenant_customer
    ON campaign_audit_log (tenant_id, customer_id, triggered_at);
