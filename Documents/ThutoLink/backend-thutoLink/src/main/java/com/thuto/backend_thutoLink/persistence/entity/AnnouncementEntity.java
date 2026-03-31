package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class AnnouncementEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String classId;

    @Column(nullable = false)
    private String teacherId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String body;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    public AnnouncementEntity() {
    }

    public AnnouncementEntity(String id, String classId, String teacherId, String title, String body, LocalDateTime sentAt) {
        this.id = id;
        this.classId = classId;
        this.teacherId = teacherId;
        this.title = title;
        this.body = body;
        this.sentAt = sentAt;
    }

    public String getId() {
        return id;
    }

    public String getClassId() {
        return classId;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
