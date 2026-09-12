CREATE TABLE campaigns (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id      UUID NOT NULL REFERENCES tenants (id),
    name           VARCHAR(255) NOT NULL,
    campaign_type  VARCHAR(32) NOT NULL CHECK (campaign_type IN
                        ('BIRTHDAY', 'TIER_UPGRADE', 'WIN_BACK', 'POINTS_EXPIRY_WARNING')),
    rule_config    JSONB NOT NULL DEFAULT '{}'::jsonb,
    active         BOOLEAN NOT NULL DEFAULT true,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_campaigns_tenant_active ON campaigns (tenant_id, active);

CREATE TABLE offers (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id        UUID NOT NULL REFERENCES tenants (id),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    points_cost      BIGINT NOT NULL CHECK (points_cost >= 0),
    category         VARCHAR(64),
    inventory_count  INT CHECK (inventory_count IS NULL OR inventory_count >= 0),
    starts_at        TIMESTAMPTZ,
    ends_at          TIMESTAMPTZ,
    active           BOOLEAN NOT NULL DEFAULT true,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (starts_at IS NULL OR ends_at IS NULL OR ends_at > starts_at)
);

CREATE INDEX idx_offers_tenant_active ON offers (tenant_id, active);
