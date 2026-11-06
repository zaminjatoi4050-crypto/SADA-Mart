package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(name = "loyalty_tier", nullable = false, length = 32)
    private String loyaltyTier = "Standard";

    @Column(name = "credit_limit", nullable = false, precision = 14, scale = 2)
    private BigDecimal creditLimit = new BigDecimal("0.00");

    @Column(name = "receivable_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal receivableBalance = new BigDecimal("0.00");

    @Column(name = "last_purchase_date")
    private LocalDate lastPurchaseDate;

    @Column(nullable = false)
    private boolean active = true;

    protected Customer() { }

    public Customer(String name, String phone, String email, String loyaltyTier, BigDecimal receivableBalance) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.loyaltyTier = loyaltyTier;
        this.receivableBalance = receivableBalance;
        this.lastPurchaseDate = LocalDate.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getLoyaltyTier() { return loyaltyTier; }
    public BigDecimal getCreditLimit() { return creditLimit; }
    public BigDecimal getReceivableBalance() { return receivableBalance; }
    public void setReceivableBalance(BigDecimal receivableBalance) { this.receivableBalance = receivableBalance; }
    public LocalDate getLastPurchaseDate() { return lastPurchaseDate; }
}
