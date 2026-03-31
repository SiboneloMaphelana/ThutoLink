package com.thuto.backend_thutoLink.model;

public record LearnerProfile(
        String id,
        String fullName,
        String gradeLabel,
        String classId,
        String parentId
) {
}
