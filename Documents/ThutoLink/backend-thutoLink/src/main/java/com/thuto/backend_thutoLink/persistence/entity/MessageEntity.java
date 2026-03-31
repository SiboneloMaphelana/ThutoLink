package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class MessageEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String schoolId;

    @Column(nullable = false)
    private String classId;

    @Column(nullable = false)
    private String teacherId;

    @Column(nullable = false)
    private String parentId;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    public MessageEntity() {
    }

    public MessageEntity(String id, String schoolId, String classId, String teacherId, String parentId, String subject, String body, LocalDateTime sentAt) {
        this.id = id;
        this.schoolId = schoolId;
        this.classId = classId;
        this.teacherId = teacherId;
        this.parentId = parentId;
        this.subject = subject;
        this.body = body;
        this.sentAt = sentAt;
    }

    public String getId() {
        return id;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getClassId() {
        return classId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
