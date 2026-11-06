package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory_balances")
public class InventoryBalance {
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "quantity_on_hand", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityOnHand;

    @Column(name = "weighted_avg_cost", nullable = false, precision = 14, scale = 4)
    private BigDecimal weightedAvgCost;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    protected InventoryBalance() { }

    public InventoryBalance(Long productId, BigDecimal quantityOnHand, BigDecimal weightedAvgCost) {
        this.productId = productId;
        this.quantityOnHand = quantityOnHand;
        this.weightedAvgCost = weightedAvgCost;
    }

    public Long getProductId() { return productId; }
    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(BigDecimal quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
        this.updatedAt = Instant.now();
    }
    public BigDecimal getWeightedAvgCost() { return weightedAvgCost; }
    public void setWeightedAvgCost(BigDecimal weightedAvgCost) {
        this.weightedAvgCost = weightedAvgCost;
        this.updatedAt = Instant.now();
    }
    public Instant getUpdatedAt() { return updatedAt; }
}
