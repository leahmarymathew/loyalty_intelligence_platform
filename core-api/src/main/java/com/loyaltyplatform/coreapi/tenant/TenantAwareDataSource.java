package com.loyaltyplatform.coreapi.tenant;

import org.springframework.jdbc.datasource.DelegatingDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Stamps every physical connection checkout with the current tenant, which
 * is what makes the FORCE ROW LEVEL SECURITY policies (V9 migration) see a
 * tenant to filter on.
 *
 * <p>The first approach tried here hooked {@code JpaTransactionManager
 * .doBegin}, on the theory that every {@code @Transactional} boundary
 * funnels through one transaction manager. That held for Spring Data's base
 * CRUD methods (save, findAll) but not for custom derived-query methods
 * (findBySlug, findByTenantIdAndExternalRef) — caught by
 * PointsLedgerServiceIntegrationTest silently reading back zero rows for
 * data that had just been written. Rather than chase which interceptor
 * layer does or doesn't call which transaction manager, this hooks one
 * level lower, at the one place every one of those paths is guaranteed to
 * pass through: checking out a physical connection.
 *
 * <p>Uses set_config's SESSION scope (is_local=false), not transaction
 * scope: since this runs once per checkout rather than once per
 * transaction, session scope is what makes the setting actually visible to
 * the statements that follow within that checkout.
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    public TenantAwareDataSource(DataSource targetDataSource) {
        super(targetDataSource);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyTenant(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applyTenant(connection);
        return connection;
    }

    private void applyTenant(Connection connection) throws SQLException {
        UUID tenantId = TenantContext.get();
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT set_config('app.current_tenant_id', ?, false)")) {
            statement.setString(1, tenantId == null ? "" : tenantId.toString());
            statement.execute();
        }
    }
}
