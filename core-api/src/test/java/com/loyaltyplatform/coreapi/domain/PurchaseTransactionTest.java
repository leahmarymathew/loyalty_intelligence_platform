package com.loyaltyplatform.coreapi.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PurchaseTransactionTest {

    private final UUID tenantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final Instant occurredAt = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void rejectsNegativeAmount() {
        assertThatThrownBy(() ->
                new PurchaseTransaction(tenantId, customerId, "ext-1", occurredAt, -1, "USD", "grocery"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void allowsZeroAmount() {
        PurchaseTransaction tx =
                new PurchaseTransaction(tenantId, customerId, "ext-1", occurredAt, 0, "USD", "grocery");

        assertThat(tx.getAmountCents()).isZero();
    }

    @Test
    void exposesConstructorValuesThroughGetters() {
        PurchaseTransaction tx =
                new PurchaseTransaction(tenantId, customerId, "ext-2", occurredAt, 2599, "USD", "electronics");

        assertThat(tx.getTenantId()).isEqualTo(tenantId);
        assertThat(tx.getCustomerId()).isEqualTo(customerId);
        assertThat(tx.getExternalRef()).isEqualTo("ext-2");
        assertThat(tx.getOccurredAt()).isEqualTo(occurredAt);
        assertThat(tx.getAmountCents()).isEqualTo(2599);
        assertThat(tx.getCurrency()).isEqualTo("USD");
        assertThat(tx.getCategory()).isEqualTo("electronics");
    }
}
