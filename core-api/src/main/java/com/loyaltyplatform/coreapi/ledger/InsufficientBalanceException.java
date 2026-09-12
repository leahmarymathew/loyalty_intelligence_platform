package com.loyaltyplatform.coreapi.ledger;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(UUID customerId, long balance, long requested) {
        super("Customer %s has balance %d, cannot debit %d".formatted(customerId, balance, requested));
    }
}
