package com.sadamart.service;

import com.sadamart.domain.*;
import com.sadamart.repository.*;
import com.sadamart.security.SecurityContextSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
public class CatalogAndInventoryService {
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final AuditLogRepository auditRepository;
    private final SecurityContextSession session;

    public CatalogAndInventoryService(ProductRepository productRepository,
                                      InventoryBalanceRepository balanceRepository,
                                      StockMovementRepository movementRepository,
                                      AuditLogRepository auditRepository,
                                      SecurityContextSession session) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.balanceRepository = Objects.requireNonNull(balanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.auditRepository = Objects.requireNonNull(auditRepository);
        this.session = Objects.requireNonNull(session);
    }

    @Transactional
    public Product createProduct(String sku, String barcode, String name, String category, String brand,
                                 BigDecimal costPrice, BigDecimal sellingPrice, int reorderPoint, BigDecimal initialStock) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        if (productRepository.findBySkuIgnoreCase(sku).isPresent()) {
            throw new IllegalArgumentException("Duplicate SKU: " + sku);
        }
        if (productRepository.findByBarcode(barcode).isPresent()) {
            throw new IllegalArgumentException("Duplicate barcode: " + barcode);
        }
        Product product = productRepository.save(new Product(
                sku.trim(), barcode.trim(), name.trim(), category.trim(), brand.trim(),
                costPrice.setScale(2, RoundingMode.HALF_UP),
                sellingPrice.setScale(2, RoundingMode.HALF_UP),
                reorderPoint
        ));

        BigDecimal openingQty = initialStock == null ? BigDecimal.ZERO : initialStock.setScale(3, RoundingMode.HALF_UP);
        BigDecimal wac = costPrice.setScale(4, RoundingMode.HALF_UP);
        balanceRepository.save(new InventoryBalance(product.getId(), openingQty, wac));

        if (openingQty.signum() > 0) {
            movementRepository.save(new StockMovement(
                    product.getId(), product.getName(), StockMovementType.COUNT_RECONCILE,
                    openingQty, wac, "INIT-" + product.getSku(), session.currentUsername(),
                    "Opening stock on product creation"
            ));
        }

        auditRepository.save(new AuditLogEntry(
                session.currentUsername(), session.currentRole().name(),
                "CREATE_PRODUCT", "Product", product.getSku(),
                "Created product " + product.getName() + " with opening stock " + openingQty
        ));
        return product;
    }

    @Transactional
    public InventoryBalance recordStockAdjustment(String sku, BigDecimal signedQtyDelta, StockMovementType type, String reason) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        if (signedQtyDelta == null || signedQtyDelta.signum() == 0) {
            throw new IllegalArgumentException("Stock adjustment quantity must be non-zero");
        }
        Product product = productRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SKU: " + sku));
        InventoryBalance balance = balanceRepository.findByProductIdForUpdate(product.getId())
                .orElseThrow(() -> new IllegalStateException("Missing inventory balance for " + sku));

        BigDecimal updatedQty = balance.getQuantityOnHand().add(signedQtyDelta);
        if (updatedQty.signum() < 0) {
            throw new IllegalArgumentException("Insufficient stock for adjustment on " + sku);
        }
        balance.setQuantityOnHand(updatedQty);
        balanceRepository.save(balance);

        String ref = "ADJ-" + System.currentTimeMillis() % 100000;
        movementRepository.save(new StockMovement(
                product.getId(), product.getName(), type,
                signedQtyDelta.setScale(3, RoundingMode.HALF_UP),
                balance.getWeightedAvgCost(), ref, session.currentUsername(), reason
        ));
        return balance;
    }

    @Transactional(readOnly = true)
    public List<Product> listProducts() {
        session.requireAuthenticated();
        return productRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<StockMovement> recentMovements() {
        session.requireAuthenticated();
        return movementRepository.findTop200ByOrderByCreatedAtDesc();
    }
}
