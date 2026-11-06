package com.sadamart.ui;

import com.sadamart.sales.CartCalculator;
import com.sadamart.sales.PaymentAllocation;
import com.sadamart.sales.PaymentAllocator;
import com.sadamart.domain.UserRole;
import com.sadamart.domain.WeightedAverageCostCalculator;
import com.sadamart.sales.PaymentMethod;
import com.sadamart.security.SecurityContextSession;
import com.sadamart.service.InventoryStack;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reactive in-memory store for the SADA Mart Enterprise UI.
 * Connects the JavaFX views to the verified CartCalculator and PaymentAllocator
 * domain services while keeping Oracle persistence boundaries clean.
 */
public final class WorkspaceDataStore {
    private static final WorkspaceDataStore INSTANCE = new WorkspaceDataStore();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String currentOperator = "Ayesha Siddiqui";
    private String currentRole = "ADMIN";
    private final SecurityContextSession securitySession = new SecurityContextSession();
    private boolean oracleConnected = false;
    private String oracleEndpoint = "localhost:1521/FREEPDB1";
    private BigDecimal taxPercent = new BigDecimal("5.00");
    private BigDecimal orderDiscount = new BigDecimal("0.00");

    public record ProductItem(String sku, String barcode, String name, String category,
                              String brand, BigDecimal costPrice, BigDecimal sellingPrice,
                              int stock, int reorderPoint, String status) { }

    public record BasketItem(ProductItem product, BigDecimal quantity, BigDecimal lineDiscount) {
        public BigDecimal lineGross() {
            return product.sellingPrice().multiply(quantity).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        public BigDecimal lineNet() {
            return lineGross().subtract(lineDiscount).max(new BigDecimal("0.00"));
        }
    }

    private final ObservableList<ProductItem> products = FXCollections.observableArrayList();
    private final InventoryStack<ProductItem> inventoryStack = new InventoryStack<>();
    private final ObservableList<BasketItem> basket = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> transactions = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> movements = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> purchases = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> suppliers = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> customers = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> returnsList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> expenses = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> shifts = FXCollections.observableArrayList();
    private final ObservableList<Map<String, String>> users = FXCollections.observableArrayList();

    private WorkspaceDataStore() {
        securitySession.establishSession("ayesha.admin", "Ayesha Siddiqui", UserRole.ADMIN);
        seedInitialCatalog();
    }

    public static WorkspaceDataStore get() {
        return INSTANCE;
    }

    private void seedInitialCatalog() {
        List<ProductItem> initialProducts = List.of(
            new ProductItem("SKU-1001", "8961001001", "Royal Basmati Rice 5kg", "Staples & Grains", "Guard", new BigDecimal("14.20"), new BigDecimal("18.50"), 64, 15, "Active"),
            new ProductItem("SKU-1002", "8961001002", "Organic Olive Oil 1L", "Cooking Oils", "Sada Pure", new BigDecimal("9.80"), new BigDecimal("13.00"), 28, 10, "Active"),
            new ProductItem("SKU-1003", "8961001003", "Artisan Arabica Coffee 500g", "Beverages", "RoastLab", new BigDecimal("7.50"), new BigDecimal("11.25"), 9, 12, "Low Stock"),
            new ProductItem("SKU-1004", "8961001004", "Full Cream Milk Pack (12x1L)", "Dairy & Chilled", "Olpers", new BigDecimal("11.00"), new BigDecimal("14.40"), 42, 20, "Active"),
            new ProductItem("SKU-1005", "8961001005", "Himalayan Pink Salt 1kg", "Spices & Condiments", "Sada Pure", new BigDecimal("1.10"), new BigDecimal("2.20"), 110, 25, "Active"),
            new ProductItem("SKU-1006", "8961001006", "Premium Green Tea 100 Bags", "Beverages", "Tapal", new BigDecimal("3.20"), new BigDecimal("4.95"), 6, 15, "Low Stock"),
            new ProductItem("SKU-1007", "8961001007", "Whole Wheat Chakki Atta 10kg", "Staples & Grains", "Sunridge", new BigDecimal("10.50"), new BigDecimal("13.80"), 38, 12, "Active"),
            new ProductItem("SKU-1008", "8961001008", "Antibacterial Hand Wash 500ml", "Personal Care", "Safeguard", new BigDecimal("2.10"), new BigDecimal("3.40"), 0, 10, "Out of Stock")
        );
        for (int i = initialProducts.size() - 1; i >= 0; i--) {
            inventoryStack.push(initialProducts.get(i));
        }
        refreshProductsFromStack();

        basket.add(new BasketItem(products.get(0), new BigDecimal("2"), new BigDecimal("1.00")));
        basket.add(new BasketItem(products.get(2), new BigDecimal("1"), new BigDecimal("0.00")));

        transactions.addAll(
            row("Transaction", "INV-2026-8841", "Date & time", "2026-11-05 20:45", "Cashier", "Ayesha S.", "Payment", "CASH + CARD", "Total", "$64.85", "Status", "Completed"),
            row("Transaction", "INV-2026-8840", "Date & time", "2026-11-05 19:52", "Cashier", "Bilal K.", "Payment", "CASH", "Total", "$28.80", "Status", "Completed"),
            row("Transaction", "INV-2026-8839", "Date & time", "2026-11-05 18:30", "Cashier", "Ayesha S.", "Payment", "MOBILE_WALLET", "Total", "$41.20", "Status", "Completed"),
            row("Transaction", "INV-2026-8838", "Date & time", "2026-11-05 17:14", "Cashier", "Zainab M.", "Payment", "CARD", "Total", "$119.50", "Status", "Completed")
        );

        movements.addAll(
            row("Date", "2026-11-05 20:45", "Product", "Royal Basmati Rice 5kg", "Type", "SALE", "Quantity", "-2", "Reference", "INV-2026-8841", "User", "Ayesha S."),
            row("Date", "2026-11-05 16:10", "Product", "Organic Olive Oil 1L", "Type", "PO_RECEIPT", "Quantity", "+24", "Reference", "PO-2026-309", "User", "Bilal K."),
            row("Date", "2026-11-05 14:00", "Product", "Artisan Arabica Coffee 500g", "Type", "ADJUSTMENT", "Quantity", "-1", "Reference", "ADJ-104", "User", "Ayesha S.")
        );

        purchases.addAll(
            row("Purchase", "PO-2026-310", "Supplier", "Indus Foods Distribution", "Date", "2026-11-05", "Receiving", "Pending (0%)", "Total", "$1,420.00", "Balance", "$1,420.00", "Status", "Open"),
            row("Purchase", "PO-2026-309", "Supplier", "Prime Agri Commodities", "Date", "2026-11-04", "Receiving", "Received (100%)", "Total", "$2,850.00", "Balance", "$450.00", "Status", "Received"),
            row("Purchase", "PO-2026-308", "Supplier", "Metro FMCG Wholesale", "Date", "2026-11-02", "Receiving", "Partial (65%)", "Total", "$980.00", "Balance", "$340.00", "Status", "Partial")
        );

        suppliers.addAll(
            row("Supplier", "Indus Foods Distribution", "Contact", "Kamran Qureshi", "Phone", "+92 300 8214490", "Email", "orders@indusfoods.pk", "Outstanding", "$1,420.00", "Status", "Active"),
            row("Supplier", "Prime Agri Commodities", "Contact", "Tariq Mehmood", "Phone", "+92 321 4491023", "Email", "supply@primeagri.pk", "Outstanding", "$450.00", "Status", "Active"),
            row("Supplier", "Metro FMCG Wholesale", "Contact", "Sanaullah Soomro", "Phone", "+92 333 7129980", "Email", "b2b@metrofmcg.pk", "Outstanding", "$340.00", "Status", "Active")
        );

        customers.addAll(
            row("Customer", "Dr. Farhan Memon", "Phone", "+92 301 3882910", "Email", "farhan.memon@clinic.pk", "Last purchase", "2026-11-05", "Balance", "$0.00", "Status", "VIP Tier"),
            row("Customer", "Nadia Bhutto", "Phone", "+92 334 9102831", "Email", "nadia.b@gmail.com", "Last purchase", "2026-11-04", "Balance", "$45.00", "Status", "Credit Active"),
            row("Customer", "Walk-in Retail Account", "Phone", "—", "Email", "pos@sadamart.local", "Last purchase", "2026-11-05", "Balance", "$0.00", "Status", "Standard")
        );

        returnsList.addAll(
            row("Return", "RET-2026-041", "Original sale", "INV-2026-8812", "Date", "2026-11-04 15:22", "Reason", "Damaged outer seal", "Refund", "$13.00", "Status", "Restocked & Refunded"),
            row("Return", "RET-2026-040", "Original sale", "INV-2026-8790", "Date", "2026-11-02 11:08", "Reason", "Customer exchanged size", "Refund", "$18.50", "Status", "Completed")
        );

        expenses.addAll(
            row("Date", "2026-11-05", "Category", "Cold Chain & Utilities", "Description", "Store chiller & HVAC power bill", "Payment", "BANK_TRANSFER", "Amount", "$310.00", "Status", "Approved"),
            row("Date", "2026-11-03", "Category", "Store Operations", "Description", "Thermal receipt paper rolls (50x)", "Payment", "CASH", "Amount", "$65.00", "Status", "Approved"),
            row("Date", "2026-11-01", "Category", "Logistics", "Description", "Local warehouse inbound freight", "Payment", "CASH", "Amount", "$120.00", "Status", "Approved")
        );

        shifts.addAll(
            row("Shift", "SHF-2026-112", "Cashier", "Ayesha Siddiqui", "Opened", "2026-11-05 09:00", "Closed", "Open (Active)", "Expected", "$1,485.40", "Actual", "—", "Variance", "$0.00", "Status", "OPEN"),
            row("Shift", "SHF-2026-111", "Cashier", "Bilal Khan", "Opened", "2026-11-04 09:00", "Closed", "2026-11-04 22:00", "Expected", "$2,190.00", "Actual", "$2,190.00", "Variance", "$0.00", "Status", "RECONCILED")
        );

        users.addAll(
            row("Name", "Ayesha Siddiqui", "Username", "ayesha.admin", "Role", "ADMIN", "Last sign-in", "2026-11-05 21:15", "Status", "Active"),
            row("Name", "Bilal Khan", "Username", "bilal.mgr", "Role", "MANAGER", "Last sign-in", "2026-11-05 18:40", "Status", "Active"),
            row("Name", "Zainab Mangi", "Username", "zainab.pos", "Role", "CASHIER", "Last sign-in", "2026-11-05 17:14", "Status", "Active")
        );
    }

    public static Map<String, String> row(String... kv) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            map.put(kv[i], kv[i + 1]);
        }
        return map;
    }

    private void refreshProductsFromStack() {
        products.setAll(inventoryStack.snapshot());
    }

    public void pushProduct(ProductItem product) {
        inventoryStack.push(product);
        refreshProductsFromStack();
    }

    public ProductItem popProduct() {
        ProductItem removed = inventoryStack.pop();
        refreshProductsFromStack();
        return removed;
    }

    public ProductItem peekProduct() {
        return inventoryStack.peek();
    }

    public boolean updateProduct(String sku, ProductItem replacement) {
        boolean updated = inventoryStack.replaceIf(p -> p.sku().equalsIgnoreCase(sku), replacement);
        if (updated) {
            refreshProductsFromStack();
        }
        return updated;
    }

    public boolean deleteProduct(String sku) {
        boolean deleted = inventoryStack.removeIf(p -> p.sku().equalsIgnoreCase(sku));
        if (deleted) {
            refreshProductsFromStack();
        }
        return deleted;
    }

    public int inventoryStackSize() {
        return inventoryStack.size();
    }

    public CartCalculator.Totals calculateCurrentBasketTotals() {
        List<CartCalculator.Line> lines = new ArrayList<>();
        for (BasketItem item : basket) {
            lines.add(new CartCalculator.Line(item.product().sellingPrice(), item.quantity(), item.lineDiscount()));
        }
        BigDecimal safeOrderDiscount = orderDiscount;
        BigDecimal grossMinusLine = lines.stream()
                .map(l -> l.unitPrice().multiply(l.quantity()).setScale(2, java.math.RoundingMode.HALF_UP).subtract(l.discount()))
                .reduce(new BigDecimal("0.00"), BigDecimal::add);
        if (safeOrderDiscount.compareTo(grossMinusLine) > 0) {
            safeOrderDiscount = grossMinusLine;
        }
        return CartCalculator.calculate(lines, safeOrderDiscount, taxPercent);
    }

    public void addToBasket(ProductItem product) {
        for (int i = 0; i < basket.size(); i++) {
            BasketItem existing = basket.get(i);
            if (existing.product().sku().equals(product.sku())) {
                basket.set(i, new BasketItem(product, existing.quantity().add(BigDecimal.ONE), existing.lineDiscount()));
                return;
            }
        }
        basket.add(new BasketItem(product, BigDecimal.ONE, new BigDecimal("0.00")));
    }

    public PaymentAllocator.Result completeCheckout(PaymentMethod method, BigDecimal tenderedAmount) {
        CartCalculator.Totals totals = calculateCurrentBasketTotals();
        PaymentAllocator.Result result = PaymentAllocator.allocate(
                totals.total(),
                List.of(new PaymentAllocation(method, tenderedAmount))
        );
        PaymentAllocator.requireFullyPaid(result);

        String invId = "INV-2026-" + (8842 + transactions.size());
        String now = LocalDateTime.now().format(TIME_FMT);
        transactions.add(0, row(
                "Transaction", invId,
                "Date & time", now,
                "Cashier", currentOperator,
                "Payment", method.name(),
                "Total", "$" + totals.total().toPlainString(),
                "Status", "Completed"
        ));
        for (BasketItem item : basket) {
            movements.add(0, row(
                    "Date", now,
                    "Product", item.product().name(),
                    "Type", "SALE",
                    "Quantity", "-" + item.quantity().stripTrailingZeros().toPlainString(),
                    "Reference", invId,
                    "User", currentOperator
            ));
        }
        basket.clear();
        orderDiscount = new BigDecimal("0.00");
        return result;
    }

    public ObservableList<ProductItem> products() { return products; }
    public ObservableList<BasketItem> basket() { return basket; }
    public ObservableList<Map<String, String>> transactions() { return transactions; }
    public ObservableList<Map<String, String>> movements() { return movements; }
    public ObservableList<Map<String, String>> purchases() { return purchases; }
    public ObservableList<Map<String, String>> suppliers() { return suppliers; }
    public ObservableList<Map<String, String>> customers() { return customers; }
    public ObservableList<Map<String, String>> returnsList() { return returnsList; }
    public ObservableList<Map<String, String>> expenses() { return expenses; }
    public ObservableList<Map<String, String>> shifts() { return shifts; }
    public ObservableList<Map<String, String>> users() { return users; }

    public String getCurrentOperator() { return currentOperator; }
    public void setCurrentOperator(String op) { this.currentOperator = op; }
    public String getCurrentRole() { return currentRole; }
    public void setCurrentRole(String role) {
        this.currentRole = role;
        UserRole parsed = "CASHIER".equalsIgnoreCase(role) ? UserRole.CASHIER
                : "MANAGER".equalsIgnoreCase(role) ? UserRole.MANAGER : UserRole.ADMIN;
        this.securitySession.establishSession(currentOperator, currentOperator, parsed);
    }
    public SecurityContextSession securitySession() { return securitySession; }
    public boolean isOracleConnected() { return oracleConnected; }
    public void setOracleConnected(boolean connected) { this.oracleConnected = connected; }
    public String getOracleEndpoint() { return oracleEndpoint; }
    public void setOracleEndpoint(String ep) { this.oracleEndpoint = ep; }
    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal tax) { this.taxPercent = tax; }
    public BigDecimal getOrderDiscount() { return orderDiscount; }
    public void setOrderDiscount(BigDecimal disc) { this.orderDiscount = disc; }
}
