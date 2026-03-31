package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.SubmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<SubmissionEntity, String> {
    List<SubmissionEntity> findAllBySchoolId(String schoolId);

    Optional<SubmissionEntity> findByIdAndSchoolId(String id, String schoolId);

    Optional<SubmissionEntity> findByAssignmentIdAndLearnerIdAndSchoolId(String assignmentId, String learnerId, String schoolId);
}
