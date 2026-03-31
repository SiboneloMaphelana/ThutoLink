package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.LearnerProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearnerProfileRepository extends JpaRepository<LearnerProfileEntity, String> {
    List<LearnerProfileEntity> findByClassId(String classId);

    List<LearnerProfileEntity> findByParentId(String parentId);
}
