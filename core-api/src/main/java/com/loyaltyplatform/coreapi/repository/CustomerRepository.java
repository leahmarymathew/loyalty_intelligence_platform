package com.loyaltyplatform.coreapi.repository;

import com.loyaltyplatform.coreapi.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findByTenantId(UUID tenantId);

    Optional<Customer> findByTenantIdAndId(UUID tenantId, UUID id);

    Optional<Customer> findByTenantIdAndExternalRef(UUID tenantId, String externalRef);
}
