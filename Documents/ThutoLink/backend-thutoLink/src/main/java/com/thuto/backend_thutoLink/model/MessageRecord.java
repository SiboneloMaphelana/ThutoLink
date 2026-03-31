package com.thuto.backend_thutoLink.model;

import java.time.LocalDateTime;

public record MessageRecord(
        String id,
        String classId,
        String teacherId,
        String parentId,
        String subject,
        String body,
        LocalDateTime sentAt
) {
}
