package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.AnnouncementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<AnnouncementEntity, String> {
    List<AnnouncementEntity> findAllBySchoolId(String schoolId);

    Optional<AnnouncementEntity> findByIdAndSchoolId(String id, String schoolId);
}
