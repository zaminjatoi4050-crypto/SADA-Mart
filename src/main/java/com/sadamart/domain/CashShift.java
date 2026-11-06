package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_shifts")
public class CashShift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shift_code", nullable = false, unique = true, length = 40)
    private String shiftCode;

    @Column(name = "cashier_username", nullable = false, length = 64)
    private String cashierUsername;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt = Instant.now();

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "opening_float", nullable = false, precision = 14, scale = 2)
    private BigDecimal openingFloat;

    @Column(name = "cash_sales", nullable = false, precision = 14, scale = 2)
    private BigDecimal cashSales = new BigDecimal("0.00");

    @Column(name = "cash_refunds", nullable = false, precision = 14, scale = 2)
    private BigDecimal cashRefunds = new BigDecimal("0.00");

    @Column(name = "cash_expenses", nullable = false, precision = 14, scale = 2)
    private BigDecimal cashExpenses = new BigDecimal("0.00");

    @Column(name = "expected_cash", nullable = false, precision = 14, scale = 2)
    private BigDecimal expectedCash;

    @Column(name = "actual_cash", precision = 14, scale = 2)
    private BigDecimal actualCash;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal variance = new BigDecimal("0.00");

    @Column(nullable = false, length = 24)
    private String status = "OPEN";

    protected CashShift() { }

    public CashShift(String shiftCode, String cashierUsername, BigDecimal openingFloat) {
        this.shiftCode = shiftCode;
        this.cashierUsername = cashierUsername;
        this.openingFloat = openingFloat;
        this.expectedCash = openingFloat;
    }

    public void recordCashSale(BigDecimal amount) {
        this.cashSales = this.cashSales.add(amount);
        recomputeExpected();
    }

    public void recordCashRefund(BigDecimal amount) {
        this.cashRefunds = this.cashRefunds.add(amount);
        recomputeExpected();
    }

    public void recordCashExpense(BigDecimal amount) {
        this.cashExpenses = this.cashExpenses.add(amount);
        recomputeExpected();
    }

    public void closeShift(BigDecimal countedCash) {
        if ("RECONCILED".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Closed shifts are immutable and cannot be modified");
        }
        recomputeExpected();
        this.actualCash = countedCash;
        this.variance = countedCash.subtract(this.expectedCash);
        this.closedAt = Instant.now();
        this.status = "RECONCILED";
    }

    private void recomputeExpected() {
        this.expectedCash = openingFloat.add(cashSales).subtract(cashRefunds).subtract(cashExpenses);
    }

    public Long getId() { return id; }
    public String getShiftCode() { return shiftCode; }
    public String getCashierUsername() { return cashierUsername; }
    public Instant getOpenedAt() { return openedAt; }
    public Instant getClosedAt() { return closedAt; }
    public BigDecimal getOpeningFloat() { return openingFloat; }
    public BigDecimal getExpectedCash() { return expectedCash; }
    public BigDecimal getActualCash() { return actualCash; }
    public BigDecimal getVariance() { return variance; }
    public String getStatus() { return status; }
}
