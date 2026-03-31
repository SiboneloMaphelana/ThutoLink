package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String schoolId;

    @Column(nullable = false)
    private String actorUserId;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false)
    private String targetType;

    @Column(nullable = false)
    private String targetId;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false, length = 4000)
    private String afterState;

    @Column(length = 4000)
    private String beforeState;

    @Column(nullable = false)
    private LocalDateTime happenedAt;

    public AuditLogEntity() {
    }

    public AuditLogEntity(
            String id,
            String schoolId,
            String actorUserId,
            String actionType,
            String targetType,
            String targetId,
            String summary,
            String beforeState,
            String afterState,
            LocalDateTime happenedAt
    ) {
        this.id = id;
        this.schoolId = schoolId;
        this.actorUserId = actorUserId;
        this.actionType = actionType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.summary = summary;
        this.beforeState = beforeState;
        this.afterState = afterState;
        this.happenedAt = happenedAt;
    }

    public String getId() {
        return id;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getActorUserId() {
        return actorUserId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getSummary() {
        return summary;
    }

    public String getAfterState() {
        return afterState;
    }

    public String getBeforeState() {
        return beforeState;
    }

    public LocalDateTime getHappenedAt() {
        return happenedAt;
    }
}
