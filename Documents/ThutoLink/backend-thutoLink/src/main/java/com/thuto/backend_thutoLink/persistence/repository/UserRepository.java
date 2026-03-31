package com.thuto.backend_thutoLink.persistence.repository;

import com.thuto.backend_thutoLink.model.UserRole;
import com.thuto.backend_thutoLink.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    Optional<UserEntity> findByIdAndSchoolId(String id, String schoolId);

    List<UserEntity> findAllBySchoolId(String schoolId);

    long countByRole(UserRole role);

    long countByRoleAndSchoolId(UserRole role, String schoolId);
}
