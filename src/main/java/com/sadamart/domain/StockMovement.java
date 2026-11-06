package com.sadamart.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_movements")
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 160)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 32)
    private StockMovementType movementType;

    @Column(name = "signed_quantity", nullable = false, precision = 14, scale = 3)
    private BigDecimal signedQuantity;

    @Column(name = "unit_cost", nullable = false, precision = 14, scale = 4)
    private BigDecimal unitCost;

    @Column(name = "reference_code", nullable = false, length = 64)
    private String referenceCode;

    @Column(name = "actor_username", nullable = false, length = 64)
    private String actorUsername;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected StockMovement() { }

    public StockMovement(Long productId, String productName, StockMovementType movementType,
                         BigDecimal signedQuantity, BigDecimal unitCost,
                         String referenceCode, String actorUsername, String reason) {
        this.productId = productId;
        this.productName = productName;
        this.movementType = movementType;
        this.signedQuantity = signedQuantity;
        this.unitCost = unitCost;
        this.referenceCode = referenceCode;
        this.actorUsername = actorUsername;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public StockMovementType getMovementType() { return movementType; }
    public BigDecimal getSignedQuantity() { return signedQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public String getReferenceCode() { return referenceCode; }
    public String getActorUsername() { return actorUsername; }
    public String getReason() { return reason; }
    public Instant getCreatedAt() { return createdAt; }
}
