package com.thuto.backend_thutoLink.service;

import com.thuto.backend_thutoLink.api.ApiException;
import com.thuto.backend_thutoLink.model.AttendanceStatus;
import com.thuto.backend_thutoLink.model.UserAccount;
import com.thuto.backend_thutoLink.model.UserRole;
import com.thuto.backend_thutoLink.persistence.entity.*;
import com.thuto.backend_thutoLink.persistence.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PlatformService {
    private static final long MAX_ATTACHMENT_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_ATTACHMENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final LearnerProfileRepository learnerProfileRepository;
    private final ClassEnrollmentRepository classEnrollmentRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final AttendanceRepository attendanceRepository;
    private final AnnouncementRepository announcementRepository;
    private final MessageRepository messageRepository;
    private final AuditLogRepository auditLogRepository;
    private final NotificationRepository notificationRepository;
    private final DomainEventPublisher domainEventPublisher;

    public PlatformService(
            SchoolRepository schoolRepository,
            UserRepository userRepository,
            LearnerProfileRepository learnerProfileRepository,
            ClassEnrollmentRepository classEnrollmentRepository,
            SchoolClassRepository schoolClassRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            AttendanceRepository attendanceRepository,
            AnnouncementRepository announcementRepository,
            MessageRepository messageRepository,
            AuditLogRepository auditLogRepository,
            NotificationRepository notificationRepository,
            DomainEventPublisher domainEventPublisher
    ) {
        this.schoolRepository = schoolRepository;
        this.userRepository = userRepository;
        this.learnerProfileRepository = learnerProfileRepository;
        this.classEnrollmentRepository = classEnrollmentRepository;
        this.schoolClassRepository = schoolClassRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.attendanceRepository = attendanceRepository;
        this.announcementRepository = announcementRepository;
        this.messageRepository = messageRepository;
        this.auditLogRepository = auditLogRepository;
        this.notificationRepository = notificationRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Transactional(readOnly = true)
    public UserAccount authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
        if (!user.getPassword().equals(password)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
        }
        return toUserAccount(user);
    }

    @Transactional(readOnly = true)
    public UserAccount findUserById(String userId) {
        return userRepository.findById(userId)
                .map(this::toUserAccount)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public DashboardResponse dashboardFor(UserAccount currentUser) {
        String schoolId = currentUser.schoolId();
        Map<String, UserEntity> users = indexUsers(schoolId);
        Map<String, LearnerProfileEntity> learners = indexLearners(schoolId);
        Map<String, ClassEnrollmentEntity> enrollments = indexEnrollments(schoolId);
        Map<String, SchoolClassEntity> classes = indexClasses(schoolId);
        Map<String, AssignmentEntity> assignments = indexAssignments(schoolId);
        Map<String, SubmissionEntity> submissions = indexSubmissions(schoolId);
        Map<String, AttendanceEntity> attendance = indexAttendance(schoolId);
        Map<String, AnnouncementEntity> announcements = indexAnnouncements(schoolId);
        Map<String, MessageEntity> messages = indexMessages(schoolId);
        List<NotificationView> notifications = scopedNotifications(currentUser, schoolId);

        SchoolEntity school = requiredSchool(schoolId);

        List<ClassView> scopedClasses = classes.values().stream()
                .map(clazz -> toSchoolClass(clazz, enrollments))
                .filter(clazz -> canAccessClass(currentUser, clazz))
                .map(clazz -> toClassView(clazz, users, learners))
                .sorted(Comparator.comparing(ClassView::name))
                .toList();

        Set<String> visibleClassIds = scopedClasses.stream().map(ClassView::id).collect(java.util.stream.Collectors.toSet());

        List<AssignmentView> scopedAssignments = assignments.values().stream()
                .map(this::toAssignmentTask)
                .filter(assignment -> visibleClassIds.contains(assignment.classId()))
                .sorted(Comparator.comparing(AssignmentTask::dueDate).thenComparing(AssignmentTask::publishedAt).reversed())
                .map(assignment -> toAssignmentView(assignment, currentUser, users, learners, classes, submissions, enrollments))
                .toList();

        List<AttendanceView> scopedAttendance = attendance.values().stream()
                .map(this::toAttendanceRecord)
                .filter(record -> visibleClassIds.contains(record.classId()))
                .sorted(Comparator.comparing(AttendanceRecord::date).reversed())
                .map(record -> toAttendanceView(record, users, classes, enrollments))
                .toList();

        List<AnnouncementView> scopedAnnouncements = announcements.values().stream()
                .map(this::toAnnouncementRecord)
                .filter(record -> visibleClassIds.contains(record.classId()))
                .sorted(Comparator.comparing(AnnouncementRecord::sentAt).reversed())
                .map(record -> toAnnouncementView(record, users, classes))
                .toList();

        List<MessageView> scopedMessages = messages.values().stream()
                .map(this::toMessageRecord)
                .filter(message -> canAccessMessage(currentUser, message))
                .sorted(Comparator.comparing(MessageRecord::sentAt).reversed())
                .map(message -> toMessageView(message, users, classes))
                .toList();

        return new DashboardResponse(
                new UserSummary(currentUser.id(), currentUser.fullName(), currentUser.email(), currentUser.role().name()),
                new SchoolSummary(school.getId(), school.getName(), school.getDistrict()),
                buildStats(currentUser, scopedClasses, scopedAssignments, notifications),
                scopedClasses,
                scopedAssignments,
                scopedAttendance,
                scopedAnnouncements,
                scopedMessages,
                notifications,
                demoCredentials()
        );
    }

    @Transactional
    public AssignmentView createAssignment(UserAccount currentUser, CreateAssignmentRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        String schoolId = currentUser.schoolId();
        SchoolClass schoolClass = requiredClass(schoolId, request.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);
        StoredAttachment storedAttachment = normalizeAttachment(request.attachment());

        AssignmentEntity entity = assignmentRepository.save(new AssignmentEntity(
                nextId("asn"),
                schoolId,
                schoolClass.id(),
                currentUser.id(),
                request.title().trim(),
                request.description().trim(),
                request.dueDate(),
                LocalDateTime.now(),
                "Published",
                storedAttachment.fileName(),
                storedAttachment.contentType(),
                storedAttachment.size(),
                storedAttachment.data()
        ));
        logAudit(
                currentUser,
                "ASSIGNMENT_PUBLISHED",
                "Assignment",
                entity.getId(),
                "Assignment published",
                null,
                assignmentAuditState(entity)
        );
        domainEventPublisher.publish(new NotificationEvents.AssignmentPublished(
                schoolId,
                schoolClass.id(),
                entity.getId(),
                entity.getTitle()
        ));

        return toAssignmentView(
                toAssignmentTask(entity),
                currentUser,
                indexUsers(schoolId),
                indexLearners(schoolId),
                indexClasses(schoolId),
                indexSubmissions(schoolId),
                indexEnrollments(schoolId)
        );
    }

    @Transactional
    public SubmissionView submitAssignment(UserAccount currentUser, String assignmentId, SubmitAssignmentRequest request) {
        assertRole(currentUser, UserRole.LEARNER);
        String schoolId = currentUser.schoolId();
        AssignmentTask assignment = requiredAssignment(schoolId, assignmentId);
        assertLearnerCanAccessAssignment(currentUser, assignment);

        SubmissionEntity existing = submissionRepository.findByAssignmentIdAndLearnerIdAndSchoolId(assignmentId, currentUser.id(), schoolId).orElse(null);
        Map<String, Object> beforeState = existing == null ? null : submissionAuditState(existing);
        StoredAttachment storedAttachment = normalizeAttachment(request.attachment());
        if (storedAttachment.isEmpty() && existing != null) {
            storedAttachment = StoredAttachment.fromEntity(existing.getAttachmentFileName(), existing.getAttachmentContentType(), existing.getAttachmentSize(), existing.getAttachmentData());
        }
        if (normalizeOptionalText(request.content()).isBlank() && storedAttachment.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Add text or attach a PDF/image before submitting.");
        }

        SubmissionEntity submission = submissionRepository.save(new SubmissionEntity(
                existing != null ? existing.getId() : nextId("sub"),
                schoolId,
                assignmentId,
                currentUser.id(),
                normalizeOptionalText(request.content()),
                LocalDateTime.now(),
                existing != null ? existing.getScore() : null,
                existing != null ? existing.getFeedback() : "Awaiting teacher review.",
                existing != null && existing.getScore() != null ? "Resubmitted" : "Submitted",
                storedAttachment.fileName(),
                storedAttachment.contentType(),
                storedAttachment.size(),
                storedAttachment.data()
        ));
        logAudit(
                currentUser,
                existing == null ? "SUBMISSION_CREATED" : "SUBMISSION_UPDATED",
                "Submission",
                submission.getId(),
                existing == null ? "Assignment submitted" : "Assignment resubmitted",
                beforeState,
                submissionAuditState(submission)
        );
        return toSubmissionView(toSubmissionRecord(submission), indexUsers(schoolId));
    }

    @Transactional
    public SubmissionView gradeSubmission(UserAccount currentUser, String submissionId, GradeSubmissionRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        String schoolId = currentUser.schoolId();
        SubmissionEntity existing = requiredSubmissionEntity(schoolId, submissionId);
        AssignmentTask assignment = requiredAssignment(schoolId, existing.getAssignmentId());
        SchoolClass schoolClass = requiredClass(schoolId, assignment.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);
        Map<String, Object> beforeState = submissionAuditState(existing);

        SubmissionEntity graded = submissionRepository.save(new SubmissionEntity(
                existing.getId(),
                schoolId,
                existing.getAssignmentId(),
                existing.getLearnerId(),
                existing.getContent(),
                existing.getSubmittedAt(),
                request.score(),
                request.feedback().trim(),
                "Reviewed",
                existing.getAttachmentFileName(),
                existing.getAttachmentContentType(),
                existing.getAttachmentSize(),
                existing.getAttachmentData()
        ));
        logAudit(
                currentUser,
                "GRADE_UPDATED",
                "Submission",
                graded.getId(),
                "Grade updated",
                beforeState,
                submissionAuditState(graded)
        );
        domainEventPublisher.publish(new NotificationEvents.AssignmentGraded(
                schoolId,
                schoolClass.id(),
                graded.getAssignmentId(),
                graded.getId(),
                graded.getLearnerId(),
                graded.getScore(),
                graded.getFeedback()
        ));
        return toSubmissionView(toSubmissionRecord(graded), indexUsers(schoolId));
    }

    @Transactional(readOnly = true)
    public DownloadedFile downloadAssignmentAttachment(UserAccount currentUser, String assignmentId) {
        String schoolId = currentUser.schoolId();
        AssignmentEntity assignment = requiredAssignmentEntity(schoolId, assignmentId);
        SchoolClass schoolClass = requiredClass(schoolId, assignment.getClassId());
        if (!canAccessClass(currentUser, schoolClass)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this assignment attachment.");
        }
        return toDownloadedFile(
                assignment.getAttachmentFileName(),
                assignment.getAttachmentContentType(),
                assignment.getAttachmentData(),
                "Assignment attachment not found."
        );
    }

    @Transactional(readOnly = true)
    public DownloadedFile downloadSubmissionAttachment(UserAccount currentUser, String submissionId) {
        String schoolId = currentUser.schoolId();
        SubmissionEntity submission = requiredSubmissionEntity(schoolId, submissionId);
        AssignmentTask assignment = requiredAssignment(schoolId, submission.getAssignmentId());
        SchoolClass schoolClass = requiredClass(schoolId, assignment.classId());
        if (!canAccessSubmissionAttachment(currentUser, submission, schoolClass)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You do not have access to this submission attachment.");
        }
        return toDownloadedFile(
                submission.getAttachmentFileName(),
                submission.getAttachmentContentType(),
                submission.getAttachmentData(),
                "Submission attachment not found."
        );
    }

    @Transactional
    public AttendanceView recordAttendance(UserAccount currentUser, String classId, RecordAttendanceRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        String schoolId = currentUser.schoolId();
        SchoolClass schoolClass = requiredClass(schoolId, classId);
        assertTeacherOwnsClass(currentUser, schoolClass);

        AttendanceEntity existing = attendanceRepository.findByClassIdAndDateAndSchoolId(classId, request.date(), schoolId).orElse(null);
        Map<String, Object> beforeState = existing == null ? null : attendanceAuditState(existing);

        AttendanceEntity record = new AttendanceEntity(
                existing != null ? existing.getId() : nextId("att"),
                schoolId,
                classId,
                currentUser.id(),
                request.date(),
                LocalDateTime.now()
        );
        for (AttendanceEntryRequest entry : request.entries()) {
            if (!schoolClass.learnerIds().contains(entry.learnerId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Attendance includes a learner outside this class.");
            }
            record.addEntry(new AttendanceEntryEntity(nextId("att-entry"), entry.learnerId(), entry.status()));
        }

        AttendanceEntity saved = attendanceRepository.save(record);
        logAudit(
                currentUser,
                existing == null ? "ATTENDANCE_RECORDED" : "ATTENDANCE_EDITED",
                "Attendance",
                saved.getId(),
                existing == null ? "Attendance recorded" : "Attendance edited",
                beforeState,
                attendanceAuditState(saved)
        );
        List<NotificationEvents.AttendanceRecipient> recipients = saved.getEntries().stream()
                .filter(entry -> entry.getStatus() == AttendanceStatus.ABSENT || entry.getStatus() == AttendanceStatus.LATE)
                .map(entry -> learnerProfileRepository.findById(entry.getLearnerId()).orElse(null))
                .filter(Objects::nonNull)
                .filter(learner -> schoolId.equals(learner.getSchoolId()))
                .map(learner -> {
                    AttendanceStatus status = saved.getEntries().stream()
                            .filter(entry -> entry.getLearnerId().equals(learner.getId()))
                            .findFirst()
                            .map(AttendanceEntryEntity::getStatus)
                            .orElse(AttendanceStatus.PRESENT);
                    return new NotificationEvents.AttendanceRecipient(
                            learner.getId(),
                            learner.getFullName(),
                            learner.getParentId(),
                            status.name()
                    );
                })
                .toList();
        if (!recipients.isEmpty()) {
            domainEventPublisher.publish(new NotificationEvents.AttendanceRecorded(
                    schoolId,
                    classId,
                    saved.getDate(),
                    recipients
            ));
        }
        return toAttendanceView(toAttendanceRecord(saved), indexUsers(schoolId), indexClasses(schoolId), indexEnrollments(schoolId));
    }

    @Transactional
    public AnnouncementView createAnnouncement(UserAccount currentUser, CreateAnnouncementRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        String schoolId = currentUser.schoolId();
        SchoolClass schoolClass = requiredClass(schoolId, request.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);

        AnnouncementEntity saved = announcementRepository.save(new AnnouncementEntity(
                nextId("ann"),
                schoolId,
                schoolClass.id(),
                currentUser.id(),
                request.title().trim(),
                request.body().trim(),
                LocalDateTime.now()
        ));
        logAudit(
                currentUser,
                "ANNOUNCEMENT_SENT",
                "Announcement",
                saved.getId(),
                "Announcement sent",
                null,
                Map.of(
                        "announcementId", saved.getId(),
                        "classId", saved.getClassId(),
                        "title", saved.getTitle(),
                        "body", saved.getBody()
                )
        );
        domainEventPublisher.publish(new NotificationEvents.AnnouncementPosted(
                schoolId,
                schoolClass.id(),
                saved.getId(),
                saved.getTitle(),
                saved.getBody()
        ));
        return toAnnouncementView(toAnnouncementRecord(saved), indexUsers(schoolId), indexClasses(schoolId));
    }

    @Transactional
    public MessageView createMessage(UserAccount currentUser, CreateMessageRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        String schoolId = currentUser.schoolId();
        SchoolClass schoolClass = requiredClass(schoolId, request.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);

        UserAccount parent = requiredUser(schoolId, request.parentId());
        if (parent.role() != UserRole.PARENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Messages can only be sent to parents.");
        }

        boolean parentLinked = parent.learnerIds().stream().anyMatch(schoolClass.learnerIds()::contains);
        if (!parentLinked) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Parent is not linked to a learner in this class.");
        }

        MessageEntity saved = messageRepository.save(new MessageEntity(
                nextId("msg"),
                schoolId,
                schoolClass.id(),
                currentUser.id(),
                parent.id(),
                request.subject().trim(),
                request.body().trim(),
                LocalDateTime.now()
        ));
        logAudit(
                currentUser,
                "MESSAGE_SENT",
                "Message",
                saved.getId(),
                "Message sent",
                null,
                Map.of(
                        "messageId", saved.getId(),
                        "classId", saved.getClassId(),
                        "parentId", saved.getParentId(),
                        "subject", saved.getSubject(),
                        "body", saved.getBody()
                )
        );
        domainEventPublisher.publish(new NotificationEvents.DirectMessageSent(
                schoolId,
                schoolClass.id(),
                saved.getId(),
                saved.getParentId(),
                saved.getSubject(),
                saved.getBody()
        ));
        return toMessageView(toMessageRecord(saved), indexUsers(schoolId), indexClasses(schoolId));
    }

    @Transactional
    public NotificationView markNotificationRead(UserAccount currentUser, String notificationId) {
        NotificationEntity notification = notificationRepository.findByIdAndRecipientUserIdAndSchoolId(notificationId, currentUser.id(), currentUser.schoolId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found."));

        NotificationEntity updated = notificationRepository.save(new NotificationEntity(
                notification.getId(),
                notification.getSchoolId(),
                notification.getRecipientUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getCreatedAt(),
                notification.getReadAt() != null ? notification.getReadAt() : LocalDateTime.now()
        ));
        return toNotificationView(updated);
    }

    private List<StatCardView> buildStats(
            UserAccount currentUser,
            List<ClassView> scopedClasses,
            List<AssignmentView> scopedAssignments,
            List<NotificationView> notifications
    ) {
        if (currentUser.role() == UserRole.TEACHER) {
            long pendingGrades = scopedAssignments.stream()
                    .flatMap(assignment -> assignment.submissions().stream())
                    .filter(submission -> "Submitted".equals(submission.status()) || "Resubmitted".equals(submission.status()))
                    .count();
            return List.of(
                    new StatCardView("Active Classes", String.valueOf(scopedClasses.size()), "Classes currently assigned"),
                    new StatCardView("Published Tasks", String.valueOf(scopedAssignments.size()), "Assignments live for learners"),
                    new StatCardView("Pending Reviews", String.valueOf(pendingGrades), "Submissions waiting for grading"),
                    new StatCardView("Unread Notices", String.valueOf(unreadCount(notifications)), "Notifications waiting to be seen")
            );
        }

        if (currentUser.role() == UserRole.PARENT) {
            return List.of(
                    new StatCardView("Linked Learners", String.valueOf(currentUser.learnerIds().size()), "Children connected to your account"),
                    new StatCardView("Unread Notifications", String.valueOf(unreadCount(notifications)), "Items needing your attention"),
                    new StatCardView("Assignment Updates", String.valueOf(countByType(notifications, "ASSIGNMENT_GRADED")), "Recently graded work"),
                    new StatCardView("Attendance Updates", String.valueOf(countByType(notifications, "ATTENDANCE_FLAGGED")), "Absence and lateness notices")
            );
        }

        if (currentUser.role() == UserRole.LEARNER) {
            long reviewed = scopedAssignments.stream()
                    .flatMap(assignment -> assignment.submissions().stream())
                    .filter(submission -> "Reviewed".equals(submission.status()))
                    .count();
            return List.of(
                    new StatCardView("My Classes", String.valueOf(scopedClasses.size()), "Subjects on your timetable"),
                    new StatCardView("Assignments", String.valueOf(scopedAssignments.size()), "Tasks available to complete"),
                    new StatCardView("Feedback Returned", String.valueOf(reviewed), "Assignments already reviewed"),
                    new StatCardView("Unread Notifications", String.valueOf(unreadCount(notifications)), "Messages and class updates")
            );
        }

        long totalLearners = learnerProfileRepository.countBySchoolId(currentUser.schoolId());
        long totalParents = userRepository.countByRoleAndSchoolId(UserRole.PARENT, currentUser.schoolId());
        return List.of(
                new StatCardView("Schools", "1", "Your tenant scope"),
                new StatCardView("Classes", String.valueOf(schoolClassRepository.countBySchoolId(currentUser.schoolId())), "Structured enrolment groups"),
                new StatCardView("Learners", String.valueOf(totalLearners), "Tracked across all classes"),
                new StatCardView("Unread Notifications", String.valueOf(unreadCount(notifications)), "School-wide outstanding notices")
        );
    }

    private boolean canAccessClass(UserAccount currentUser, SchoolClass schoolClass) {
        return switch (currentUser.role()) {
            case ADMIN -> true;
            case TEACHER -> schoolClass.teacherId().equals(currentUser.id());
            case LEARNER -> schoolClass.learnerIds().contains(currentUser.id());
            case PARENT -> currentUser.learnerIds().stream().anyMatch(schoolClass.learnerIds()::contains);
        };
    }

    private boolean canAccessMessage(UserAccount currentUser, MessageRecord message) {
        return currentUser.role() == UserRole.ADMIN
                || message.teacherId().equals(currentUser.id())
                || message.parentId().equals(currentUser.id());
    }

    private void assertRole(UserAccount currentUser, UserRole role) {
        if (currentUser.role() != role) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This action is not available for your role.");
        }
    }

    private void assertTeacherOwnsClass(UserAccount currentUser, SchoolClass schoolClass) {
        if (!schoolClass.teacherId().equals(currentUser.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only manage classes assigned to you.");
        }
    }

    private void assertLearnerCanAccessAssignment(UserAccount currentUser, AssignmentTask assignment) {
        SchoolClass schoolClass = requiredClass(currentUser.schoolId(), assignment.classId());
        if (!schoolClass.learnerIds().contains(currentUser.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This assignment is not assigned to you.");
        }
    }

    private SchoolEntity requiredSchool(String schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "School not found."));
    }

    private SchoolClass requiredClass(String schoolId, String classId) {
        return schoolClassRepository.findByIdAndSchoolId(classId, schoolId)
                .map(entity -> toSchoolClass(entity, indexEnrollments(schoolId)))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found."));
    }

    private AssignmentTask requiredAssignment(String schoolId, String assignmentId) {
        return assignmentRepository.findByIdAndSchoolId(assignmentId, schoolId)
                .map(this::toAssignmentTask)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found."));
    }

    private AssignmentEntity requiredAssignmentEntity(String schoolId, String assignmentId) {
        return assignmentRepository.findByIdAndSchoolId(assignmentId, schoolId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found."));
    }

    private SubmissionEntity requiredSubmissionEntity(String schoolId, String submissionId) {
        return submissionRepository.findByIdAndSchoolId(submissionId, schoolId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Submission not found."));
    }

    private UserAccount requiredUser(String schoolId, String userId) {
        return userRepository.findByIdAndSchoolId(userId, schoolId)
                .map(this::toUserAccount)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private ClassView toClassView(SchoolClass schoolClass, Map<String, UserEntity> users, Map<String, LearnerProfileEntity> learners) {
        UserEntity teacher = requiredUserEntity(users, schoolClass.teacherId());
        List<UserSummary> learnerViews = schoolClass.learnerIds().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .map(user -> new UserSummary(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name()))
                .toList();

        List<ParentLinkView> parentLinks = schoolClass.learnerIds().stream()
                .map(learners::get)
                .filter(Objects::nonNull)
                .map(learner -> {
                    UserEntity parent = requiredUserEntity(users, learner.getParentId());
                    return new ParentLinkView(
                            learner.getId(),
                            learner.getFullName(),
                            new UserSummary(parent.getId(), parent.getFullName(), parent.getEmail(), parent.getRole().name())
                    );
                })
                .toList();

        return new ClassView(
                schoolClass.id(),
                schoolClass.name(),
                schoolClass.gradeLabel(),
                schoolClass.subject(),
                new UserSummary(teacher.getId(), teacher.getFullName(), teacher.getEmail(), teacher.getRole().name()),
                learnerViews,
                parentLinks
        );
    }

    private AssignmentView toAssignmentView(
            AssignmentTask assignment,
            UserAccount currentUser,
            Map<String, UserEntity> users,
            Map<String, LearnerProfileEntity> learners,
            Map<String, SchoolClassEntity> classes,
            Map<String, SubmissionEntity> submissions,
            Map<String, ClassEnrollmentEntity> enrollments
    ) {
        SchoolClass schoolClass = toSchoolClass(requiredClassEntity(classes, assignment.classId()), enrollments);
        List<SubmissionView> scopedSubmissions = submissions.values().stream()
                .map(this::toSubmissionRecord)
                .filter(submission -> submission.assignmentId().equals(assignment.id()))
                .filter(submission -> currentUser.role() != UserRole.LEARNER || submission.learnerId().equals(currentUser.id()))
                .filter(submission -> currentUser.role() != UserRole.PARENT || currentUser.learnerIds().contains(submission.learnerId()))
                .sorted(Comparator.comparing(SubmissionRecord::submittedAt).reversed())
                .map(submission -> toSubmissionView(submission, users))
                .toList();

        return new AssignmentView(
                assignment.id(),
                schoolClass.id(),
                schoolClass.name(),
                assignment.title(),
                assignment.description(),
                assignment.dueDate(),
                assignment.publishedAt(),
                assignment.status(),
                assignment.attachment(),
                scopedSubmissions
        );
    }

    private SubmissionView toSubmissionView(SubmissionRecord submission, Map<String, UserEntity> users) {
        UserEntity learner = requiredUserEntity(users, submission.learnerId());
        return new SubmissionView(
                submission.id(),
                new UserSummary(learner.getId(), learner.getFullName(), learner.getEmail(), learner.getRole().name()),
                submission.content(),
                submission.submittedAt(),
                submission.score(),
                submission.feedback(),
                submission.status(),
                submission.attachment()
        );
    }

    private AttendanceView toAttendanceView(
            AttendanceRecord record,
            Map<String, UserEntity> users,
            Map<String, SchoolClassEntity> classes,
            Map<String, ClassEnrollmentEntity> enrollments
    ) {
        SchoolClass schoolClass = toSchoolClass(requiredClassEntity(classes, record.classId()), enrollments);
        List<AttendanceEntryView> entries = record.entries().entrySet().stream()
                .map(entry -> {
                    UserEntity learner = requiredUserEntity(users, entry.getKey());
                    return new AttendanceEntryView(
                            new UserSummary(learner.getId(), learner.getFullName(), learner.getEmail(), learner.getRole().name()),
                            entry.getValue().name()
                    );
                })
                .sorted(Comparator.comparing(view -> view.learner().fullName()))
                .toList();

        return new AttendanceView(record.id(), schoolClass.id(), schoolClass.name(), record.date(), entries);
    }

    private AnnouncementView toAnnouncementView(AnnouncementRecord record, Map<String, UserEntity> users, Map<String, SchoolClassEntity> classes) {
        UserEntity teacher = requiredUserEntity(users, record.teacherId());
        SchoolClassEntity schoolClass = requiredClassEntity(classes, record.classId());
        return new AnnouncementView(
                record.id(),
                schoolClass.getId(),
                schoolClass.getName(),
                record.title(),
                record.body(),
                record.sentAt(),
                new UserSummary(teacher.getId(), teacher.getFullName(), teacher.getEmail(), teacher.getRole().name())
        );
    }

    private MessageView toMessageView(MessageRecord record, Map<String, UserEntity> users, Map<String, SchoolClassEntity> classes) {
        UserEntity teacher = requiredUserEntity(users, record.teacherId());
        UserEntity parent = requiredUserEntity(users, record.parentId());
        SchoolClassEntity schoolClass = requiredClassEntity(classes, record.classId());
        return new MessageView(
                record.id(),
                schoolClass.getId(),
                schoolClass.getName(),
                record.subject(),
                record.body(),
                record.sentAt(),
                new UserSummary(teacher.getId(), teacher.getFullName(), teacher.getEmail(), teacher.getRole().name()),
                new UserSummary(parent.getId(), parent.getFullName(), parent.getEmail(), parent.getRole().name())
        );
    }

    private UserAccount toUserAccount(UserEntity user) {
        List<String> learnerIds = user.getRole() == UserRole.PARENT
                ? learnerProfileRepository.findByParentIdAndSchoolId(user.getId(), user.getSchoolId()).stream().map(LearnerProfileEntity::getId).toList()
                : List.of();

        List<String> classIds;
        if (user.getRole() == UserRole.TEACHER) {
            classIds = schoolClassRepository.findByTeacherIdAndSchoolId(user.getId(), user.getSchoolId()).stream()
                    .map(SchoolClassEntity::getId)
                    .toList();
        } else if (user.getRole() == UserRole.LEARNER) {
            classIds = classEnrollmentRepository.findByLearnerIdAndSchoolId(user.getId(), user.getSchoolId()).stream()
                    .map(ClassEnrollmentEntity::getClassId)
                    .distinct()
                    .toList();
        } else {
            classIds = List.of();
        }

        return new UserAccount(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                user.getSchoolId(),
                classIds,
                learnerIds
        );
    }

    private SchoolClass toSchoolClass(SchoolClassEntity entity, Map<String, ClassEnrollmentEntity> enrollments) {
        List<String> learnerIds = enrollments.values().stream()
                .filter(enrollment -> enrollment.getClassId().equals(entity.getId()))
                .map(ClassEnrollmentEntity::getLearnerId)
                .distinct()
                .toList();
        return new SchoolClass(entity.getId(), entity.getSchoolId(), entity.getName(), entity.getGradeLabel(), entity.getSubject(), entity.getTeacherId(), learnerIds);
    }

    private AssignmentTask toAssignmentTask(AssignmentEntity entity) {
        return new AssignmentTask(
                entity.getId(),
                entity.getClassId(),
                entity.getTeacherId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDueDate(),
                entity.getPublishedAt(),
                entity.getStatus(),
                toAttachmentView(entity.getAttachmentFileName(), entity.getAttachmentContentType(), entity.getAttachmentSize())
        );
    }

    private SubmissionRecord toSubmissionRecord(SubmissionEntity entity) {
        return new SubmissionRecord(
                entity.getId(),
                entity.getAssignmentId(),
                entity.getLearnerId(),
                entity.getContent(),
                entity.getSubmittedAt(),
                entity.getScore(),
                entity.getFeedback(),
                entity.getStatus(),
                toAttachmentView(entity.getAttachmentFileName(), entity.getAttachmentContentType(), entity.getAttachmentSize())
        );
    }

    private AttendanceRecord toAttendanceRecord(AttendanceEntity entity) {
        Map<String, AttendanceStatus> entries = new LinkedHashMap<>();
        entity.getEntries().forEach(entry -> entries.put(entry.getLearnerId(), entry.getStatus()));
        return new AttendanceRecord(entity.getId(), entity.getClassId(), entity.getTeacherId(), entity.getDate(), entries, entity.getRecordedAt());
    }

    private AnnouncementRecord toAnnouncementRecord(AnnouncementEntity entity) {
        return new AnnouncementRecord(entity.getId(), entity.getClassId(), entity.getTeacherId(), entity.getTitle(), entity.getBody(), entity.getSentAt());
    }

    private MessageRecord toMessageRecord(MessageEntity entity) {
        return new MessageRecord(entity.getId(), entity.getClassId(), entity.getTeacherId(), entity.getParentId(), entity.getSubject(), entity.getBody(), entity.getSentAt());
    }

    private Map<String, UserEntity> indexUsers(String schoolId) {
        return userRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(UserEntity::getId, user -> user));
    }

    private Map<String, LearnerProfileEntity> indexLearners(String schoolId) {
        return learnerProfileRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(LearnerProfileEntity::getId, learner -> learner));
    }

    private Map<String, ClassEnrollmentEntity> indexEnrollments(String schoolId) {
        return classEnrollmentRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(ClassEnrollmentEntity::getId, enrollment -> enrollment));
    }

    private Map<String, SchoolClassEntity> indexClasses(String schoolId) {
        return schoolClassRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(SchoolClassEntity::getId, clazz -> clazz));
    }

    private Map<String, AssignmentEntity> indexAssignments(String schoolId) {
        return assignmentRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(AssignmentEntity::getId, assignment -> assignment));
    }

    private Map<String, SubmissionEntity> indexSubmissions(String schoolId) {
        return submissionRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(SubmissionEntity::getId, submission -> submission));
    }

    private Map<String, AttendanceEntity> indexAttendance(String schoolId) {
        return attendanceRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(AttendanceEntity::getId, record -> record));
    }

    private Map<String, AnnouncementEntity> indexAnnouncements(String schoolId) {
        return announcementRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(AnnouncementEntity::getId, announcement -> announcement));
    }

    private Map<String, MessageEntity> indexMessages(String schoolId) {
        return messageRepository.findAllBySchoolId(schoolId).stream().collect(java.util.stream.Collectors.toMap(MessageEntity::getId, message -> message));
    }

    private List<NotificationView> scopedNotifications(UserAccount currentUser, String schoolId) {
        List<NotificationEntity> notifications = currentUser.role() == UserRole.ADMIN
                ? notificationRepository.findAllBySchoolIdOrderByCreatedAtDesc(schoolId)
                : notificationRepository.findAllByRecipientUserIdAndSchoolIdOrderByCreatedAtDesc(currentUser.id(), schoolId);

        return notifications.stream()
                .map(this::toNotificationView)
                .toList();
    }

    private UserEntity requiredUserEntity(Map<String, UserEntity> users, String userId) {
        UserEntity user = users.get(userId);
        if (user == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User not found.");
        }
        return user;
    }

    private SchoolClassEntity requiredClassEntity(Map<String, SchoolClassEntity> classes, String classId) {
        SchoolClassEntity schoolClass = classes.get(classId);
        if (schoolClass == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Class not found.");
        }
        return schoolClass;
    }

    private List<DemoCredential> demoCredentials() {
        return List.of(
                new DemoCredential("Admin", "principal@thutolink.school", "admin123", "ADMIN"),
                new DemoCredential("Teacher", "teacher.nkosi@thutolink.school", "teacher123", "TEACHER"),
                new DemoCredential("Parent", "parent.dlamini@thutolink.school", "parent123", "PARENT"),
                new DemoCredential("Learner", "amahle@thutolink.school", "learner123", "LEARNER")
        );
    }

    private String nextId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private FileAttachmentView toAttachmentView(String fileName, String contentType, Long size) {
        if (fileName == null || contentType == null || size == null) {
            return null;
        }
        return new FileAttachmentView(fileName, contentType, size);
    }

    private DownloadedFile toDownloadedFile(String fileName, String contentType, byte[] data, String errorMessage) {
        if (fileName == null || contentType == null || data == null || data.length == 0) {
            throw new ApiException(HttpStatus.NOT_FOUND, errorMessage);
        }
        return new DownloadedFile(fileName, contentType, data);
    }

    private StoredAttachment normalizeAttachment(FileUploadPayload payload) {
        if (payload == null || payload.base64Data() == null || payload.base64Data().isBlank()) {
            return StoredAttachment.empty();
        }

        String fileName = normalizeRequiredText(payload.fileName(), "Attachment file name is required.");
        String contentType = normalizeRequiredText(payload.contentType(), "Attachment type is required.").toLowerCase(Locale.ROOT);
        if (!ALLOWED_ATTACHMENT_TYPES.contains(contentType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF and image attachments are supported.");
        }

        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(payload.base64Data());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Attachment content is not valid.");
        }
        if (decoded.length == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Attachment content is empty.");
        }
        if (decoded.length > MAX_ATTACHMENT_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Attachments must be 5 MB or smaller.");
        }
        long declaredSize = payload.size() == null ? decoded.length : payload.size();
        if (declaredSize != decoded.length) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Attachment size metadata does not match the uploaded file.");
        }
        return new StoredAttachment(fileName, contentType, declaredSize, decoded);
    }

    private String normalizeRequiredText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean canAccessSubmissionAttachment(UserAccount currentUser, SubmissionEntity submission, SchoolClass schoolClass) {
        if (currentUser.role() == UserRole.ADMIN) {
            return true;
        }
        if (currentUser.role() == UserRole.TEACHER) {
            return schoolClass.teacherId().equals(currentUser.id());
        }
        if (currentUser.role() == UserRole.LEARNER) {
            return submission.getLearnerId().equals(currentUser.id());
        }
        return currentUser.role() == UserRole.PARENT && currentUser.learnerIds().contains(submission.getLearnerId());
    }

    private NotificationView toNotificationView(NotificationEntity entity) {
        return new NotificationView(
                entity.getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getBody(),
                entity.getCreatedAt(),
                entity.getReadAt(),
                entity.getReadAt() != null
        );
    }

    private void logAudit(
            UserAccount actor,
            String actionType,
            String targetType,
            String targetId,
            String summary,
            Object beforeState,
            Object afterState
    ) {
        auditLogRepository.save(new AuditLogEntity(
                nextId("audit"),
                actor.schoolId(),
                actor.id(),
                actionType,
                targetType,
                targetId,
                summary,
                toJson(beforeState),
                toJson(afterState),
                LocalDateTime.now()
        ));
    }

    private Map<String, Object> submissionAuditState(SubmissionEntity entity) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("submissionId", entity.getId());
        payload.put("assignmentId", entity.getAssignmentId());
        payload.put("learnerId", entity.getLearnerId());
        payload.put("content", entity.getContent());
        payload.put("attachment", attachmentAuditState(entity.getAttachmentFileName(), entity.getAttachmentContentType(), entity.getAttachmentSize()));
        payload.put("score", entity.getScore());
        payload.put("feedback", entity.getFeedback());
        payload.put("status", entity.getStatus());
        payload.put("submittedAt", String.valueOf(entity.getSubmittedAt()));
        return payload;
    }

    private Map<String, Object> assignmentAuditState(AssignmentEntity entity) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("assignmentId", entity.getId());
        payload.put("classId", entity.getClassId());
        payload.put("teacherId", entity.getTeacherId());
        payload.put("title", entity.getTitle());
        payload.put("description", entity.getDescription());
        payload.put("dueDate", String.valueOf(entity.getDueDate()));
        payload.put("status", entity.getStatus());
        payload.put("attachment", attachmentAuditState(entity.getAttachmentFileName(), entity.getAttachmentContentType(), entity.getAttachmentSize()));
        return payload;
    }

    private Map<String, Object> attachmentAuditState(String fileName, String contentType, Long size) {
        if (fileName == null || contentType == null || size == null) {
            return null;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fileName", fileName);
        payload.put("contentType", contentType);
        payload.put("size", size);
        return payload;
    }

    private Map<String, Object> attendanceAuditState(AttendanceEntity entity) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("attendanceId", entity.getId());
        payload.put("classId", entity.getClassId());
        payload.put("teacherId", entity.getTeacherId());
        payload.put("date", String.valueOf(entity.getDate()));
        payload.put("recordedAt", String.valueOf(entity.getRecordedAt()));

        List<Map<String, String>> entries = entity.getEntries().stream()
                .sorted(Comparator.comparing(AttendanceEntryEntity::getLearnerId))
                .map(entry -> Map.of(
                        "learnerId", entry.getLearnerId(),
                        "status", entry.getStatus().name()
                ))
                .toList();
        payload.put("entries", entries);
        return payload;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    private long unreadCount(List<NotificationView> notifications) {
        return notifications.stream().filter(notification -> !notification.read()).count();
    }

    private long countByType(List<NotificationView> notifications, String type) {
        return notifications.stream().filter(notification -> notification.type().equals(type)).count();
    }

    public record LoginRequest(String email, String password) {
    }

    public record LoginResponse(String token, UserSummary user, List<DemoCredential> demoCredentials) {
    }

    public record UserSummary(String id, String fullName, String email, String role) {
    }

    public record SchoolSummary(String id, String name, String district) {
    }

    public record StatCardView(String label, String value, String caption) {
    }

    public record ClassView(
            String id,
            String name,
            String gradeLabel,
            String subject,
            UserSummary teacher,
            List<UserSummary> learners,
            List<ParentLinkView> parentLinks
    ) {
    }

    public record ParentLinkView(
            String learnerId,
            String learnerName,
            UserSummary parent
    ) {
    }

    public record SubmissionView(
            String id,
            UserSummary learner,
            String content,
            LocalDateTime submittedAt,
            Integer score,
            String feedback,
            String status,
            FileAttachmentView attachment
    ) {
    }

    public record FileAttachmentView(
            String fileName,
            String contentType,
            long size
    ) {
    }

    public record AssignmentView(
            String id,
            String classId,
            String className,
            String title,
            String description,
            LocalDate dueDate,
            LocalDateTime publishedAt,
            String status,
            FileAttachmentView attachment,
            List<SubmissionView> submissions
    ) {
    }

    public record AttendanceEntryView(UserSummary learner, String status) {
    }

    public record AttendanceView(
            String id,
            String classId,
            String className,
            LocalDate date,
            List<AttendanceEntryView> entries
    ) {
    }

    public record AnnouncementView(
            String id,
            String classId,
            String className,
            String title,
            String body,
            LocalDateTime sentAt,
            UserSummary teacher
    ) {
    }

    public record MessageView(
            String id,
            String classId,
            String className,
            String subject,
            String body,
            LocalDateTime sentAt,
            UserSummary teacher,
            UserSummary parent
    ) {
    }

    public record NotificationView(
            String id,
            String type,
            String title,
            String body,
            LocalDateTime createdAt,
            LocalDateTime readAt,
            boolean read
    ) {
    }

    public record DemoCredential(String label, String email, String password, String role) {
    }

    public record DashboardResponse(
            UserSummary currentUser,
            SchoolSummary school,
            List<StatCardView> stats,
            List<ClassView> classes,
            List<AssignmentView> assignments,
            List<AttendanceView> attendance,
            List<AnnouncementView> announcements,
            List<MessageView> messages,
            List<NotificationView> notifications,
            List<DemoCredential> demoCredentials
    ) {
    }

    public record CreateAssignmentRequest(
            String classId,
            String title,
            String description,
            LocalDate dueDate,
            FileUploadPayload attachment
    ) {
    }

    public record SubmitAssignmentRequest(String content, FileUploadPayload attachment) {
    }

    public record FileUploadPayload(
            String fileName,
            String contentType,
            Long size,
            String base64Data
    ) {
    }

    public record GradeSubmissionRequest(Integer score, String feedback) {
    }

    public record AttendanceEntryRequest(String learnerId, AttendanceStatus status) {
    }

    public record RecordAttendanceRequest(LocalDate date, List<AttendanceEntryRequest> entries) {
    }

    public record CreateAnnouncementRequest(String classId, String title, String body) {
    }

    public record CreateMessageRequest(String classId, String parentId, String subject, String body) {
    }

    private record SchoolClass(
            String id,
            String schoolId,
            String name,
            String gradeLabel,
            String subject,
            String teacherId,
            List<String> learnerIds
    ) {
    }

    private record AssignmentTask(
            String id,
            String classId,
            String teacherId,
            String title,
            String description,
            LocalDate dueDate,
            LocalDateTime publishedAt,
            String status,
            FileAttachmentView attachment
    ) {
    }

    private record SubmissionRecord(
            String id,
            String assignmentId,
            String learnerId,
            String content,
            LocalDateTime submittedAt,
            Integer score,
            String feedback,
            String status,
            FileAttachmentView attachment
    ) {
    }

    public record DownloadedFile(
            String fileName,
            String contentType,
            byte[] data
    ) {
    }

    private record StoredAttachment(
            String fileName,
            String contentType,
            Long size,
            byte[] data
    ) {
        private static StoredAttachment empty() {
            return new StoredAttachment(null, null, null, null);
        }

        private static StoredAttachment fromEntity(String fileName, String contentType, Long size, byte[] data) {
            return new StoredAttachment(fileName, contentType, size, data);
        }

        private boolean isEmpty() {
            return fileName == null || contentType == null || size == null || data == null || data.length == 0;
        }
    }

    private record AttendanceRecord(
            String id,
            String classId,
            String teacherId,
            LocalDate date,
            Map<String, AttendanceStatus> entries,
            LocalDateTime recordedAt
    ) {
    }

    private record AnnouncementRecord(
            String id,
            String classId,
            String teacherId,
            String title,
            String body,
            LocalDateTime sentAt
    ) {
    }

    private record MessageRecord(
            String id,
            String classId,
            String teacherId,
            String parentId,
            String subject,
            String body,
            LocalDateTime sentAt
    ) {
    }
}
