package com.sadamart.repository;

import com.sadamart.domain.SaleTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SaleTransactionRepository extends JpaRepository<SaleTransaction, Long> {
    Optional<SaleTransaction> findByInvoiceNumberIgnoreCase(String invoiceNumber);
    Optional<SaleTransaction> findByIdempotencyKey(String idempotencyKey);
}
