-- Once a custom GUC like app.current_tenant_id has been set at least once in
-- a session via set_config(..., is_local=true), it reverts to an empty
-- string '' at transaction end, not NULL. On a pooled connection that
-- previously served a tenant-scoped request and now serves one with no
-- tenant context, current_setting(..., true)::uuid was raising a hard
-- error ("invalid input syntax for type uuid") instead of the intended
-- fail-closed behavior (zero rows). NULLIF converts the empty string back
-- to NULL before the cast, so both "never set" and "reset to empty" fail
-- closed the same way, matching V9's original intent.
DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY[
        'customers', 'transactions', 'points_ledger', 'tiers', 'tier_rules',
        'campaigns', 'offers', 'redemptions', 'campaign_audit_log', 'idempotency_keys'
    ]
    LOOP
        EXECUTE format('DROP POLICY tenant_isolation ON %I', t);
        EXECUTE format(
            'CREATE POLICY tenant_isolation ON %I
                USING (tenant_id = NULLIF(current_setting(''app.current_tenant_id'', true), '''')::uuid)
                WITH CHECK (tenant_id = NULLIF(current_setting(''app.current_tenant_id'', true), '''')::uuid)',
            t
        );
    END LOOP;
END $$;
