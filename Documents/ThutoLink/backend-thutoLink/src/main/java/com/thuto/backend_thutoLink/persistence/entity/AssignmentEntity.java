package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class AssignmentEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String schoolId;

    @Column(nullable = false)
    private String classId;

    @Column(nullable = false)
    private String teacherId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDateTime publishedAt;

    @Column(nullable = false)
    private String status;

    private String attachmentFileName;

    private String attachmentContentType;

    private Long attachmentSize;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] attachmentData;

    public AssignmentEntity() {
    }

    public AssignmentEntity(
            String id,
            String schoolId,
            String classId,
            String teacherId,
            String title,
            String description,
            LocalDate dueDate,
            LocalDateTime publishedAt,
            String status,
            String attachmentFileName,
            String attachmentContentType,
            Long attachmentSize,
            byte[] attachmentData
    ) {
        this.id = id;
        this.schoolId = schoolId;
        this.classId = classId;
        this.teacherId = teacherId;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.publishedAt = publishedAt;
        this.status = status;
        this.attachmentFileName = attachmentFileName;
        this.attachmentContentType = attachmentContentType;
        this.attachmentSize = attachmentSize;
        this.attachmentData = attachmentData;
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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public String getStatus() {
        return status;
    }

    public String getAttachmentFileName() {
        return attachmentFileName;
    }

    public String getAttachmentContentType() {
        return attachmentContentType;
    }

    public Long getAttachmentSize() {
        return attachmentSize;
    }

    public byte[] getAttachmentData() {
        return attachmentData;
    }
}
