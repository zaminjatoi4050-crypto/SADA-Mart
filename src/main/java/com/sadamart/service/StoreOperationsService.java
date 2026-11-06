package com.sadamart.service;

import com.sadamart.domain.*;
import com.sadamart.repository.*;
import com.sadamart.security.SecurityContextSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class StoreOperationsService {
    public record FinancialSummary(
            BigDecimal netSalesRevenue,
            BigDecimal totalRefunds,
            BigDecimal historicalCogs,
            BigDecimal grossProfit,
            BigDecimal operatingExpenses,
            BigDecimal netOperatingResult
    ) { }

    private final CustomerRepository customerRepository;
    private final ExpenseVoucherRepository expenseRepository;
    private final CashShiftRepository shiftRepository;
    private final SaleTransactionRepository saleRepository;
    private final SaleReturnRepository returnRepository;
    private final StoreSettingRepository settingRepository;
    private final SecurityContextSession session;

    public StoreOperationsService(CustomerRepository customerRepository,
                                  ExpenseVoucherRepository expenseRepository,
                                  CashShiftRepository shiftRepository,
                                  SaleTransactionRepository saleRepository,
                                  SaleReturnRepository returnRepository,
                                  StoreSettingRepository settingRepository,
                                  SecurityContextSession session) {
        this.customerRepository = Objects.requireNonNull(customerRepository);
        this.expenseRepository = Objects.requireNonNull(expenseRepository);
        this.shiftRepository = Objects.requireNonNull(shiftRepository);
        this.saleRepository = Objects.requireNonNull(saleRepository);
        this.returnRepository = Objects.requireNonNull(returnRepository);
        this.settingRepository = Objects.requireNonNull(settingRepository);
        this.session = Objects.requireNonNull(session);
    }

    @Transactional
    public Customer registerCustomer(String name, String phone, String email, String tier) {
        session.requireAuthenticated();
        return customerRepository.save(new Customer(name.trim(), phone.trim(), email, tier, new BigDecimal("0.00")));
    }

    @Transactional
    public ExpenseVoucher recordExpense(String voucherCode, String category, String description, String paymentMethod, BigDecimal amount) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);
        ExpenseVoucher saved = expenseRepository.save(new ExpenseVoucher(
                voucherCode, LocalDate.now(), category, description, paymentMethod, normalized, session.currentUsername()
        ));
        if ("CASH".equalsIgnoreCase(paymentMethod)) {
            shiftRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN").ifPresent(shift -> {
                shift.recordCashExpense(normalized);
                shiftRepository.save(shift);
            });
        }
        return saved;
    }

    @Transactional
    public CashShift openShift(String shiftCode, BigDecimal openingFloat) {
        session.requireAuthenticated();
        if (shiftRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN").isPresent()) {
            throw new IllegalStateException("An active cash shift is already open");
        }
        return shiftRepository.save(new CashShift(shiftCode, session.currentUsername(), openingFloat.setScale(2, RoundingMode.HALF_UP)));
    }

    @Transactional
    public CashShift closeActiveShift(BigDecimal countedDrawerCash) {
        session.requireRole(UserRole.ADMIN, UserRole.MANAGER);
        CashShift active = shiftRepository.findFirstByStatusOrderByOpenedAtDesc("OPEN")
                .orElseThrow(() -> new IllegalStateException("No open cash shift found"));
        active.closeShift(countedDrawerCash.setScale(2, RoundingMode.HALF_UP));
        return shiftRepository.save(active);
    }

    @Transactional(readOnly = true)
    public FinancialSummary calculateFinancialSummary() {
        session.requireAuthenticated();
        List<SaleTransaction> sales = saleRepository.findAll();
        List<SaleReturn> returns = returnRepository.findAll();
        List<ExpenseVoucher> expenses = expenseRepository.findAll();

        BigDecimal netRevenue = sales.stream()
                .map(SaleTransaction::getTaxableAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        BigDecimal grossCogs = sales.stream()
                .flatMap(s -> s.getLines().stream())
                .map(l -> l.getUnitCostAtSale().multiply(l.getQuantity()).setScale(2, RoundingMode.HALF_UP))
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        BigDecimal totalRefunds = returns.stream()
                .map(SaleReturn::getRefundAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        BigDecimal restockedCogs = returns.stream()
                .filter(SaleReturn::isRestocked)
                .map(r -> r.getOriginalUnitCost().multiply(r.getReturnedQty()).setScale(2, RoundingMode.HALF_UP))
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        BigDecimal effectiveNetSales = netRevenue.subtract(totalRefunds);
        BigDecimal effectiveCogs = grossCogs.subtract(restockedCogs);
        BigDecimal grossProfit = effectiveNetSales.subtract(effectiveCogs);

        BigDecimal totalExpenses = expenses.stream()
                .map(ExpenseVoucher::getAmount)
                .reduce(new BigDecimal("0.00"), BigDecimal::add);

        BigDecimal netOperatingResult = grossProfit.subtract(totalExpenses);

        return new FinancialSummary(effectiveNetSales, totalRefunds, effectiveCogs, grossProfit, totalExpenses, netOperatingResult);
    }
}
