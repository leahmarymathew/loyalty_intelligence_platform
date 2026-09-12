package com.loyaltyplatform.coreapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class PurchaseTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "external_ref", length = 128)
    private String externalRef;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 64)
    private String category;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PurchaseTransaction() {
    }

    public PurchaseTransaction(UUID tenantId, UUID customerId, String externalRef, Instant occurredAt,
                                long amountCents, String currency, String category) {
        if (amountCents < 0) {
            throw new IllegalArgumentException("amountCents must not be negative: " + amountCents);
        }
        this.tenantId = tenantId;
        this.customerId = customerId;
        this.externalRef = externalRef;
        this.occurredAt = occurredAt;
        this.amountCents = amountCents;
        this.currency = currency;
        this.category = category;
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

    public String getExternalRef() {
        return externalRef;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCategory() {
        return category;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
