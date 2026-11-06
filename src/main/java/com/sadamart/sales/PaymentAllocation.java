package com.sadamart.sales;

import java.math.BigDecimal;
import java.util.Objects;

public record PaymentAllocation(PaymentMethod method, BigDecimal amount) {
    public PaymentAllocation {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(amount, "amount");
        if (amount.signum() <= 0 || amount.scale() > 2) {
            throw new IllegalArgumentException("Payment amount must be positive with at most two decimal places");
        }
        amount = amount.setScale(2);
    }
}
