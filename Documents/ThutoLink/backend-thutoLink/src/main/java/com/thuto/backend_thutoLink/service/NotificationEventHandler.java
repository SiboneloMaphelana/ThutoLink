package com.thuto.backend_thutoLink.service;

import com.thuto.backend_thutoLink.persistence.entity.LearnerProfileEntity;
import com.thuto.backend_thutoLink.persistence.entity.NotificationEntity;
import com.thuto.backend_thutoLink.persistence.repository.LearnerProfileRepository;
import com.thuto.backend_thutoLink.persistence.repository.NotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class NotificationEventHandler {
    private final NotificationRepository notificationRepository;
    private final LearnerProfileRepository learnerProfileRepository;

    public NotificationEventHandler(
            NotificationRepository notificationRepository,
            LearnerProfileRepository learnerProfileRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.learnerProfileRepository = learnerProfileRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssignmentPublished(NotificationEvents.AssignmentPublished event) {
        List<LearnerProfileEntity> learners = learnerProfileRepository.findByClassIdAndSchoolId(event.classId(), event.schoolId());
        Set<String> recipients = new LinkedHashSet<>();
        learners.forEach(learner -> {
            recipients.add(learner.getId());
            recipients.add(learner.getParentId());
        });

        recipients.forEach(recipientUserId -> saveNotification(
                event.schoolId(),
                recipientUserId,
                "ASSIGNMENT_PUBLISHED",
                "New assignment posted",
                event.title() + " is now available in your class dashboard."
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAssignmentGraded(NotificationEvents.AssignmentGraded event) {
        LearnerProfileEntity learner = learnerProfileRepository.findById(event.learnerId()).orElse(null);
        if (learner == null || !event.schoolId().equals(learner.getSchoolId())) {
            return;
        }

        String body = "A submission was graded" +
                (event.score() != null ? " with score " + event.score() + "." : ".") +
                (event.feedback() != null && !event.feedback().isBlank() ? " Feedback: " + event.feedback() : "");

        saveNotification(event.schoolId(), event.learnerId(), "ASSIGNMENT_GRADED", "Assignment graded", body);
        saveNotification(event.schoolId(), learner.getParentId(), "ASSIGNMENT_GRADED", "Assignment graded", learner.getFullName() + ": " + body);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAttendanceRecorded(NotificationEvents.AttendanceRecorded event) {
        event.recipients().forEach(recipient -> saveNotification(
                event.schoolId(),
                recipient.parentId(),
                "ATTENDANCE_FLAGGED",
                "Attendance update",
                recipient.learnerName() + " was marked " + recipient.status().toLowerCase() + " on " + event.date() + "."
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnnouncementPosted(NotificationEvents.AnnouncementPosted event) {
        List<LearnerProfileEntity> learners = learnerProfileRepository.findByClassIdAndSchoolId(event.classId(), event.schoolId());
        Set<String> recipients = new LinkedHashSet<>();
        learners.forEach(learner -> {
            recipients.add(learner.getId());
            recipients.add(learner.getParentId());
        });

        recipients.forEach(recipientUserId -> saveNotification(
                event.schoolId(),
                recipientUserId,
                "ANNOUNCEMENT_POSTED",
                event.title(),
                event.body()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDirectMessageSent(NotificationEvents.DirectMessageSent event) {
        saveNotification(
                event.schoolId(),
                event.parentId(),
                "DIRECT_MESSAGE",
                event.subject(),
                event.body()
        );
    }

    private void saveNotification(
            String schoolId,
            String recipientUserId,
            String type,
            String title,
            String body
    ) {
        notificationRepository.save(new NotificationEntity(
                nextId(),
                schoolId,
                recipientUserId,
                type,
                title,
                body,
                LocalDateTime.now(),
                null
        ));
    }

    private String nextId() {
        return "notif-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
