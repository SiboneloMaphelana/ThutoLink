package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.ClassEnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollmentEntity, String> {
    List<ClassEnrollmentEntity> findAllBySchoolId(String schoolId);

    List<ClassEnrollmentEntity> findByLearnerIdAndSchoolId(String learnerId, String schoolId);

    List<ClassEnrollmentEntity> findByClassIdAndSchoolId(String classId, String schoolId);
}
