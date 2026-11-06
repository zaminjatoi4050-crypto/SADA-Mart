package com.sadamart.sales;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/** Pure calculation only. It does not authorize, persist, accept payments or complete a sale. */
public final class CartCalculator {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private CartCalculator() { }

    public record Line(BigDecimal unitPrice, BigDecimal quantity, BigDecimal discount) { }
    public record Totals(BigDecimal subtotal, BigDecimal lineDiscounts, BigDecimal orderDiscount,
                         BigDecimal taxableAmount, BigDecimal tax, BigDecimal total) { }

    /** Prices and discounts are tax-exclusive; tax is rounded once at order level, HALF_UP. */
    public static Totals calculate(List<Line> lines, BigDecimal orderDiscount, BigDecimal taxPercent) {
        Objects.requireNonNull(lines, "lines");
        BigDecimal subtotal = ZERO;
        BigDecimal discounts = ZERO;
        for (Line line : lines) {
            Objects.requireNonNull(line, "line");
            BigDecimal price = money(line.unitPrice(), "unit price");
            BigDecimal quantity = Objects.requireNonNull(line.quantity(), "quantity");
            if (quantity.signum() <= 0 || quantity.scale() > 3) throw new IllegalArgumentException("Quantity must be positive with at most three decimal places");
            BigDecimal gross = price.multiply(quantity).setScale(2, RoundingMode.HALF_UP);
            BigDecimal discount = money(line.discount(), "line discount");
            if (discount.compareTo(gross) > 0) throw new IllegalArgumentException("Line discount exceeds line total");
            subtotal = subtotal.add(gross);
            discounts = discounts.add(discount);
        }
        BigDecimal order = money(orderDiscount, "order discount");
        BigDecimal taxable = subtotal.subtract(discounts).subtract(order);
        if (taxable.signum() < 0) throw new IllegalArgumentException("Order discount exceeds discounted subtotal");
        BigDecimal rate = Objects.requireNonNull(taxPercent, "tax percent");
        if (rate.signum() < 0 || rate.compareTo(HUNDRED) > 0 || rate.scale() > 4) throw new IllegalArgumentException("Tax percent must be between 0 and 100, at most four decimal places");
        BigDecimal tax = taxable.multiply(rate).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return new Totals(subtotal, discounts, order, taxable, tax, taxable.add(tax));
    }
    private static BigDecimal money(BigDecimal value, String name) {
        Objects.requireNonNull(value, name);
        if (value.signum() < 0 || value.scale() > 2) throw new IllegalArgumentException(name + " must be non-negative with at most two decimal places");
        return value.setScale(2);
    }
}
