package com.loyaltyplatform.coreapi.migration;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Runs the real Flyway migrations against a real Postgres (via Testcontainers)
 * and proves the two guarantees the schema depends on: the points_ledger is
 * genuinely append-only, and row-level security genuinely blocks cross-tenant
 * reads — not just that the migrations execute without error.
 *
 * <p>The Testcontainers bootstrap user (like the official postgres image's
 * POSTGRES_USER) is a superuser, and superusers unconditionally bypass RLS no
 * matter what the policies say. So this deliberately mirrors production
 * (db/init/01-create-app-role.sh): bootstrap connects once to create a
 * non-superuser {@code loyalty_app} role that owns the schema, and every
 * migration and test connection after that uses loyalty_app — otherwise the
 * RLS tests below would pass even if the policies did nothing at all.
 */
@Testcontainers
class FlywayMigrationTest {

    private static final String APP_USER = "loyalty_app";
    private static final String APP_PASSWORD = "app-test-password";

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("loyalty");

    @BeforeAll
    static void createAppRoleAndMigrate() throws SQLException {
        try (Connection bootstrap = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var stmt = bootstrap.createStatement()) {
            stmt.execute("CREATE ROLE " + APP_USER + " LOGIN PASSWORD '" + APP_PASSWORD + "'");
            stmt.execute("ALTER DATABASE " + POSTGRES.getDatabaseName() + " OWNER TO " + APP_USER);
            stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + POSTGRES.getDatabaseName()
                    + " TO " + APP_USER);
        }

        Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), APP_USER, APP_PASSWORD)
                .load()
                .migrate();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), APP_USER, APP_PASSWORD);
    }

    @Test
    void allExpectedTablesExist() throws SQLException {
        List<String> expected = List.of(
                "tenants", "customers", "transactions", "points_ledger", "tiers",
                "tier_rules", "campaigns", "offers", "redemptions",
                "campaign_audit_log", "idempotency_keys");

        try (Connection conn = connect()) {
            for (String table : expected) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT to_regclass(?) IS NOT NULL")) {
                    ps.setString(1, "public." + table);
                    ResultSet rs = ps.executeQuery();
                    rs.next();
                    assertThat(rs.getBoolean(1))
                            .as("table %s should exist", table)
                            .isTrue();
                }
            }
        }
    }

    @Test
    void pointsLedgerRejectsUpdate() throws SQLException {
        try (Connection conn = connect()) {
            UUID tenantId = insertTenant(conn);
            UUID customerId = insertCustomer(conn, tenantId);
            UUID ledgerId = insertLedgerEntry(conn, tenantId, customerId, 100);

            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE points_ledger SET points = 999 WHERE id = ?")) {
                ps.setObject(1, ledgerId);
                assertThrows(SQLException.class, ps::executeUpdate);
            }
        }
    }

    @Test
    void pointsLedgerRejectsDelete() throws SQLException {
        try (Connection conn = connect()) {
            UUID tenantId = insertTenant(conn);
            UUID customerId = insertCustomer(conn, tenantId);
            UUID ledgerId = insertLedgerEntry(conn, tenantId, customerId, 100);

            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM points_ledger WHERE id = ?")) {
                ps.setObject(1, ledgerId);
                assertThrows(SQLException.class, ps::executeUpdate);
            }
        }
    }

    @Test
    void rowLevelSecurityHidesOtherTenantsRowsWhenSessionTenantIsSet() throws SQLException {
        try (Connection conn = connect()) {
            UUID tenantA = insertTenant(conn);
            UUID tenantB = insertTenant(conn);
            insertCustomer(conn, tenantA);
            insertCustomer(conn, tenantB);

            setSessionTenant(conn, tenantA);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT count(*) FROM customers")) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                assertThat(rs.getInt(1)).isEqualTo(1);
            }
        }
    }

    @Test
    void rowLevelSecurityFailsClosedWhenSessionTenantIsNotSet() throws SQLException {
        try (Connection conn = connect()) {
            insertTenant(conn);

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT count(*) FROM customers")) {
                ResultSet rs = ps.executeQuery();
                rs.next();
                assertThat(rs.getInt(1)).isZero();
            }
        }
    }

    private void setSessionTenant(Connection conn, UUID tenantId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT set_config('app.current_tenant_id', ?, false)")) {
            ps.setString(1, tenantId.toString());
            ps.execute();
        }
    }

    private UUID insertTenant(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tenants (slug, name) VALUES (?, ?) RETURNING id")) {
            ps.setString(1, "tenant-" + UUID.randomUUID());
            ps.setString(2, "Test Tenant");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return (UUID) rs.getObject(1);
        }
    }

    private UUID insertCustomer(Connection conn, UUID tenantId) throws SQLException {
        setSessionTenant(conn, tenantId);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customers (tenant_id, email, full_name) VALUES (?, ?, ?) RETURNING id")) {
            ps.setObject(1, tenantId);
            ps.setString(2, "customer@example.com");
            ps.setString(3, "Test Customer");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return (UUID) rs.getObject(1);
        }
    }

    private UUID insertLedgerEntry(Connection conn, UUID tenantId, UUID customerId, long points)
            throws SQLException {
        setSessionTenant(conn, tenantId);
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO points_ledger (tenant_id, customer_id, entry_type, points, occurred_at) "
                        + "VALUES (?, ?, 'EARN', ?, now()) RETURNING id")) {
            ps.setObject(1, tenantId);
            ps.setObject(2, customerId);
            ps.setLong(3, points);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return (UUID) rs.getObject(1);
        }
    }
}
