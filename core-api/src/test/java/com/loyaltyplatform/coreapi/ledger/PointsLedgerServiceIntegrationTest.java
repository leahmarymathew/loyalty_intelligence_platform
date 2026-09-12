package com.loyaltyplatform.coreapi.ledger;

import com.loyaltyplatform.coreapi.domain.Customer;
import com.loyaltyplatform.coreapi.domain.PointsLedgerEntry;
import com.loyaltyplatform.coreapi.domain.PurchaseTransaction;
import com.loyaltyplatform.coreapi.domain.Tenant;
import com.loyaltyplatform.coreapi.repository.CustomerRepository;
import com.loyaltyplatform.coreapi.repository.PointsLedgerEntryRepository;
import com.loyaltyplatform.coreapi.repository.PurchaseTransactionRepository;
import com.loyaltyplatform.coreapi.repository.TenantRepository;
import com.loyaltyplatform.coreapi.support.AbstractIntegrationTest;
import com.loyaltyplatform.coreapi.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PointsLedgerServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PointsLedgerService ledgerService;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PurchaseTransactionRepository transactionRepository;

    @Autowired
    private PointsLedgerEntryRepository ledgerEntryRepository;

    private String tenantSlug;
    private UUID tenantId;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        tenantSlug = "tenant-" + UUID.randomUUID();
        Tenant tenant = tenantRepository.save(new Tenant(tenantSlug, "Test Tenant"));
        tenantId = tenant.getId();
        TenantContext.set(tenantId);

        Customer customer = customerRepository.save(
                new Customer(tenantId, "ext-1", "x@example.com", "X", LocalDate.of(1990, 1, 1)));
        customerId = customer.getId();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void balanceAccumulatesAcrossEarnAndRedeem() {
        ledgerService.recordEarn(tenantId, customerId, null, 500, Instant.now(), "earn-1");
        ledgerService.recordEarn(tenantId, customerId, null, 200, Instant.now(), "earn-2");
        ledgerService.recordRedemption(tenantId, customerId, 300, Instant.now(), "redeem-1");

        assertThat(ledgerService.getBalance(tenantId, customerId)).isEqualTo(400);
    }

    @Test
    void redemptionBeyondBalanceIsRejectedAndLeavesBalanceUnchanged() {
        ledgerService.recordEarn(tenantId, customerId, null, 100, Instant.now(), "earn-1");

        assertThatThrownBy(() ->
                ledgerService.recordRedemption(tenantId, customerId, 200, Instant.now(), "redeem-1"))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(ledgerService.getBalance(tenantId, customerId)).isEqualTo(100);
    }

    @Test
    void duplicateReferenceIsIdempotentAgainstTheRealUniqueConstraint() {
        PointsLedgerEntry first = ledgerService.recordEarn(tenantId, customerId, null, 100, Instant.now(), "same-ref");
        PointsLedgerEntry second = ledgerService.recordEarn(tenantId, customerId, null, 100, Instant.now(), "same-ref");

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(ledgerService.getBalance(tenantId, customerId)).isEqualTo(100);
    }

    @Test
    void backdatedEarnStillContributesToBalance() {
        Instant sixMonthsAgo = Instant.now().minusSeconds(180L * 24 * 3600);

        ledgerService.recordEarn(tenantId, customerId, null, 250, sixMonthsAgo, "backdated-1");

        assertThat(ledgerService.getBalance(tenantId, customerId)).isEqualTo(250);
    }

    @Test
    void earnLinkedToARealPurchaseTransactionIsTraceableBackToIt() {
        PurchaseTransaction transaction = transactionRepository.save(new PurchaseTransaction(
                tenantId, customerId, "pos-ref-1", Instant.now(), 4599, "USD", "grocery"));

        ledgerService.recordEarn(tenantId, customerId, transaction.getId(), 45, Instant.now(), "earn-pos-ref-1");

        List<PointsLedgerEntry> entries =
                ledgerEntryRepository.findByTenantIdAndCustomerIdOrderByOccurredAtDesc(tenantId, customerId);
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).getTransactionId()).isEqualTo(transaction.getId());

        assertThat(transactionRepository.findByTenantIdAndExternalRef(tenantId, "pos-ref-1")).isPresent();
    }

    @Test
    void tenantAndCustomerExposeTheirEnrollmentAndProfileFields() {
        assertThat(tenantRepository.findBySlug(tenantSlug)).isPresent();

        Customer customer = customerRepository.findByTenantIdAndExternalRef(tenantId, "ext-1").orElseThrow();
        assertThat(customer.getBirthDate()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(customer.getEnrolledAt()).isNotNull();
        assertThat(customer.getCurrentTierId()).isNull();

        UUID tierId = UUID.randomUUID();
        customer.assignTier(tierId);
        assertThat(customer.getCurrentTierId()).isEqualTo(tierId);
    }
}
