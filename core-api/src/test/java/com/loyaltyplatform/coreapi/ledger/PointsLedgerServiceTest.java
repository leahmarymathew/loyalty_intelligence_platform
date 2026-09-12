package com.loyaltyplatform.coreapi.ledger;

import com.loyaltyplatform.coreapi.domain.LedgerEntryType;
import com.loyaltyplatform.coreapi.domain.PointsLedgerEntry;
import com.loyaltyplatform.coreapi.repository.PointsLedgerEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointsLedgerServiceTest {

    @Mock
    private PointsLedgerEntryRepository ledgerRepository;

    private PointsLedgerService service;

    private final UUID tenantId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();
    private final Instant now = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        service = new PointsLedgerService(ledgerRepository);
    }

    @Test
    void recordEarnRejectsNonPositivePoints() {
        assertThatThrownBy(() -> service.recordEarn(tenantId, customerId, null, 0, now, "ref"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.recordEarn(tenantId, customerId, null, -5, now, "ref"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordEarnPersistsAPositiveEntry() {
        when(ledgerRepository.findByTenantIdAndReference(tenantId, "earn-1")).thenReturn(Optional.empty());
        when(ledgerRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        PointsLedgerEntry entry = service.recordEarn(tenantId, customerId, null, 150, now, "earn-1");

        assertThat(entry.getEntryType()).isEqualTo(LedgerEntryType.EARN);
        assertThat(entry.getPoints()).isEqualTo(150);
    }

    @Test
    void recordRedemptionDebitsAsNegativePoints() {
        when(ledgerRepository.sumBalance(tenantId, customerId)).thenReturn(500L);
        when(ledgerRepository.findByTenantIdAndReference(tenantId, "redeem-1")).thenReturn(Optional.empty());
        when(ledgerRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        PointsLedgerEntry entry = service.recordRedemption(tenantId, customerId, 300, now, "redeem-1");

        assertThat(entry.getEntryType()).isEqualTo(LedgerEntryType.REDEEM);
        assertThat(entry.getPoints()).isEqualTo(-300);
    }

    @Test
    void recordRedemptionRejectsWhenBalanceInsufficient() {
        when(ledgerRepository.sumBalance(tenantId, customerId)).thenReturn(100L);

        assertThatThrownBy(() -> service.recordRedemption(tenantId, customerId, 300, now, "redeem-1"))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(ledgerRepository, never()).saveAndFlush(any());
    }

    @Test
    void recordRedemptionRejectsNonPositivePoints() {
        assertThatThrownBy(() -> service.recordRedemption(tenantId, customerId, 0, now, "ref"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordAdjustmentRejectsZero() {
        assertThatThrownBy(() -> service.recordAdjustment(tenantId, customerId, 0, now, "ref"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void recordAdjustmentAllowsNegativePoints() {
        when(ledgerRepository.findByTenantIdAndReference(tenantId, "adj-1")).thenReturn(Optional.empty());
        when(ledgerRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

        PointsLedgerEntry entry = service.recordAdjustment(tenantId, customerId, -50, now, "adj-1");

        assertThat(entry.getEntryType()).isEqualTo(LedgerEntryType.ADJUSTMENT);
        assertThat(entry.getPoints()).isEqualTo(-50);
    }

    @Test
    void duplicateReferenceIsIdempotentWithoutASecondWrite() {
        PointsLedgerEntry existing =
                new PointsLedgerEntry(tenantId, customerId, null, LedgerEntryType.EARN, 100, now, "same-ref");
        when(ledgerRepository.findByTenantIdAndReference(tenantId, "same-ref")).thenReturn(Optional.of(existing));

        PointsLedgerEntry result = service.recordEarn(tenantId, customerId, null, 100, now, "same-ref");

        assertThat(result).isSameAs(existing);
        verify(ledgerRepository, never()).saveAndFlush(any());
    }

    @Test
    void concurrentDuplicateReferenceFallsBackToTheWinningRowInsteadOfThrowing() {
        PointsLedgerEntry winner =
                new PointsLedgerEntry(tenantId, customerId, null, LedgerEntryType.EARN, 100, now, "race-ref");

        when(ledgerRepository.findByTenantIdAndReference(tenantId, "race-ref"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(winner));
        when(ledgerRepository.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("unique violation"));

        PointsLedgerEntry result = service.recordEarn(tenantId, customerId, null, 100, now, "race-ref");

        assertThat(result).isSameAs(winner);
    }

    @Test
    void balanceIsReadDirectlyFromTheRepositorySum() {
        when(ledgerRepository.sumBalance(tenantId, customerId)).thenReturn(275L);

        assertThat(service.getBalance(tenantId, customerId)).isEqualTo(275);
        verify(ledgerRepository).sumBalance(eq(tenantId), eq(customerId));
    }
}
