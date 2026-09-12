package com.loyaltyplatform.coreapi.support;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Boots the full Spring context against a real Postgres container, using a
 * non-superuser application role — not the Testcontainers bootstrap user,
 * which (like the official postgres image's POSTGRES_USER) is a superuser
 * and would silently bypass every RLS policy, making any isolation test
 * built on it meaningless. This mirrors what
 * core-api/db/init/01-create-app-role.sh does for real deployments.
 *
 * <p>The container is started manually (the "singleton container" pattern)
 * rather than via {@code @Testcontainers @Container}: that annotation-based
 * approach stops the container in {@code afterAll} of whichever subclass
 * happens to run first, leaving the next subclass unable to connect —
 * caught by TenantIsolationIntegrationTest failing with "connection
 * refused" right after PointsLedgerServiceIntegrationTest had run. A
 * container started in a static initializer and never explicitly stopped
 * is reused across every subclass for the life of the JVM; Ryuk (started
 * automatically by Testcontainers) tears it down when the test JVM exits.
 */
@SpringBootTest
public abstract class AbstractIntegrationTest {

    protected static final String APP_USER = "loyalty_app";
    protected static final String APP_PASSWORD = "app-test-password";

    protected static final PostgreSQLContainer<?> POSTGRES;

    static {
        POSTGRES = new PostgreSQLContainer<>("postgres:16").withDatabaseName("loyalty");
        POSTGRES.start();
        createAppRoleIfAbsent();
    }

    @DynamicPropertySource
    static void registerDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", () -> APP_USER);
        registry.add("spring.datasource.password", () -> APP_PASSWORD);
    }

    private static void createAppRoleIfAbsent() {
        try (Connection bootstrap = DriverManager.getConnection(
                POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             Statement stmt = bootstrap.createStatement()) {
            stmt.execute("""
                    DO $$
                    BEGIN
                        IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = '%s') THEN
                            CREATE ROLE %s LOGIN PASSWORD '%s';
                        END IF;
                    END $$
                    """.formatted(APP_USER, APP_USER, APP_PASSWORD));
            stmt.execute("ALTER DATABASE " + POSTGRES.getDatabaseName() + " OWNER TO " + APP_USER);
            stmt.execute("GRANT ALL PRIVILEGES ON DATABASE " + POSTGRES.getDatabaseName() + " TO " + APP_USER);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to provision test app role", e);
        }
    }
}
