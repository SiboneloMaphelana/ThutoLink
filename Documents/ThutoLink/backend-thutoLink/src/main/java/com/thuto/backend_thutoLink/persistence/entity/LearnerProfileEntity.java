package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "learner_profiles")
public class LearnerProfileEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String gradeLabel;

    @Column(nullable = false)
    private String classId;

    @Column(nullable = false)
    private String parentId;

    public LearnerProfileEntity() {
    }

    public LearnerProfileEntity(String id, String fullName, String gradeLabel, String classId, String parentId) {
        this.id = id;
        this.fullName = fullName;
        this.gradeLabel = gradeLabel;
        this.classId = classId;
        this.parentId = parentId;
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGradeLabel() {
        return gradeLabel;
    }

    public String getClassId() {
        return classId;
    }

    public String getParentId() {
        return parentId;
    }
}
