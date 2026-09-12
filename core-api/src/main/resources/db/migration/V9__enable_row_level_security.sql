-- Multi-tenant isolation is enforced here, at the database, not only in
-- repository query code. Every tenant-scoped table gets a policy that only
-- admits rows matching the session's current tenant. FORCE ROW LEVEL SECURITY
-- means even the owning application role (loyalty_app) is subject to it —
-- only a superuser connection can bypass it, and nothing at runtime connects
-- as one. If a query is ever written without a tenant filter, or the session
-- variable is never set, current_setting(..., true) returns NULL and the
-- comparison fails closed: zero rows, not every tenant's rows.
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'customers', 'transactions', 'points_ledger', 'tiers', 'tier_rules',
        'campaigns', 'offers', 'redemptions', 'campaign_audit_log', 'idempotency_keys'
    ]
    LOOP
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', t);
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', t);
        EXECUTE format(
            'CREATE POLICY tenant_isolation ON %I
                USING (tenant_id = current_setting(''app.current_tenant_id'', true)::uuid)
                WITH CHECK (tenant_id = current_setting(''app.current_tenant_id'', true)::uuid)',
            t
        );
    END LOOP;
END $$;
