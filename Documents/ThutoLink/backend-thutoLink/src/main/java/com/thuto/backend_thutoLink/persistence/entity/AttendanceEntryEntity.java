package com.thuto.backend_thutoLink.persistence.entity;

import com.thuto.backend_thutoLink.model.AttendanceStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "attendance_entries")
public class AttendanceEntryEntity {
    @Id
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_id", nullable = false)
    private AttendanceEntity attendance;

    @Column(nullable = false)
    private String learnerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    public AttendanceEntryEntity() {
    }

    public AttendanceEntryEntity(String id, String learnerId, AttendanceStatus status) {
        this.id = id;
        this.learnerId = learnerId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public AttendanceEntity getAttendance() {
        return attendance;
    }

    public void setAttendance(AttendanceEntity attendance) {
        this.attendance = attendance;
    }

    public String getLearnerId() {
        return learnerId;
    }

    public AttendanceStatus getStatus() {
        return status;
    }
}
