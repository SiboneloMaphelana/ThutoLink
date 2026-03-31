package com.thuto.backend_thutoLink.model;

import java.time.LocalDateTime;

public record SubmissionRecord(
        String id,
        String assignmentId,
        String learnerId,
        String content,
        LocalDateTime submittedAt,
        Integer score,
        String feedback,
        String status
) {
}
