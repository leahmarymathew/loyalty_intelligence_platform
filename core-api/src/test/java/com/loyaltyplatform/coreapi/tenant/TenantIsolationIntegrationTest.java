package com.loyaltyplatform.coreapi.tenant;

import com.loyaltyplatform.coreapi.domain.Customer;
import com.loyaltyplatform.coreapi.domain.Tenant;
import com.loyaltyplatform.coreapi.repository.CustomerRepository;
import com.loyaltyplatform.coreapi.repository.TenantRepository;
import com.loyaltyplatform.coreapi.support.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test the assignment asked for by name: proves that tenant A cannot
 * read tenant B's data, through the real repository layer the application
 * actually uses (not a raw SQL probe). If TenantSessionAspect ever stops
 * wiring the session variable, or a future migration drops FORCE ROW LEVEL
 * SECURITY, this fails.
 */
class TenantIsolationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void tenantCannotReadAnotherTenantsCustomers() {
        Tenant tenantA = createTenant();
        Tenant tenantB = createTenant();

        TenantContext.set(tenantA.getId());
        customerRepository.save(new Customer(tenantA.getId(), null, "a@example.com", "Customer A", null));

        TenantContext.set(tenantB.getId());
        customerRepository.save(new Customer(tenantB.getId(), null, "b@example.com", "Customer B", null));

        TenantContext.set(tenantA.getId());
        List<Customer> visibleToA = customerRepository.findAll();

        assertThat(visibleToA).hasSize(1);
        assertThat(visibleToA.get(0).getEmail()).isEqualTo("a@example.com");
    }

    @Test
    void noTenantContextMeansNoCustomersAreVisible() {
        Tenant tenant = createTenant();
        TenantContext.set(tenant.getId());
        customerRepository.save(new Customer(tenant.getId(), null, "c@example.com", "Customer C", null));

        TenantContext.clear();

        assertThat(customerRepository.findAll()).isEmpty();
    }

    private Tenant createTenant() {
        return tenantRepository.save(new Tenant("tenant-" + UUID.randomUUID(), "Test Tenant"));
    }
}
