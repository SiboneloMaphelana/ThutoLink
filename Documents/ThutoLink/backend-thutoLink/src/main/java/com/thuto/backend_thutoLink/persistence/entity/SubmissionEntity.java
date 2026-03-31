package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "submissions",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_assignment_learner", columnNames = {"assignmentId", "learnerId"})
)
public class SubmissionEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String assignmentId;

    @Column(nullable = false)
    private String learnerId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    private Integer score;

    @Column(length = 2000)
    private String feedback;

    @Column(nullable = false)
    private String status;

    public SubmissionEntity() {
    }

    public SubmissionEntity(
            String id,
            String assignmentId,
            String learnerId,
            String content,
            LocalDateTime submittedAt,
            Integer score,
            String feedback,
            String status
    ) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.learnerId = learnerId;
        this.content = content;
        this.submittedAt = submittedAt;
        this.score = score;
        this.feedback = feedback;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public String getLearnerId() {
        return learnerId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public Integer getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getStatus() {
        return status;
    }
}
