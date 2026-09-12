package com.loyaltyplatform.coreapi.ledger;

import com.loyaltyplatform.coreapi.domain.LedgerEntryType;
import com.loyaltyplatform.coreapi.domain.PointsLedgerEntry;
import com.loyaltyplatform.coreapi.repository.PointsLedgerEntryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * The only place that writes to points_ledger. Balances are never stored —
 * every read derives from summing entries (see getBalance) — so this class
 * is where the append-only-ledger design decision actually lives.
 */
@Service
public class PointsLedgerService {

    private final PointsLedgerEntryRepository ledgerRepository;

    public PointsLedgerService(PointsLedgerEntryRepository ledgerRepository) {
        this.ledgerRepository = ledgerRepository;
    }

    @Transactional
    public PointsLedgerEntry recordEarn(UUID tenantId, UUID customerId, UUID transactionId, long points,
                                         Instant occurredAt, String reference) {
        if (points <= 0) {
            throw new IllegalArgumentException("Earn entries must be positive: " + points);
        }
        return recordEntry(tenantId, customerId, transactionId, LedgerEntryType.EARN, points, occurredAt, reference);
    }

    @Transactional
    public PointsLedgerEntry recordRedemption(UUID tenantId, UUID customerId, long points, Instant occurredAt,
                                               String reference) {
        if (points <= 0) {
            throw new IllegalArgumentException("Redemption debits must be a positive point count: " + points);
        }
        long balance = getBalance(tenantId, customerId);
        if (balance < points) {
            throw new InsufficientBalanceException(customerId, balance, points);
        }
        return recordEntry(tenantId, customerId, null, LedgerEntryType.REDEEM, -points, occurredAt, reference);
    }

    @Transactional
    public PointsLedgerEntry recordAdjustment(UUID tenantId, UUID customerId, long points, Instant occurredAt,
                                               String reference) {
        if (points == 0) {
            throw new IllegalArgumentException("Adjustment must be non-zero");
        }
        return recordEntry(tenantId, customerId, null, LedgerEntryType.ADJUSTMENT, points, occurredAt, reference);
    }

    @Transactional(readOnly = true)
    public long getBalance(UUID tenantId, UUID customerId) {
        return ledgerRepository.sumBalance(tenantId, customerId);
    }

    private PointsLedgerEntry recordEntry(UUID tenantId, UUID customerId, UUID transactionId, LedgerEntryType type,
                                           long points, Instant occurredAt, String reference) {
        if (reference != null) {
            Optional<PointsLedgerEntry> existing = ledgerRepository.findByTenantIdAndReference(tenantId, reference);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        try {
            PointsLedgerEntry entry =
                    new PointsLedgerEntry(tenantId, customerId, transactionId, type, points, occurredAt, reference);
            return ledgerRepository.saveAndFlush(entry);
        } catch (DataIntegrityViolationException raced) {
            // Another concurrent call won the (tenant_id, reference) unique constraint between our
            // existence check and insert. That's the same request retried, not an error: return
            // what it wrote instead of propagating.
            if (reference == null) {
                throw raced;
            }
            return ledgerRepository.findByTenantIdAndReference(tenantId, reference).orElseThrow(() -> raced);
        }
    }
}
