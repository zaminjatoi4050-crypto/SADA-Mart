package com.sadamart.sales;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentAllocatorTest {
    private static BigDecimal d(String value) {
        return new BigDecimal(value);
    }

    @Test
    void supportsSplitPayments() {
        var result = PaymentAllocator.allocate(d("125.50"), List.of(
                new PaymentAllocation(PaymentMethod.CASH, d("50")),
                new PaymentAllocation(PaymentMethod.CARD, d("75.50"))));

        assertEquals(d("125.50"), result.paid());
        assertEquals(d("0.00"), result.remaining());
        assertEquals(d("0.00"), result.change());
    }

    @Test
    void calculatesCashChange() {
        var result = PaymentAllocator.allocate(d("40.00"),
                List.of(new PaymentAllocation(PaymentMethod.CASH, d("50"))));

        assertEquals(d("10.00"), result.change());
        assertEquals(d("0.00"), result.remaining());
    }

    @Test
    void reportsUnderpaymentAndRejectsCompletion() {
        var result = PaymentAllocator.allocate(d("40.00"),
                List.of(new PaymentAllocation(PaymentMethod.CASH, d("30"))));

        assertEquals(d("10.00"), result.remaining());
        assertThrows(IllegalArgumentException.class, () -> PaymentAllocator.requireFullyPaid(result));
    }

    @Test
    void rejectsInvalidAmounts() {
        assertThrows(IllegalArgumentException.class,
                () -> PaymentAllocator.allocate(d("-1"), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new PaymentAllocation(PaymentMethod.CASH, d("1.001")));
    }
}
