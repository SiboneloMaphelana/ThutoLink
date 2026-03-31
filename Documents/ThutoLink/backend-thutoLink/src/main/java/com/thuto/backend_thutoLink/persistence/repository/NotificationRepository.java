package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, String> {
    List<NotificationEntity> findAllBySchoolIdOrderByCreatedAtDesc(String schoolId);

    List<NotificationEntity> findAllByRecipientUserIdAndSchoolIdOrderByCreatedAtDesc(String recipientUserId, String schoolId);

    Optional<NotificationEntity> findByIdAndRecipientUserIdAndSchoolId(String id, String recipientUserId, String schoolId);
}
