package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, String> {
    Optional<SubmissionEntity> findByAssignmentIdAndLearnerId(String assignmentId, String learnerId);
}
