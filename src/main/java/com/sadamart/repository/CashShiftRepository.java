package com.sadamart.repository;

import com.sadamart.domain.CashShift;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CashShiftRepository extends JpaRepository<CashShift, Long> {
    Optional<CashShift> findFirstByStatusOrderByOpenedAtDesc(String status);
}
