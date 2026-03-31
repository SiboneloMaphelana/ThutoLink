package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "class_enrollments")
public class ClassEnrollmentEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String learnerId;

    @Column(nullable = false)
    private String classId;

    public ClassEnrollmentEntity() {
    }

    public ClassEnrollmentEntity(String id, String learnerId, String classId) {
        this.id = id;
        this.learnerId = learnerId;
        this.classId = classId;
    }

    public String getId() {
        return id;
    }

    public String getLearnerId() {
        return learnerId;
    }

    public String getClassId() {
        return classId;
    }
}
