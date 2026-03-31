package com.thuto.backend_thutoLink.service;

import java.time.LocalDate;
import java.util.List;

public final class NotificationEvents {
    private NotificationEvents() {
    }

    public record AssignmentPublished(
            String schoolId,
            String classId,
            String assignmentId,
            String title
    ) {
    }

    public record AssignmentGraded(
            String schoolId,
            String classId,
            String assignmentId,
            String submissionId,
            String learnerId,
            Integer score,
            String feedback
    ) {
    }

    public record AttendanceRecorded(
            String schoolId,
            String classId,
            LocalDate date,
            List<AttendanceRecipient> recipients
    ) {
    }

    public record AttendanceRecipient(
            String learnerId,
            String learnerName,
            String parentId,
            String status
    ) {
    }

    public record AnnouncementPosted(
            String schoolId,
            String classId,
            String announcementId,
            String title,
            String body
    ) {
    }

    public record DirectMessageSent(
            String schoolId,
            String classId,
            String messageId,
            String parentId,
            String subject,
            String body
    ) {
    }
}
