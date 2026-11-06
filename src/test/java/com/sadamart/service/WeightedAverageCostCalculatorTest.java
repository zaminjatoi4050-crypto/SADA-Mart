package com.sadamart.service;

import com.sadamart.domain.WeightedAverageCostCalculator;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightedAverageCostCalculatorTest {
    private static BigDecimal d(String v) { return new BigDecimal(v); }

    @Test
    void computesWeightedAverageOnReceivingNewStock() {
        BigDecimal wac = WeightedAverageCostCalculator.computeNewAverageCost(
                d("10.000"), d("10.0000"), d("10.000"), d("15.00"));
        assertEquals(d("12.5000"), wac);
    }

    @Test
    void adoptsIncomingUnitCostWhenExistingStockIsZero() {
        BigDecimal wac = WeightedAverageCostCalculator.computeNewAverageCost(
                d("0.000"), d("9.5000"), d("24.000"), d("11.25"));
        assertEquals(d("11.2500"), wac);
    }

    @Test
    void rejectsNonPositiveReceivedQuantityOrNegativeCosts() {
        assertThrows(IllegalArgumentException.class, () ->
                WeightedAverageCostCalculator.computeNewAverageCost(d("5"), d("10"), d("0"), d("10")));
        assertThrows(IllegalArgumentException.class, () ->
                WeightedAverageCostCalculator.computeNewAverageCost(d("5"), d("-1"), d("5"), d("10")));
    }
}
