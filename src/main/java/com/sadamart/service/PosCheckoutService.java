package com.sadamart.service;

import com.sadamart.domain.*;
import com.sadamart.repository.*;
import com.sadamart.sales.CartCalculator;
import com.sadamart.sales.PaymentAllocation;
import com.sadamart.sales.PaymentAllocator;
import com.sadamart.sales.PaymentMethod;
import com.sadamart.security.SecurityContextSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PosCheckoutService {
    public record CheckoutItemRequest(String sku, BigDecimal quantity, BigDecimal lineDiscount) { }
    public record CheckoutResult(SaleTransaction transaction, PaymentAllocator.Result paymentResult) { }

    private final ProductRepository productRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final SaleTransactionRepository saleRepository;
    private final CashShiftRepository shiftRepository;
    private final AuditLogRepository auditRepository;
    private final SecurityContextSession session;

    public PosCheckoutService(ProductRepository productRepository,
                              InventoryBalanceRepository balanceRepository,
                              StockMovementRepository movementRepository,
                              SaleTransactionRepository saleRepository,
                              CashShiftRepository shiftRepository,
                              AuditLogRepository auditRepository,
                              SecurityContextSession session) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.balanceRepository = Objects.requireNonNull(balanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.saleRepository = Objects.requireNonNull(saleRepository);
        this.shiftRepository = Objects.requireNonNull(shiftRepository);
        this.auditRepository = Objects.requireNonNull(auditRepository);
        this.session = Objects.requireNonNull(session);
    }

    @Transactional
    public CheckoutResult completeAtomicSale(
            String idempotencyKey,
            String invoiceNumber,
            List<CheckoutItemRequest> items,
            BigDecimal orderDiscount,
            BigDecimal taxPercent,
            List<PaymentAllocation> payments
    ) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER, UserRole.CASHIER);
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(invoiceNumber, "invoiceNumber");
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Cannot complete a sale with an empty basket");
        }

        if (saleRepository.findByIdempotencyKey(idempotencyKey).isPresent()) {
            throw new IllegalStateException("Duplicate sale submission blocked by idempotency key: " + idempotencyKey);
        }

        Map<String, Product> productsBySku = new HashMap<>();
        for (CheckoutItemRequest req : items) {
            Product product = productRepository.findBySkuIgnoreCase(req.sku())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown product SKU: " + req.sku()));
            if (!product.isActive()) {
                throw new IllegalStateException("Product is inactive: " + product.getSku());
            }
            productsBySku.put(req.sku().toUpperCase(Locale.ROOT), product);
        }

        List<Long> sortedProductIds = productsBySku.values().stream()
                .map(Product::getId)
                .distinct()
                .sorted()
                .toList();

        Map<Long, InventoryBalance> lockedBalances = new HashMap<>();
        for (Long productId : sortedProductIds) {
            InventoryBalance bal = balanceRepository.findByProductIdForUpdate(productId)
                    .orElseThrow(() -> new IllegalStateException("Missing inventory balance for product ID " + productId));
            lockedBalances.put(productId, bal);
        }

        List<CartCalculator.Line> calcLines = new ArrayList<>();
        for (CheckoutItemRequest req : items) {
            Product product = productsBySku.get(req.sku().toUpperCase(Locale.ROOT));
            InventoryBalance balance = lockedBalances.get(product.getId());
            if (balance.getQuantityOnHand().compareTo(req.quantity()) < 0) {
                throw new IllegalStateException("Insufficient stock for " + product.getName()
                        + " (Available: " + balance.getQuantityOnHand() + ", Requested: " + req.quantity() + ")");
            }
            calcLines.add(new CartCalculator.Line(product.getSellingPrice(), req.quantity(), req.lineDiscount()));
        }

        CartCalculator.Totals totals = CartCalculator.calculate(calcLines, orderDiscount, taxPercent);
        PaymentAllocator.Result paymentResult = PaymentAllocator.allocate(totals.total(), payments);
        PaymentAllocator.requireFullyPaid(paymentResult);

        String paymentSummary = payments.stream()
                .map(p -> p.method().name())
                .distinct()
                .collect(Collectors.joining(" + "));

        SaleTransaction sale = new SaleTransaction(
                invoiceNumber, idempotencyKey, session.currentUsername(),
                totals.subtotal(), totals.lineDiscounts(), totals.orderDiscount(),
                totals.taxableAmount(), taxPercent, totals.tax(), totals.total(),
                paymentSummary
        );

        for (CheckoutItemRequest req : items) {
            Product product = productsBySku.get(req.sku().toUpperCase(Locale.ROOT));
            InventoryBalance balance = lockedBalances.get(product.getId());
            BigDecimal qty = req.quantity().setScale(3, RoundingMode.HALF_UP);
            BigDecimal lineGross = product.getSellingPrice().multiply(qty).setScale(2, RoundingMode.HALF_UP);
            BigDecimal lineNet = lineGross.subtract(req.lineDiscount().setScale(2, RoundingMode.HALF_UP));

            sale.addLine(new SaleLineItem(
                    product.getId(), product.getSku(), product.getName(),
                    qty, balance.getWeightedAvgCost(),
                    product.getSellingPrice(), req.lineDiscount().setScale(2, RoundingMode.HALF_UP), lineNet
            ));

            balance.setQuantityOnHand(balance.getQuantityOnHand().subtract(qty));
            balanceRepository.save(balance);

            movementRepository.save(new StockMovement(
                    product.getId(), product.getName(), StockMovementType.SALE,
                    qty.negate(), balance.getWeightedAvgCost(),
                    invoiceNumber, session.currentUsername(), "POS sale " + invoiceNumber
            ));
        }

        SaleTransaction savedSale = saleRepository.save(sale);

        BigDecimal cashNet = payments.stream()
                .filter(p -> p.method() == PaymentMethod.CASH)
                .map(PaymentAllocation::amount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add)
                .subtract(paymentResult.change())
                .max(new BigDecimal("0.00"));

        if (cashNet.signum() > 0) {
            shiftRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN").ifPresent(shift -> {
                shift.recordCashSale(cashNet);
                shiftRepository.save(shift);
            });
        }

        auditRepository.save(new AuditLogEntry(
                session.currentUsername(), session.currentRole().name(),
                "COMPLETE_SALE", "SaleTransaction", invoiceNumber,
                "Committed sale total $" + totals.total()
        ));

        return new CheckoutResult(savedSale, paymentResult);
    }
}
