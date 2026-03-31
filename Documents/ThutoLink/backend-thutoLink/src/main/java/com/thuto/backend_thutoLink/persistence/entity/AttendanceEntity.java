package com.thuto.backend_thutoLink.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attendance_records")
public class AttendanceEntity {
    @Id
    private String id;

    @Column(nullable = false)
    private String classId;

    @Column(nullable = false)
    private String teacherId;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    @OneToMany(mappedBy = "attendance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AttendanceEntryEntity> entries = new ArrayList<>();

    public AttendanceEntity() {
    }

    public AttendanceEntity(String id, String classId, String teacherId, LocalDate date, LocalDateTime recordedAt) {
        this.id = id;
        this.classId = classId;
        this.teacherId = teacherId;
        this.date = date;
        this.recordedAt = recordedAt;
    }

    public void addEntry(AttendanceEntryEntity entry) {
        entries.add(entry);
        entry.setAttendance(this);
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

    public LocalDate getDate() {
        return date;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public List<AttendanceEntryEntity> getEntries() {
        return entries;
    }
}
