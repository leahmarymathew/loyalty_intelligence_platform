package com.loyaltyplatform.coreapi.repository;

import com.loyaltyplatform.coreapi.domain.PointsLedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PointsLedgerEntryRepository extends JpaRepository<PointsLedgerEntry, UUID> {

    Optional<PointsLedgerEntry> findByTenantIdAndReference(UUID tenantId, String reference);

    List<PointsLedgerEntry> findByTenantIdAndCustomerIdOrderByOccurredAtDesc(UUID tenantId, UUID customerId);

    @Query("SELECT COALESCE(SUM(p.points), 0) FROM PointsLedgerEntry p "
            + "WHERE p.tenantId = :tenantId AND p.customerId = :customerId")
    long sumBalance(@Param("tenantId") UUID tenantId, @Param("customerId") UUID customerId);
}
