package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "suppliers")
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 160)
    private String name;

    @Column(name = "contact_person", nullable = false, length = 120)
    private String contactPerson;

    @Column(nullable = false, length = 40)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(name = "payable_balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal payableBalance = new BigDecimal("0.00");

    @Column(nullable = false)
    private boolean active = true;

    protected Supplier() { }

    public Supplier(String name, String contactPerson, String phone, String email, BigDecimal payableBalance) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.payableBalance = payableBalance;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public BigDecimal getPayableBalance() { return payableBalance; }
    public void setPayableBalance(BigDecimal payableBalance) { this.payableBalance = payableBalance; }
    public boolean isActive() { return active; }
}
