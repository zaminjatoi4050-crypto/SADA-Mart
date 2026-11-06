package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_lines")
public class SaleLineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleTransaction sale;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_sku", nullable = false, length = 40)
    private String productSku;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Column(name = "returned_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal returnedQuantity = new BigDecimal("0.000");

    @Column(name = "unit_cost_at_sale", nullable = false, precision = 14, scale = 4)
    private BigDecimal unitCostAtSale;

    @Column(name = "unit_price", nullable = false, precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_discount", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineDiscount;

    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;

    protected SaleLineItem() { }

    public SaleLineItem(Long productId, String productSku, String productName,
                        BigDecimal quantity, BigDecimal unitCostAtSale,
                        BigDecimal unitPrice, BigDecimal lineDiscount, BigDecimal lineTotal) {
        this.productId = productId;
        this.productSku = productSku;
        this.productName = productName;
        this.quantity = quantity;
        this.unitCostAtSale = unitCostAtSale;
        this.unitPrice = unitPrice;
        this.lineDiscount = lineDiscount;
        this.lineTotal = lineTotal;
    }

    public void setSale(SaleTransaction sale) { this.sale = sale; }
    public Long getProductId() { return productId; }
    public String getProductSku() { return productSku; }
    public String getProductName() { return productName; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getReturnedQuantity() { return returnedQuantity; }
    public void addReturnedQuantity(BigDecimal qty) {
        BigDecimal updated = this.returnedQuantity.add(qty);
        if (updated.compareTo(this.quantity) > 0) {
            throw new IllegalArgumentException("Cumulative returned quantity exceeds sold quantity for " + productSku);
        }
        this.returnedQuantity = updated;
    }
    public BigDecimal getUnitCostAtSale() { return unitCostAtSale; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getLineDiscount() { return lineDiscount; }
    public BigDecimal getLineTotal() { return lineTotal; }
}
