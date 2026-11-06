package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class ExpenseVoucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_code", nullable = false, unique = true, length = 40)
    private String voucherCode;

    @Column(name = "expense_date", nullable = false)
    private LocalDate expenseDate;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(name = "payment_method", nullable = false, length = 32)
    private String paymentMethod;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 24)
    private String status = "Approved";

    @Column(name = "approved_by", nullable = false, length = 64)
    private String approvedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected ExpenseVoucher() { }

    public ExpenseVoucher(String voucherCode, LocalDate expenseDate, String category,
                          String description, String paymentMethod, BigDecimal amount, String approvedBy) {
        this.voucherCode = voucherCode;
        this.expenseDate = expenseDate;
        this.category = category;
        this.description = description;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.approvedBy = approvedBy;
    }

    public Long getId() { return id; }
    public String getVoucherCode() { return voucherCode; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getPaymentMethod() { return paymentMethod; }
    public BigDecimal getAmount() { return amount; }
    public String getStatus() { return status; }
    public String getApprovedBy() { return approvedBy; }
}
