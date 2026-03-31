package com.thuto.backend_thutoLink.config;

import com.thuto.backend_thutoLink.model.AttendanceStatus;
import com.thuto.backend_thutoLink.model.UserRole;
import com.thuto.backend_thutoLink.persistence.entity.*;
import com.thuto.backend_thutoLink.persistence.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {
    @Bean
    CommandLineRunner seedDemoData(
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
            NotificationRepository notificationRepository
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();

            SchoolEntity school = new SchoolEntity("school-1", "ThutoLink Academy", "Johannesburg South");
            schoolRepository.save(school);

            userRepository.saveAll(List.of(
                    new UserEntity("admin-1", "Lerato Mokoena", "principal@thutolink.school", "admin123", UserRole.ADMIN, school.getId()),
                    new UserEntity("teacher-1", "Sibusiso Nkosi", "teacher.nkosi@thutolink.school", "teacher123", UserRole.TEACHER, school.getId()),
                    new UserEntity("parent-1", "Nomsa Dlamini", "parent.dlamini@thutolink.school", "parent123", UserRole.PARENT, school.getId()),
                    new UserEntity("parent-2", "Themba Khumalo", "parent.khumalo@thutolink.school", "parent123", UserRole.PARENT, school.getId()),
                    new UserEntity("learner-1", "Amahle Dlamini", "amahle@thutolink.school", "learner123", UserRole.LEARNER, school.getId()),
                    new UserEntity("learner-2", "Lethabo Khumalo", "lethabo@thutolink.school", "learner123", UserRole.LEARNER, school.getId()),
                    new UserEntity("learner-3", "Thando Dlamini", "thando@thutolink.school", "learner123", UserRole.LEARNER, school.getId()),
                    new UserEntity("learner-4", "Karabo Khumalo", "karabo@thutolink.school", "learner123", UserRole.LEARNER, school.getId())
            ));

            learnerProfileRepository.saveAll(List.of(
                    new LearnerProfileEntity("learner-1", school.getId(), "Amahle Dlamini", "Grade 6", "class-1", "parent-1"),
                    new LearnerProfileEntity("learner-2", school.getId(), "Lethabo Khumalo", "Grade 6", "class-1", "parent-2"),
                    new LearnerProfileEntity("learner-3", school.getId(), "Thando Dlamini", "Grade 6", "class-1", "parent-1"),
                    new LearnerProfileEntity("learner-4", school.getId(), "Karabo Khumalo", "Grade 6", "class-2", "parent-2")
            ));

            classEnrollmentRepository.saveAll(List.of(
                    new ClassEnrollmentEntity("enr-1", school.getId(), "learner-1", "class-1"),
                    new ClassEnrollmentEntity("enr-2", school.getId(), "learner-2", "class-1"),
                    new ClassEnrollmentEntity("enr-3", school.getId(), "learner-2", "class-2"),
                    new ClassEnrollmentEntity("enr-4", school.getId(), "learner-1", "class-2"),
                    new ClassEnrollmentEntity("enr-5", school.getId(), "learner-1", "class-3"),
                    new ClassEnrollmentEntity("enr-6", school.getId(), "learner-3", "class-1"),
                    new ClassEnrollmentEntity("enr-7", school.getId(), "learner-3", "class-3"),
                    new ClassEnrollmentEntity("enr-8", school.getId(), "learner-4", "class-2"),
                    new ClassEnrollmentEntity("enr-9", school.getId(), "learner-4", "class-3")
            ));

            schoolClassRepository.saveAll(List.of(
                    new SchoolClassEntity("class-1", school.getId(), "Grade 6 Mathematics", "Grade 6", "Mathematics", "teacher-1"),
                    new SchoolClassEntity("class-2", school.getId(), "Grade 6 Natural Sciences", "Grade 6", "Natural Sciences", "teacher-1"),
                    new SchoolClassEntity("class-3", school.getId(), "Grade 6 English Home Language", "Grade 6", "English Home Language", "teacher-1")
            ));

            assignmentRepository.saveAll(List.of(
                    new AssignmentEntity("asn-1", school.getId(), "class-1", "teacher-1", "Fractions Practice Set", "Complete the worksheet on equivalent fractions and upload your worked solutions.", today.minusDays(5), now.minusDays(12), "Published", "fractions-practice-pack.pdf", "application/pdf", 96L, samplePdf("Fractions Practice Pack")),
                    new AssignmentEntity("asn-2", school.getId(), "class-2", "teacher-1", "Ecosystems Reflection", "Write a short paragraph explaining the role of decomposers in an ecosystem and include one local example.", today.minusDays(2), now.minusDays(9), "Published", null, null, null, null),
                    new AssignmentEntity("asn-3", school.getId(), "class-3", "teacher-1", "Character Diary Entry", "Write a diary entry from the perspective of the main character using evidence from the text.", today.plusDays(2), now.minusDays(4), "Published", "character-journal-brief.pdf", "application/pdf", 97L, samplePdf("Character Journal Brief")),
                    new AssignmentEntity("asn-4", school.getId(), "class-1", "teacher-1", "Decimal Word Problems", "Solve the five decimal word problems and show all working clearly in your submission.", today.plusDays(6), now.minusDays(1), "Published", null, null, null, null)
            ));

            submissionRepository.saveAll(List.of(
                    new SubmissionEntity("sub-1", school.getId(), "asn-1", "learner-1", "I completed the worksheet and checked my answers using equivalent fraction models.", now.minusDays(7), 91, "Excellent accuracy and neat working. Keep explaining your method like this.", "Reviewed", "amahle-fractions-solutions.pdf", "application/pdf", 91L, samplePdf("Amahle Fractions Solutions")),
                    new SubmissionEntity("sub-2", school.getId(), "asn-1", "learner-2", "I solved the fractions using number lines and simplified each answer.", now.minusDays(6).minusHours(3), 76, "A solid attempt. Revisit question 4 and 7 where the denominators changed.", "Reviewed", null, null, null, null),
                    new SubmissionEntity("sub-3", school.getId(), "asn-1", "learner-3", "I used pictures to compare the fractions and wrote short explanations for each answer.", now.minusDays(6).minusHours(1), null, "Awaiting teacher review.", "Submitted", "thando-fractions-worksheet.jpg", "image/jpeg", 72L, sampleImage("Thando Fractions Worksheet")),
                    new SubmissionEntity("sub-4", school.getId(), "asn-2", "learner-1", "Decomposers break down dead plants and animals and return nutrients to the soil.", now.minusDays(3).minusHours(5), 68, "Good science ideas. Next time include a more detailed example from our class notes.", "Reviewed", null, null, null, null),
                    new SubmissionEntity("sub-5", school.getId(), "asn-2", "learner-2", "Without decomposers, waste would build up and plants would not get enough nutrients back.", now.minusDays(3), 88, "Strong explanation and good use of vocabulary from the lesson.", "Reviewed", "lethabo-ecosystems-notes.pdf", "application/pdf", 89L, samplePdf("Lethabo Ecosystems Notes")),
                    new SubmissionEntity("sub-6", school.getId(), "asn-2", "learner-4", "I added a second paragraph after revising my first answer with the feedback from class.", now.minusDays(2).minusHours(10), null, "Awaiting teacher review.", "Resubmitted", null, null, null, null),
                    new SubmissionEntity("sub-7", school.getId(), "asn-3", "learner-1", "I wrote the diary entry as if the character had just returned home after the argument.", now.minusHours(30), null, "Awaiting teacher review.", "Submitted", null, null, null, null),
                    new SubmissionEntity("sub-8", school.getId(), "asn-3", "learner-3", "My diary entry shows how the character changed from the beginning of the chapter to the end.", now.minusHours(26), null, "Awaiting teacher review.", "Submitted", "thando-diary-draft.pdf", "application/pdf", 84L, samplePdf("Thando Diary Draft"))
            ));

            AttendanceEntity attendanceOne = new AttendanceEntity("att-1", school.getId(), "class-1", "teacher-1", today.minusDays(1), now.minusDays(1).minusHours(2));
            attendanceOne.addEntry(new AttendanceEntryEntity("att-entry-1", "learner-1", AttendanceStatus.LATE));
            attendanceOne.addEntry(new AttendanceEntryEntity("att-entry-2", "learner-2", AttendanceStatus.PRESENT));
            attendanceOne.addEntry(new AttendanceEntryEntity("att-entry-3", "learner-3", AttendanceStatus.ABSENT));

            AttendanceEntity attendanceTwo = new AttendanceEntity("att-2", school.getId(), "class-2", "teacher-1", today.minusDays(2), now.minusDays(2).minusHours(1));
            attendanceTwo.addEntry(new AttendanceEntryEntity("att-entry-4", "learner-1", AttendanceStatus.PRESENT));
            attendanceTwo.addEntry(new AttendanceEntryEntity("att-entry-5", "learner-2", AttendanceStatus.LATE));
            attendanceTwo.addEntry(new AttendanceEntryEntity("att-entry-6", "learner-4", AttendanceStatus.PRESENT));

            AttendanceEntity attendanceThree = new AttendanceEntity("att-3", school.getId(), "class-3", "teacher-1", today.minusDays(4), now.minusDays(4).minusHours(2));
            attendanceThree.addEntry(new AttendanceEntryEntity("att-entry-7", "learner-1", AttendanceStatus.PRESENT));
            attendanceThree.addEntry(new AttendanceEntryEntity("att-entry-8", "learner-3", AttendanceStatus.PRESENT));
            attendanceThree.addEntry(new AttendanceEntryEntity("att-entry-9", "learner-4", AttendanceStatus.ABSENT));

            AttendanceEntity attendanceFour = new AttendanceEntity("att-4", school.getId(), "class-1", "teacher-1", today.minusDays(6), now.minusDays(6).minusHours(3));
            attendanceFour.addEntry(new AttendanceEntryEntity("att-entry-10", "learner-1", AttendanceStatus.ABSENT));
            attendanceFour.addEntry(new AttendanceEntryEntity("att-entry-11", "learner-2", AttendanceStatus.PRESENT));
            attendanceFour.addEntry(new AttendanceEntryEntity("att-entry-12", "learner-3", AttendanceStatus.PRESENT));

            attendanceRepository.saveAll(List.of(attendanceOne, attendanceTwo, attendanceThree, attendanceFour));

            announcementRepository.saveAll(List.of(
                    new AnnouncementEntity("ann-1", school.getId(), "class-1", "teacher-1", "Friday revision pack", "Please remind learners to bring their completed revision pack on Friday for peer review.", now.minusDays(5).minusHours(4)),
                    new AnnouncementEntity("ann-2", school.getId(), "class-2", "teacher-1", "Science project materials", "Learners can bring recycled packaging on Thursday for the ecosystems mini-project.", now.minusDays(3).minusHours(2)),
                    new AnnouncementEntity("ann-3", school.getId(), "class-3", "teacher-1", "Reading circle preparation", "Please read chapter 6 tonight and arrive ready to discuss the narrator's point of view.", now.minusHours(18))
            ));

            messageRepository.saveAll(List.of(
                    new MessageEntity("msg-1", school.getId(), "class-1", "teacher-1", "parent-1", "Amahle's attendance follow-up", "Amahle missed Monday's mathematics lesson. I have shared the fractions catch-up sheet and can support her at break if needed.", now.minusDays(5).minusHours(1)),
                    new MessageEntity("msg-2", school.getId(), "class-3", "teacher-1", "parent-1", "Thando's writing progress", "Thando is contributing thoughtful ideas in English. I would like him to add more detail when explaining character motivation.", now.minusDays(2).minusHours(6)),
                    new MessageEntity("msg-3", school.getId(), "class-2", "teacher-1", "parent-2", "Lethabo's science reflection", "Lethabo showed strong understanding in Natural Sciences. Please encourage him to keep expanding his examples in written work.", now.minusDays(1).minusHours(4))
            ));

            notificationRepository.saveAll(List.of(
                    new NotificationEntity("notif-1", school.getId(), "learner-1", "ASSIGNMENT_GRADED", "Assignment graded", "Your fractions practice set was graded with score 91.", now.minusDays(6), now.minusDays(5)),
                    new NotificationEntity("notif-2", school.getId(), "parent-1", "ASSIGNMENT_GRADED", "Assignment graded", "Amahle Dlamini's fractions practice set was graded with score 91.", now.minusDays(6), null),
                    new NotificationEntity("notif-3", school.getId(), "parent-1", "ATTENDANCE_FLAGGED", "Attendance update", "Amahle Dlamini was marked absent on " + today.minusDays(6) + ".", now.minusDays(6).plusHours(1), null),
                    new NotificationEntity("notif-4", school.getId(), "learner-3", "ANNOUNCEMENT_POSTED", "Reading circle preparation", "Please read chapter 6 tonight and arrive ready to discuss the narrator's point of view.", now.minusHours(18), null),
                    new NotificationEntity("notif-5", school.getId(), "parent-2", "DIRECT_MESSAGE", "Lethabo's science reflection", "Lethabo showed strong understanding in Natural Sciences. Please encourage him to keep expanding his examples in written work.", now.minusDays(1).minusHours(4), null),
                    new NotificationEntity("notif-6", school.getId(), "learner-2", "ASSIGNMENT_PUBLISHED", "New assignment posted", "Decimal Word Problems is now available in your class dashboard.", now.minusHours(22), null)
            ));
        };
    }

    private static byte[] samplePdf(String title) {
        return ("%PDF-1.4\n1 0 obj<</Type/Catalog>>endobj\n% " + title + "\n").getBytes(StandardCharsets.UTF_8);
    }

    private static byte[] sampleImage(String title) {
        return ("IMG:" + title).getBytes(StandardCharsets.UTF_8);
    }
}
