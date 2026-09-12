CREATE TABLE tiers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id   UUID NOT NULL REFERENCES tenants (id),
    name        VARCHAR(64) NOT NULL,
    rank        INT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, rank),
    UNIQUE (tenant_id, name)
);

ALTER TABLE customers
    ADD CONSTRAINT fk_customers_current_tier
    FOREIGN KEY (current_tier_id) REFERENCES tiers (id);

-- Thresholds live in the database, versioned by effective date, so a brand
-- can change tier rules without a code deploy: the engine reads whichever
-- row is effective at evaluation time instead of a hardcoded constant.
CREATE TABLE tier_rules (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL REFERENCES tenants (id),
    tier_id                 UUID NOT NULL REFERENCES tiers (id),
    min_points_threshold    BIGINT NOT NULL CHECK (min_points_threshold >= 0),
    evaluation_window_days  INT,
    effective_from          TIMESTAMPTZ NOT NULL DEFAULT now(),
    effective_to            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (effective_to IS NULL OR effective_to > effective_from)
);

CREATE INDEX idx_tier_rules_tenant_tier_effective
    ON tier_rules (tenant_id, tier_id, effective_from);
