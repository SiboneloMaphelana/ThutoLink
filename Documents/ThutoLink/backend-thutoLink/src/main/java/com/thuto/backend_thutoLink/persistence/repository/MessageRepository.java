package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.persistence.entity.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    List<MessageEntity> findAllBySchoolId(String schoolId);

    Optional<MessageEntity> findByIdAndSchoolId(String id, String schoolId);
}
