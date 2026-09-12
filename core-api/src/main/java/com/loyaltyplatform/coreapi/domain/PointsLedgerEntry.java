package com.loyaltyplatform.coreapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.Instant;
import java.util.UUID;

/**
 * One row per credit or debit; balances are always derived by summing this
 * table (see PointsLedgerService#getBalance), never stored as a mutable
 * column. {@code @Immutable} tells Hibernate to never issue an UPDATE for
 * this entity even if a field were mutated in memory — matching the
 * database-level guarantee from the BEFORE UPDATE/DELETE triggers in the V3
 * migration. Corrections are new compensating rows, never edits.
 */
@Entity
@Table(name = "points_ledger")
@Immutable
public class PointsLedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 32)
    private LedgerEntryType entryType;

    @Column(nullable = false)
    private long points;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(length = 128)
    private String reference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PointsLedgerEntry() {
    }

    public PointsLedgerEntry(UUID tenantId, UUID customerId, UUID transactionId, LedgerEntryType entryType,
                              long points, Instant occurredAt, String reference) {
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.transactionId = transactionId;
        this.entryType = entryType;
        this.points = points;
        this.occurredAt = occurredAt;
        this.reference = reference;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public LedgerEntryType getEntryType() {
        return entryType;
    }

    public long getPoints() {
        return points;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
