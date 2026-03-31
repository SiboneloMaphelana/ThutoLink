package com.thuto.backend_thutoLink.model;

import java.util.List;

public record UserAccount(
        String id,
        String fullName,
        String email,
        String password,
        UserRole role,
        String schoolId,
        List<String> classIds,
        List<String> learnerIds
) {
}
