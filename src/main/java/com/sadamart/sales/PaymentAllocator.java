package com.sadamart.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

public final class PaymentAllocator {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private PaymentAllocator() {
    }

    public record Result(BigDecimal due, BigDecimal paid, BigDecimal change, BigDecimal remaining) {
    }

    public static Result allocate(BigDecimal due, List<PaymentAllocation> payments) {
        Objects.requireNonNull(due, "due");
        Objects.requireNonNull(payments, "payments");
        BigDecimal normalizedDue = money(due, "Due amount");
        BigDecimal paid = ZERO;
        for (PaymentAllocation payment : payments) {
            paid = paid.add(payment.amount());
        }
        BigDecimal change = paid.subtract(normalizedDue).max(ZERO).setScale(2, RoundingMode.UNNECESSARY);
        BigDecimal remaining = normalizedDue.subtract(paid).max(ZERO).setScale(2, RoundingMode.UNNECESSARY);
        return new Result(normalizedDue, paid.setScale(2), change, remaining);
    }

    public static void requireFullyPaid(Result result) {
        Objects.requireNonNull(result, "result");
        if (result.remaining().signum() > 0) {
            throw new IllegalArgumentException("Payment does not cover the amount due");
        }
    }

    private static BigDecimal money(BigDecimal value, String name) {
        if (value.signum() < 0 || value.scale() > 2) {
            throw new IllegalArgumentException(name + " must be non-negative with at most two decimal places");
        }
        return value.setScale(2);
    }
}
