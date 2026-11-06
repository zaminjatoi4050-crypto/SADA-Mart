package com.sadamart.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class WeightedAverageCostCalculator {
    private WeightedAverageCostCalculator() { }

    public static BigDecimal computeNewAverageCost(
            BigDecimal currentOnHandQty,
            BigDecimal currentUnitCost,
            BigDecimal receivedQty,
            BigDecimal receivedUnitCost
    ) {
        Objects.requireNonNull(currentOnHandQty, "currentOnHandQty");
        Objects.requireNonNull(currentUnitCost, "currentUnitCost");
        Objects.requireNonNull(receivedQty, "receivedQty");
        Objects.requireNonNull(receivedUnitCost, "receivedUnitCost");

        if (currentOnHandQty.signum() < 0) {
            throw new IllegalArgumentException("Current on-hand quantity cannot be negative");
        }
        if (currentUnitCost.signum() < 0 || receivedUnitCost.signum() < 0) {
            throw new IllegalArgumentException("Unit cost cannot be negative");
        }
        if (receivedQty.signum() <= 0) {
            throw new IllegalArgumentException("Received quantity must be positive");
        }
        if (currentOnHandQty.signum() == 0) {
            return receivedUnitCost.setScale(4, RoundingMode.HALF_UP);
        }
        BigDecimal currentValuation = currentOnHandQty.multiply(currentUnitCost);
        BigDecimal incomingValuation = receivedQty.multiply(receivedUnitCost);
        BigDecimal totalQty = currentOnHandQty.add(receivedQty);
        return currentValuation.add(incomingValuation)
                .divide(totalQty, 4, RoundingMode.HALF_UP);
    }
}
