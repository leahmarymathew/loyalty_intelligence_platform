package com.loyaltyplatform.coreapi.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * tenantId, and every other cross-entity reference in this package, is
 * modeled as a plain UUID field rather than a JPA @ManyToOne association.
 * These tables are write-heavy (ledger, transactions) and this avoids
 * incidental lazy-loading behavior; joins are written explicitly in
 * repository queries where they're actually needed.
 */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "external_ref", length = 128)
    private String externalRef;

    @Column(nullable = false)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "current_tier_id")
    private UUID currentTierId;

    @CreationTimestamp
    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private Instant enrolledAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Customer() {
    }

    public Customer(UUID tenantId, String externalRef, String email, String fullName, LocalDate birthDate) {
        this.tenantId = tenantId;
        this.externalRef = externalRef;
        this.email = email;
        this.fullName = fullName;
        this.birthDate = birthDate;
    }

    public void assignTier(UUID tierId) {
        this.currentTierId = tierId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getExternalRef() {
        return externalRef;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public UUID getCurrentTierId() {
        return currentTierId;
    }

    public Instant getEnrolledAt() {
        return enrolledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
