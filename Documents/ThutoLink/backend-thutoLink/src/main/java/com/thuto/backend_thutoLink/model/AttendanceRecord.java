package com.thuto.backend_thutoLink.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public record AttendanceRecord(
        String id,
        String classId,
        String teacherId,
        LocalDate date,
        Map<String, AttendanceStatus> entries,
        LocalDateTime recordedAt
) {
}
