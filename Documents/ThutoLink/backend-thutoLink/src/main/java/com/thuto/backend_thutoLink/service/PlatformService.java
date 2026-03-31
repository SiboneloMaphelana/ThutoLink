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
            MessageRepository messageRepository
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
        Map<String, UserEntity> users = indexUsers();
        Map<String, LearnerProfileEntity> learners = indexLearners();
        Map<String, ClassEnrollmentEntity> enrollments = indexEnrollments();
        Map<String, SchoolClassEntity> classes = indexClasses();
        Map<String, AssignmentEntity> assignments = indexAssignments();
        Map<String, SubmissionEntity> submissions = indexSubmissions();
        Map<String, AttendanceEntity> attendance = indexAttendance();
        Map<String, AnnouncementEntity> announcements = indexAnnouncements();
        Map<String, MessageEntity> messages = indexMessages();

        SchoolEntity school = requiredSchool(currentUser.schoolId());

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
                .map(assignment -> toAssignmentView(assignment, currentUser, users, learners, classes, submissions))
                .toList();

        List<AttendanceView> scopedAttendance = attendance.values().stream()
                .map(this::toAttendanceRecord)
                .filter(record -> visibleClassIds.contains(record.classId()))
                .sorted(Comparator.comparing(AttendanceRecord::date).reversed())
                .map(record -> toAttendanceView(record, users, classes))
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

        List<AlertView> alerts = buildAlerts(currentUser, scopedAttendance);

        return new DashboardResponse(
                new UserSummary(currentUser.id(), currentUser.fullName(), currentUser.email(), currentUser.role().name()),
                new SchoolSummary(school.getId(), school.getName(), school.getDistrict()),
                buildStats(currentUser, scopedClasses, scopedAssignments, scopedAttendance, scopedMessages, alerts),
                scopedClasses,
                scopedAssignments,
                scopedAttendance,
                scopedAnnouncements,
                scopedMessages,
                alerts,
                demoCredentials()
        );
    }

    @Transactional
    public AssignmentView createAssignment(UserAccount currentUser, CreateAssignmentRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        SchoolClass schoolClass = requiredClass(request.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);

        AssignmentEntity entity = assignmentRepository.save(new AssignmentEntity(
                nextId("asn"),
                schoolClass.id(),
                currentUser.id(),
                request.title().trim(),
                request.description().trim(),
                request.dueDate(),
                LocalDateTime.now(),
                "Published"
        ));

        return toAssignmentView(
                toAssignmentTask(entity),
                currentUser,
                indexUsers(),
                indexLearners(),
                indexClasses(),
                indexSubmissions()
        );
    }

    @Transactional
    public SubmissionView submitAssignment(UserAccount currentUser, String assignmentId, SubmitAssignmentRequest request) {
        assertRole(currentUser, UserRole.LEARNER);
        AssignmentTask assignment = requiredAssignment(assignmentId);
        assertLearnerCanAccessAssignment(currentUser, assignment);

        SubmissionEntity existing = submissionRepository.findByAssignmentIdAndLearnerId(assignmentId, currentUser.id()).orElse(null);

        SubmissionEntity submission = submissionRepository.save(new SubmissionEntity(
                existing != null ? existing.getId() : nextId("sub"),
                assignmentId,
                currentUser.id(),
                request.content().trim(),
                LocalDateTime.now(),
                existing != null ? existing.getScore() : null,
                existing != null ? existing.getFeedback() : "Awaiting teacher review.",
                existing != null && existing.getScore() != null ? "Resubmitted" : "Submitted"
        ));
        return toSubmissionView(toSubmissionRecord(submission), indexUsers());
    }

    @Transactional
    public SubmissionView gradeSubmission(UserAccount currentUser, String submissionId, GradeSubmissionRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        SubmissionEntity existing = requiredSubmissionEntity(submissionId);
        AssignmentTask assignment = requiredAssignment(existing.getAssignmentId());
        SchoolClass schoolClass = requiredClass(assignment.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);

        SubmissionEntity graded = submissionRepository.save(new SubmissionEntity(
                existing.getId(),
                existing.getAssignmentId(),
                existing.getLearnerId(),
                existing.getContent(),
                existing.getSubmittedAt(),
                request.score(),
                request.feedback().trim(),
                "Reviewed"
        ));
        return toSubmissionView(toSubmissionRecord(graded), indexUsers());
    }

    @Transactional
    public AttendanceView recordAttendance(UserAccount currentUser, String classId, RecordAttendanceRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        SchoolClass schoolClass = requiredClass(classId);
        assertTeacherOwnsClass(currentUser, schoolClass);

        AttendanceEntity record = new AttendanceEntity(nextId("att"), classId, currentUser.id(), request.date(), LocalDateTime.now());
        for (AttendanceEntryRequest entry : request.entries()) {
            if (!schoolClass.learnerIds().contains(entry.learnerId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Attendance includes a learner outside this class.");
            }
            record.addEntry(new AttendanceEntryEntity(nextId("att-entry"), entry.learnerId(), entry.status()));
        }

        AttendanceEntity saved = attendanceRepository.save(record);
        return toAttendanceView(toAttendanceRecord(saved), indexUsers(), indexClasses());
    }

    @Transactional
    public AnnouncementView createAnnouncement(UserAccount currentUser, CreateAnnouncementRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        SchoolClass schoolClass = requiredClass(request.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);

        AnnouncementEntity saved = announcementRepository.save(new AnnouncementEntity(
                nextId("ann"),
                schoolClass.id(),
                currentUser.id(),
                request.title().trim(),
                request.body().trim(),
                LocalDateTime.now()
        ));
        return toAnnouncementView(toAnnouncementRecord(saved), indexUsers(), indexClasses());
    }

    @Transactional
    public MessageView createMessage(UserAccount currentUser, CreateMessageRequest request) {
        assertRole(currentUser, UserRole.TEACHER);
        SchoolClass schoolClass = requiredClass(request.classId());
        assertTeacherOwnsClass(currentUser, schoolClass);

        UserAccount parent = requiredUser(request.parentId());
        if (parent.role() != UserRole.PARENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Messages can only be sent to parents.");
        }

        boolean parentLinked = parent.learnerIds().stream().anyMatch(schoolClass.learnerIds()::contains);
        if (!parentLinked) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Parent is not linked to a learner in this class.");
        }

        MessageEntity saved = messageRepository.save(new MessageEntity(
                nextId("msg"),
                schoolClass.id(),
                currentUser.id(),
                parent.id(),
                request.subject().trim(),
                request.body().trim(),
                LocalDateTime.now()
        ));
        return toMessageView(toMessageRecord(saved), indexUsers(), indexClasses());
    }

    private List<StatCardView> buildStats(
            UserAccount currentUser,
            List<ClassView> scopedClasses,
            List<AssignmentView> scopedAssignments,
            List<AttendanceView> scopedAttendance,
            List<MessageView> scopedMessages,
            List<AlertView> alerts
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
                    new StatCardView("Parent Reach-outs", String.valueOf(scopedMessages.size()), "Structured parent communication sent")
            );
        }

        if (currentUser.role() == UserRole.PARENT) {
            long absentDays = scopedAttendance.stream()
                    .flatMap(view -> view.entries().stream())
                    .filter(entry -> "ABSENT".equals(entry.status()))
                    .count();
            return List.of(
                    new StatCardView("Linked Learners", String.valueOf(currentUser.learnerIds().size()), "Children connected to your account"),
                    new StatCardView("Attendance Alerts", String.valueOf(alerts.size()), "Recent absences or lateness"),
                    new StatCardView("Absence Records", String.valueOf(absentDays), "Logged across recent registers"),
                    new StatCardView("Teacher Messages", String.valueOf(scopedMessages.size()), "Updates that need your attention")
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
                    new StatCardView("Announcements", String.valueOf(scopedMessages.size() + alerts.size()), "Messages and attendance notices")
            );
        }

        long totalLearners = learnerProfileRepository.count();
        long totalParents = userRepository.countByRole(UserRole.PARENT);
        return List.of(
                new StatCardView("Schools", String.valueOf(schoolRepository.count()), "Configured for the demo"),
                new StatCardView("Classes", String.valueOf(schoolClassRepository.count()), "Structured enrolment groups"),
                new StatCardView("Learners", String.valueOf(totalLearners), "Tracked across all classes"),
                new StatCardView("Parents", String.valueOf(totalParents), "Linked to learner accounts")
        );
    }

    private List<AlertView> buildAlerts(UserAccount currentUser, List<AttendanceView> scopedAttendance) {
        List<AlertView> alerts = new ArrayList<>();
        for (AttendanceView record : scopedAttendance) {
            for (AttendanceEntryView entry : record.entries()) {
                if ("ABSENT".equals(entry.status()) || "LATE".equals(entry.status())) {
                    if (currentUser.role() == UserRole.PARENT && !currentUser.learnerIds().contains(entry.learner().id())) {
                        continue;
                    }
                    if (currentUser.role() == UserRole.LEARNER && !currentUser.id().equals(entry.learner().id())) {
                        continue;
                    }
                    alerts.add(new AlertView(
                            "Attendance " + entry.status().toLowerCase(Locale.ROOT),
                            entry.learner().fullName() + " was marked " + entry.status().toLowerCase(Locale.ROOT) + " in " + record.className(),
                            record.date().toString()
                    ));
                }
            }
        }
        return alerts;
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
        SchoolClass schoolClass = requiredClass(assignment.classId());
        if (!schoolClass.learnerIds().contains(currentUser.id())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This assignment is not assigned to you.");
        }
    }

    private SchoolEntity requiredSchool(String schoolId) {
        return schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "School not found."));
    }

    private SchoolClass requiredClass(String classId) {
        return schoolClassRepository.findById(classId)
                .map(entity -> toSchoolClass(entity, indexEnrollments()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Class not found."));
    }

    private AssignmentTask requiredAssignment(String assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .map(this::toAssignmentTask)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assignment not found."));
    }

    private SubmissionEntity requiredSubmissionEntity(String submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Submission not found."));
    }

    private UserAccount requiredUser(String userId) {
        return userRepository.findById(userId)
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
            Map<String, SubmissionEntity> submissions
    ) {
        SchoolClass schoolClass = toSchoolClass(requiredClassEntity(classes, assignment.classId()), indexEnrollments());
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
                submission.status()
        );
    }

    private AttendanceView toAttendanceView(AttendanceRecord record, Map<String, UserEntity> users, Map<String, SchoolClassEntity> classes) {
        SchoolClass schoolClass = toSchoolClass(requiredClassEntity(classes, record.classId()), indexEnrollments());
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
                ? learnerProfileRepository.findByParentId(user.getId()).stream().map(LearnerProfileEntity::getId).toList()
                : List.of();

        List<String> classIds;
        if (user.getRole() == UserRole.TEACHER) {
            classIds = schoolClassRepository.findAll().stream()
                    .filter(clazz -> clazz.getTeacherId().equals(user.getId()))
                    .map(SchoolClassEntity::getId)
                    .toList();
        } else if (user.getRole() == UserRole.LEARNER) {
            classIds = classEnrollmentRepository.findByLearnerId(user.getId()).stream()
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
                entity.getStatus()
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
                entity.getStatus()
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

    private Map<String, UserEntity> indexUsers() {
        return userRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(UserEntity::getId, user -> user));
    }

    private Map<String, LearnerProfileEntity> indexLearners() {
        return learnerProfileRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(LearnerProfileEntity::getId, learner -> learner));
    }

    private Map<String, ClassEnrollmentEntity> indexEnrollments() {
        return classEnrollmentRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(ClassEnrollmentEntity::getId, enrollment -> enrollment));
    }

    private Map<String, SchoolClassEntity> indexClasses() {
        return schoolClassRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(SchoolClassEntity::getId, clazz -> clazz));
    }

    private Map<String, AssignmentEntity> indexAssignments() {
        return assignmentRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(AssignmentEntity::getId, assignment -> assignment));
    }

    private Map<String, SubmissionEntity> indexSubmissions() {
        return submissionRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(SubmissionEntity::getId, submission -> submission));
    }

    private Map<String, AttendanceEntity> indexAttendance() {
        return attendanceRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(AttendanceEntity::getId, record -> record));
    }

    private Map<String, AnnouncementEntity> indexAnnouncements() {
        return announcementRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(AnnouncementEntity::getId, announcement -> announcement));
    }

    private Map<String, MessageEntity> indexMessages() {
        return messageRepository.findAll().stream().collect(java.util.stream.Collectors.toMap(MessageEntity::getId, message -> message));
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
            String status
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

    public record AlertView(String title, String body, String date) {
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
            List<AlertView> alerts,
            List<DemoCredential> demoCredentials
    ) {
    }

    public record CreateAssignmentRequest(String classId, String title, String description, LocalDate dueDate) {
    }

    public record SubmitAssignmentRequest(String content) {
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
            String status
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
            String status
    ) {
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
