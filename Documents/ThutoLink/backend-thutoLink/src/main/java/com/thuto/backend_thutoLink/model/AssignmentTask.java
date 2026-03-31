package com.thuto.backend_thutoLink.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AssignmentTask(
        String id,
        String classId,
        String teacherId,
        String title,
        String description,
        LocalDate dueDate,
        LocalDateTime publishedAt,
        String status
) {
}
