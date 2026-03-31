package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.AssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentRepository extends JpaRepository<AssignmentEntity, String> {
    List<AssignmentEntity> findAllBySchoolId(String schoolId);

    Optional<AssignmentEntity> findByIdAndSchoolId(String id, String schoolId);
}
