package com.sadamart.repository;

import com.sadamart.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySkuIgnoreCase(String sku);
    Optional<Product> findByBarcode(String barcode);
}
