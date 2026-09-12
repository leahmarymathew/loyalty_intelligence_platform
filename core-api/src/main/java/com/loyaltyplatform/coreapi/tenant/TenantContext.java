package com.loyaltyplatform.coreapi.tenant;

import java.util.UUID;

/**
 * Per-request holder for the authenticated caller's tenant. Populated by a
 * web filter (added with the REST layer) from the caller's credentials, and
 * read by {@link TenantSessionAspect} to bind the Postgres RLS session
 * variable that actually enforces isolation. This class is just a carrier —
 * it grants no access by itself.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(UUID tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static UUID get() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
