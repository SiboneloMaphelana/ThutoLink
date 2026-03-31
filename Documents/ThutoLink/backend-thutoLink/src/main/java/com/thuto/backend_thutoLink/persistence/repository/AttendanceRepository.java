package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceEntity, String> {
    List<AttendanceEntity> findAllBySchoolId(String schoolId);

    Optional<AttendanceEntity> findByIdAndSchoolId(String id, String schoolId);

    Optional<AttendanceEntity> findByClassIdAndDateAndSchoolId(String classId, LocalDate date, String schoolId);
}
