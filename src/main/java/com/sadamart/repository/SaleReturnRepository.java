package com.sadamart.repository;

import com.sadamart.domain.SaleReturn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaleReturnRepository extends JpaRepository<SaleReturn, Long> {
    List<SaleReturn> findByOriginalInvoiceIgnoreCase(String originalInvoice);
}
