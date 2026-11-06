package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "sale_returns")
public class SaleReturn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "return_number", nullable = false, unique = true, length = 40)
    private String returnNumber;

    @Column(name = "original_sale_id", nullable = false)
    private Long originalSaleId;

    @Column(name = "original_invoice", nullable = false, length = 40)
    private String originalInvoice;

    @Column(name = "product_sku", nullable = false, length = 40)
    private String productSku;

    @Column(name = "returned_qty", nullable = false, precision = 14, scale = 3)
    private BigDecimal returnedQty;

    @Column(name = "original_unit_cost", nullable = false, precision = 14, scale = 4)
    private BigDecimal originalUnitCost;

    @Column(name = "refund_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal refundAmount;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(nullable = false)
    private boolean restocked = true;

    @Column(name = "actor_username", nullable = false, length = 64)
    private String actorUsername;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected SaleReturn() { }

    public SaleReturn(String returnNumber, Long originalSaleId, String originalInvoice,
                      String productSku, BigDecimal returnedQty, BigDecimal originalUnitCost,
                      BigDecimal refundAmount, String reason, boolean restocked, String actorUsername) {
        this.returnNumber = returnNumber;
        this.originalSaleId = originalSaleId;
        this.originalInvoice = originalInvoice;
        this.productSku = productSku;
        this.returnedQty = returnedQty;
        this.originalUnitCost = originalUnitCost;
        this.refundAmount = refundAmount;
        this.reason = reason;
        this.restocked = restocked;
        this.actorUsername = actorUsername;
    }

    public Long getId() { return id; }
    public String getReturnNumber() { return returnNumber; }
    public Long getOriginalSaleId() { return originalSaleId; }
    public String getOriginalInvoice() { return originalInvoice; }
    public String getProductSku() { return productSku; }
    public BigDecimal getReturnedQty() { return returnedQty; }
    public BigDecimal getOriginalUnitCost() { return originalUnitCost; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public String getReason() { return reason; }
    public boolean isRestocked() { return restocked; }
    public String getActorUsername() { return actorUsername; }
    public Instant getCreatedAt() { return createdAt; }
}
