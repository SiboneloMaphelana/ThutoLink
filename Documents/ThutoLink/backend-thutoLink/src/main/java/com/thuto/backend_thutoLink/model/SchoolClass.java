package com.thuto.backend_thutoLink.model;

import java.util.List;

public record SchoolClass(
        String id,
        String schoolId,
        String name,
        String gradeLabel,
        String subject,
        String teacherId,
        List<String> learnerIds
) {
}
