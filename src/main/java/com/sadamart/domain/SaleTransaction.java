package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales")
public class SaleTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 80)
    private String idempotencyKey;

    @Column(name = "cashier_username", nullable = false, length = 64)
    private String cashierUsername;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "line_discounts", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineDiscounts;

    @Column(name = "order_discount", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderDiscount;

    @Column(name = "taxable_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxableAmount;

    @Column(name = "tax_percent", nullable = false, precision = 7, scale = 4)
    private BigDecimal taxPercent;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_summary", nullable = false, length = 64)
    private String paymentSummary;

    @Column(nullable = false, length = 24)
    private String status = "COMPLETED";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<SaleLineItem> lines = new ArrayList<>();

    protected SaleTransaction() { }

    public SaleTransaction(String invoiceNumber, String idempotencyKey, String cashierUsername,
                           BigDecimal subtotal, BigDecimal lineDiscounts, BigDecimal orderDiscount,
                           BigDecimal taxableAmount, BigDecimal taxPercent, BigDecimal taxAmount,
                           BigDecimal totalAmount, String paymentSummary) {
        this.invoiceNumber = invoiceNumber;
        this.idempotencyKey = idempotencyKey;
        this.cashierUsername = cashierUsername;
        this.subtotal = subtotal;
        this.lineDiscounts = lineDiscounts;
        this.orderDiscount = orderDiscount;
        this.taxableAmount = taxableAmount;
        this.taxPercent = taxPercent;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.paymentSummary = paymentSummary;
    }

    public void addLine(SaleLineItem line) {
        line.setSale(this);
        this.lines.add(line);
    }

    public Long getId() { return id; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getCashierUsername() { return cashierUsername; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getLineDiscounts() { return lineDiscounts; }
    public BigDecimal getOrderDiscount() { return orderDiscount; }
    public BigDecimal getTaxableAmount() { return taxableAmount; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public BigDecimal getTaxAmount() { return taxAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getPaymentSummary() { return paymentSummary; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public List<SaleLineItem> getLines() { return lines; }
}
