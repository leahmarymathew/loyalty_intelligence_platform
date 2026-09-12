package com.loyaltyplatform.coreapi.repository;

import com.loyaltyplatform.coreapi.domain.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, UUID> {

    Optional<PurchaseTransaction> findByTenantIdAndExternalRef(UUID tenantId, String externalRef);
}
