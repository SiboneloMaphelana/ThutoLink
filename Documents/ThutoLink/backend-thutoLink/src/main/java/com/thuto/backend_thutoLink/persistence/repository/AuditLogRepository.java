package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {
    List<AuditLogEntity> findAllBySchoolIdOrderByHappenedAtDesc(String schoolId);
}
