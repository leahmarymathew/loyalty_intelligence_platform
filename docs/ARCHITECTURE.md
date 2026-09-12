# Architecture

## Key design decisions

### Why an append-only points ledger instead of a balance column

A mutable `balance` column can only ever tell you the current number; it can't answer "why is this customer's balance what it is" or survive a bug that double-writes an update. `points_ledger` (`core-api/src/main/resources/db/migration/V3__create_points_ledger.sql`) stores every earn, redemption debit, manual adjustment, expiry, and campaign bonus as its own signed-integer row, and the balance shown anywhere is `SUM(points)` over that table. Two consequences follow: any balance can be reconstructed as of any past timestamp (audit, dispute resolution, "why did my tier change"), and corrections are new compensating rows, never edits to history. The append-only property isn't just a coding convention — `BEFORE UPDATE` and `BEFORE DELETE` triggers on the table raise an exception, so it holds even against a query that bypasses the service layer entirely.

### How multi-tenant isolation is enforced

Every tenant-scoped table has a Postgres row-level security policy (`V9__enable_row_level_security.sql`) that only admits rows where `tenant_id` matches a per-connection setting (`app.current_tenant_id`). Two migration-level details make this a real guarantee rather than a convention: the policy uses `FORCE ROW LEVEL SECURITY`, so it applies even to the role that owns the tables (Postgres exempts owners from RLS by default, which would otherwise make the policy a no-op for the app's own connection); and the tenant comparison guards against both an unset variable and Postgres's empty-string reset behavior (see below) — `tenant_id = NULL` is never true, so a forgotten tenant context fails closed (zero rows) instead of open (every tenant's rows).

The application side (`TenantAwareDataSource`, `com.loyaltyplatform.coreapi.tenant`) binds `TenantContext`'s value by wrapping `DataSource.getConnection()` — every physical connection checkout runs `set_config('app.current_tenant_id', ..., false)` before handing the connection back. This replaced two earlier attempts that looked reasonable but weren't, both caught by `PointsLedgerServiceIntegrationTest` and `TenantIsolationIntegrationTest` actually running against real Postgres: an `@Aspect` matching the `@Transactional` pointcut never fired for Spring Data repository proxies (they aren't woven by user-defined aspects the same way a plain `@Service` is), and a `JpaTransactionManager.doBegin` override worked for base CRUD methods (`save`, `findAll`) but not custom derived-query methods (`findBySlug`), which don't reliably route through the same transaction manager hook. Binding at the connection-checkout level sidesteps the question of which interceptor layer initiated the call, because every path ends at the same `DataSource`.

This pushes isolation below the ORM: a service-layer bug that omits a `WHERE tenant_id = ?` clause still can't leak another tenant's data, because the database itself won't return it.

### How the recommender avoids leaking future information into training features

_TBD — addressed when the AI service recommender is built._

## Known limitations

- `TenantContext` is currently populated only by test code; the web-layer piece that reads it from an authenticated request (JWT/header → `TenantContext.set(...)`) lands with the REST layer, not this stage.
- `current_tier_id` on `customers` is a cached pointer for fast reads, populated by the tier evaluation engine — it is never the source of truth for eligibility, which is always recomputed from `tier_rules` against the ledger-derived balance.
- Once a custom Postgres GUC like `app.current_tenant_id` has been set at least once via `set_config(..., is_local=true)` in a session, it resets to an empty string at transaction end, not `NULL` — not documented behavior we found written down anywhere; found by a test failing with `invalid input syntax for type uuid: ""` on a pooled connection reused across tenant contexts. `V11__fix_rls_empty_tenant_setting.sql`'s policies guard against it with `NULLIF(..., '')`, and `TenantAwareDataSource` sidesteps it entirely by using session scope (`is_local=false`) and re-stamping on every checkout rather than relying on transaction-end reset semantics.
- `mvn verify` requires a running Docker daemon (Testcontainers spins up real Postgres). Verified locally: all 11 migrations apply cleanly, the append-only trigger rejects UPDATE/DELETE on `points_ledger`, RLS isolates tenants and fails closed with no session tenant set (both via raw JDBC and through the real repository layer), and the ledger service's arithmetic, idempotency, and insufficient-balance rules hold against real Postgres. 26/26 tests passing, 86.9% line coverage on `core-api` (JaCoCo-enforced minimum: 80%).
