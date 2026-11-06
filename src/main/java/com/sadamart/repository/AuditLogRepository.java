package com.sadamart.repository;

import com.sadamart.domain.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
    List<AuditLogEntry> findTop100ByOrderByCreatedAtDesc();
}
