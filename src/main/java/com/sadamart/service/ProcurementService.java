package com.sadamart.service;

import com.sadamart.domain.*;
import com.sadamart.repository.*;
import com.sadamart.security.SecurityContextSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class ProcurementService {
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ProductRepository productRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final SecurityContextSession session;

    public ProcurementService(SupplierRepository supplierRepository,
                              PurchaseOrderRepository purchaseOrderRepository,
                              ProductRepository productRepository,
                              InventoryBalanceRepository balanceRepository,
                              StockMovementRepository movementRepository,
                              SecurityContextSession session) {
        this.supplierRepository = Objects.requireNonNull(supplierRepository);
        this.purchaseOrderRepository = Objects.requireNonNull(purchaseOrderRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.balanceRepository = Objects.requireNonNull(balanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.session = Objects.requireNonNull(session);
    }

    @Transactional
    public Supplier registerSupplier(String name, String contactPerson, String phone, String email) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        return supplierRepository.save(new Supplier(name.trim(), contactPerson.trim(), phone.trim(), email, new BigDecimal("0.00")));
    }

    @Transactional
    public PurchaseOrder createPurchaseOrder(String poNumber, Long supplierId, BigDecimal totalAmount) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown supplier ID: " + supplierId));
        BigDecimal normalized = totalAmount.setScale(2, RoundingMode.HALF_UP);
        return purchaseOrderRepository.save(new PurchaseOrder(
                poNumber.trim(), supplier.getId(), supplier.getName(),
                LocalDate.now(), "OPEN", normalized, normalized, session.currentUsername()
        ));
    }

    @Transactional
    public PurchaseOrder receivePurchaseOrder(String poNumber, String productSku, BigDecimal receivedQty, BigDecimal unitCost) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        PurchaseOrder po = purchaseOrderRepository.findByPoNumberIgnoreCase(poNumber)
                .orElseThrow(() -> new IllegalArgumentException("Unknown purchase order: " + poNumber));
        if ("RECEIVED".equalsIgnoreCase(po.getStatus())) {
            throw new IllegalStateException("Purchase order " + poNumber + " has already been received");
        }

        Product product = productRepository.findBySkuIgnoreCase(productSku)
                .orElseThrow(() -> new IllegalArgumentException("Unknown SKU: " + productSku));
        InventoryBalance balance = balanceRepository.findByProductIdForUpdate(product.getId())
                .orElseThrow(() -> new IllegalStateException("Missing inventory balance for " + productSku));

        BigDecimal newAvgCost = WeightedAverageCostCalculator.computeNewAverageCost(
                balance.getQuantityOnHand(),
                balance.getWeightedAvgCost(),
                receivedQty,
                unitCost
        );
        BigDecimal newQty = balance.getQuantityOnHand().add(receivedQty.setScale(3, RoundingMode.HALF_UP));

        balance.setWeightedAvgCost(newAvgCost);
        balance.setQuantityOnHand(newQty);
        balanceRepository.save(balance);

        product.setCostPrice(newAvgCost.setScale(2, RoundingMode.HALF_UP));
        productRepository.save(product);

        movementRepository.save(new StockMovement(
                product.getId(), product.getName(), StockMovementType.PO_RECEIPT,
                receivedQty.setScale(3, RoundingMode.HALF_UP), newAvgCost,
                po.getPoNumber(), session.currentUsername(), "Goods received against " + po.getPoNumber()
        ));

        Supplier supplier = supplierRepository.findById(po.getSupplierId())
                .orElseThrow(() -> new IllegalStateException("Missing supplier for PO"));
        supplier.setPayableBalance(supplier.getPayableBalance().add(po.getTotalAmount()));
        supplierRepository.save(supplier);

        po.setStatus("RECEIVED");
        return purchaseOrderRepository.save(po);
    }
}
