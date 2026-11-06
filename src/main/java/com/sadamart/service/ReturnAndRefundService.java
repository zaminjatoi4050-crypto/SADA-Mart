package com.sadamart.service;

import com.sadamart.domain.*;
import com.sadamart.repository.*;
import com.sadamart.security.SecurityContextSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
public class ReturnAndRefundService {
    private final SaleTransactionRepository saleRepository;
    private final SaleReturnRepository returnRepository;
    private final InventoryBalanceRepository balanceRepository;
    private final StockMovementRepository movementRepository;
    private final CashShiftRepository shiftRepository;
    private final AuditLogRepository auditRepository;
    private final SecurityContextSession session;

    public ReturnAndRefundService(SaleTransactionRepository saleRepository,
                                  SaleReturnRepository returnRepository,
                                  InventoryBalanceRepository balanceRepository,
                                  StockMovementRepository movementRepository,
                                  CashShiftRepository shiftRepository,
                                  AuditLogRepository auditRepository,
                                  SecurityContextSession session) {
        this.saleRepository = Objects.requireNonNull(saleRepository);
        this.returnRepository = Objects.requireNonNull(returnRepository);
        this.balanceRepository = Objects.requireNonNull(balanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.shiftRepository = Objects.requireNonNull(shiftRepository);
        this.auditRepository = Objects.requireNonNull(auditRepository);
        this.session = Objects.requireNonNull(session);
    }

    @Transactional
    public SaleReturn processAuthorizedReturn(
            String returnNumber,
            String originalInvoiceNumber,
            String productSku,
            BigDecimal returnQuantity,
            String reason,
            boolean restockInventory
    ) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        if (returnQuantity == null || returnQuantity.signum() <= 0) {
            throw new IllegalArgumentException("Return quantity must be positive");
        }

        SaleTransaction originalSale = saleRepository.findByInvoiceNumberIgnoreCase(originalInvoiceNumber)
                .orElseThrow(() -> new IllegalArgumentException("Original sale invoice not found: " + originalInvoiceNumber));

        SaleLineItem matchingLine = originalSale.getLines().stream()
                .filter(l -> l.getProductSku().equalsIgnoreCase(productSku))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("SKU " + productSku + " was not part of invoice " + originalInvoiceNumber));

        BigDecimal normalizedQty = returnQuantity.setScale(3, RoundingMode.HALF_UP);
        matchingLine.addReturnedQuantity(normalizedQty);
        saleRepository.save(originalSale);

        BigDecimal effectiveUnitNet = matchingLine.getLineTotal()
                .divide(matchingLine.getQuantity(), 2, RoundingMode.HALF_UP);
        BigDecimal refundAmount = effectiveUnitNet.multiply(normalizedQty).setScale(2, RoundingMode.HALF_UP);

        if (restockInventory) {
            InventoryBalance balance = balanceRepository.findByProductIdForUpdate(matchingLine.getProductId())
                    .orElseThrow(() -> new IllegalStateException("Missing inventory balance for " + productSku));
            balance.setQuantityOnHand(balance.getQuantityOnHand().add(normalizedQty));
            balanceRepository.save(balance);

            movementRepository.save(new StockMovement(
                    matchingLine.getProductId(), matchingLine.getProductName(),
                    StockMovementType.RETURN, normalizedQty,
                    matchingLine.getUnitCostAtSale(), returnNumber,
                    session.currentUsername(), reason
            ));
        }

        shiftRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN").ifPresent(shift -> {
            shift.recordCashRefund(refundAmount);
            shiftRepository.save(shift);
        });

        SaleReturn savedReturn = returnRepository.save(new SaleReturn(
                returnNumber, originalSale.getId(), originalSale.getInvoiceNumber(),
                matchingLine.getProductSku(), normalizedQty,
                matchingLine.getUnitCostAtSale(), refundAmount,
                reason, restockInventory, session.currentUsername()
        ));

        auditRepository.save(new AuditLogEntry(
                session.currentUsername(), session.currentRole().name(),
                "PROCESS_RETURN", "SaleReturn", returnNumber,
                "Refunded $" + refundAmount + " against " + originalInvoiceNumber
        ));

        return savedReturn;
    }
}
