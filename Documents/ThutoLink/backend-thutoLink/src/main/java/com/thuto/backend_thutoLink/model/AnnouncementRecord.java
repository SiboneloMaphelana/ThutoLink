package com.thuto.backend_thutoLink.model;

import java.time.LocalDateTime;

public record AnnouncementRecord(
        String id,
        String classId,
        String teacherId,
        String title,
        String body,
        LocalDateTime sentAt
) {
}
