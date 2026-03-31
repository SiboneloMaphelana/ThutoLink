package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.SchoolClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClassEntity, String> {
    long countBySchoolId(String schoolId);

    List<SchoolClassEntity> findAllBySchoolId(String schoolId);

    List<SchoolClassEntity> findByTeacherIdAndSchoolId(String teacherId, String schoolId);

    Optional<SchoolClassEntity> findByIdAndSchoolId(String id, String schoolId);
}
