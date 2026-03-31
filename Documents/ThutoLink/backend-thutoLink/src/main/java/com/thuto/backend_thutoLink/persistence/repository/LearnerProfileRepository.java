package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.LearnerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearnerProfileRepository extends JpaRepository<LearnerProfileEntity, String> {
    List<LearnerProfileEntity> findAllBySchoolId(String schoolId);

    long countBySchoolId(String schoolId);

    List<LearnerProfileEntity> findByClassIdAndSchoolId(String classId, String schoolId);

    List<LearnerProfileEntity> findByParentIdAndSchoolId(String parentId, String schoolId);
}
