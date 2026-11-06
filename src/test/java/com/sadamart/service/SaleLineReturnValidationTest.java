package com.sadamart.service;

import com.sadamart.domain.SaleLineItem;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SaleLineReturnValidationTest {
    private static BigDecimal d(String v) { return new BigDecimal(v); }

    @Test
    void enforcesCumulativeReturnLimitAndPreservesHistoricalCost() {
        SaleLineItem line = new SaleLineItem(
                1L, "SKU-1001", "Royal Basmati Rice 5kg",
                d("3.000"), d("14.2000"), d("18.50"), d("0.00"), d("55.50")
        );

        line.addReturnedQuantity(d("1.000"));
        line.addReturnedQuantity(d("2.000"));
        assertEquals(d("3.000"), line.getReturnedQuantity());
        assertEquals(d("14.2000"), line.getUnitCostAtSale());
        assertThrows(IllegalArgumentException.class, () -> line.addReturnedQuantity(d("0.001")));
    }
}
