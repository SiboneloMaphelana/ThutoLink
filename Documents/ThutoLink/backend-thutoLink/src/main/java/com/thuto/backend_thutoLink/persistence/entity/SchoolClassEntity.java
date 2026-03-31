package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "school_classes")
public class SchoolClassEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String schoolId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String gradeLabel;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String teacherId;

    public SchoolClassEntity() {
    }

    public SchoolClassEntity(String id, String schoolId, String name, String gradeLabel, String subject, String teacherId) {
        this.id = id;
        this.schoolId = schoolId;
        this.name = name;
        this.gradeLabel = gradeLabel;
        this.subject = subject;
        this.teacherId = teacherId;
    }

    public String getId() {
        return id;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public String getName() {
        return name;
    }

    public String getGradeLabel() {
        return gradeLabel;
    }

    public String getSubject() {
        return subject;
    }

    public String getTeacherId() {
        return teacherId;
    }
}
