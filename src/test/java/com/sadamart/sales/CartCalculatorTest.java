package com.sadamart.sales;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CartCalculatorTest {
    private static BigDecimal d(String n) { return new BigDecimal(n); }
    @Test void calculatesTaxAfterBothDiscounts() {
        var totals = CartCalculator.calculate(List.of(new CartCalculator.Line(d("100.00"), d("2"), d("10.00")), new CartCalculator.Line(d("20"), d("1"), d("0"))), d("10"), d("5"));
        assertEquals(d("220.00"), totals.subtotal());
        assertEquals(d("200.00"), totals.taxableAmount());
        assertEquals(d("10.00"), totals.tax());
        assertEquals(d("210.00"), totals.total());
    }
    @Test void roundsEachLineAndTaxHalfUp() {
        var totals = CartCalculator.calculate(List.of(new CartCalculator.Line(d("0.05"), d("0.100"), d("0"))), d("0"), d("50"));
        assertEquals(d("0.01"), totals.subtotal());
        assertEquals(d("0.01"), totals.tax());
        assertEquals(d("0.02"), totals.total());
    }
    @Test void emptyCartHasZeroTotals() { assertEquals(d("0.00"), CartCalculator.calculate(List.of(), d("0"), d("0")).total()); }
    @Test void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> CartCalculator.calculate(List.of(new CartCalculator.Line(d("1"), d("0"), d("0"))), d("0"), d("0")));
        assertThrows(IllegalArgumentException.class, () -> CartCalculator.calculate(List.of(new CartCalculator.Line(d("1"), d("1"), d("2"))), d("0"), d("0")));
        assertThrows(IllegalArgumentException.class, () -> CartCalculator.calculate(List.of(new CartCalculator.Line(d("1"), d("1"), d("0"))), d("2"), d("0")));
        assertThrows(IllegalArgumentException.class, () -> CartCalculator.calculate(List.of(), d("0"), d("101")));
        assertThrows(IllegalArgumentException.class, () -> CartCalculator.calculate(List.of(new CartCalculator.Line(d("1.001"), d("1"), d("0"))), d("0"), d("0")));
        assertThrows(IllegalArgumentException.class, () -> CartCalculator.calculate(List.of(new CartCalculator.Line(d("1"), d("1.0001"), d("0"))), d("0"), d("0")));
    }
}
