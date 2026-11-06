package com.sadamart.repository;

import com.sadamart.domain.ExpenseVoucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseVoucherRepository extends JpaRepository<ExpenseVoucher, Long> { }
