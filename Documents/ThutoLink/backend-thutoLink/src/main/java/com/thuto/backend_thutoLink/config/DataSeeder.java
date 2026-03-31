package com.thuto.backend_thutoLink.config;

import com.thuto.backend_thutoLink.model.AttendanceStatus;
import com.thuto.backend_thutoLink.model.UserRole;
import com.thuto.backend_thutoLink.persistence.entity.*;
import com.thuto.backend_thutoLink.persistence.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            MessageRepository messageRepository
    ) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            SchoolEntity school = new SchoolEntity("school-1", "ThutoLink Academy", "Johannesburg South");
            schoolRepository.save(school);

            userRepository.saveAll(List.of(
                    new UserEntity("admin-1", "Lerato Mokoena", "principal@thutolink.school", "admin123", UserRole.ADMIN, school.getId()),
                    new UserEntity("teacher-1", "Sibusiso Nkosi", "teacher.nkosi@thutolink.school", "teacher123", UserRole.TEACHER, school.getId()),
                    new UserEntity("parent-1", "Nomsa Dlamini", "parent.dlamini@thutolink.school", "parent123", UserRole.PARENT, school.getId()),
                    new UserEntity("parent-2", "Themba Khumalo", "parent.khumalo@thutolink.school", "parent123", UserRole.PARENT, school.getId()),
                    new UserEntity("learner-1", "Amahle Dlamini", "amahle@thutolink.school", "learner123", UserRole.LEARNER, school.getId()),
                    new UserEntity("learner-2", "Lethabo Khumalo", "lethabo@thutolink.school", "learner123", UserRole.LEARNER, school.getId())
            ));

            learnerProfileRepository.saveAll(List.of(
                    new LearnerProfileEntity("learner-1", "Amahle Dlamini", "Grade 6", "class-1", "parent-1"),
                    new LearnerProfileEntity("learner-2", "Lethabo Khumalo", "Grade 6", "class-1", "parent-2")
            ));

            classEnrollmentRepository.saveAll(List.of(
                    new ClassEnrollmentEntity("enr-1", "learner-1", "class-1"),
                    new ClassEnrollmentEntity("enr-2", "learner-2", "class-1"),
                    new ClassEnrollmentEntity("enr-3", "learner-2", "class-2")
            ));

            schoolClassRepository.saveAll(List.of(
                    new SchoolClassEntity("class-1", school.getId(), "Grade 6 Mathematics", "Grade 6", "Mathematics", "teacher-1"),
                    new SchoolClassEntity("class-2", school.getId(), "Grade 6 Natural Sciences", "Grade 6", "Natural Sciences", "teacher-1")
            ));

            assignmentRepository.saveAll(List.of(
                    new AssignmentEntity("asn-1", "class-1", "teacher-1", "Fractions Practice Set", "Complete the worksheet on equivalent fractions and upload your worked solutions.", LocalDate.now().plusDays(3), LocalDateTime.now().minusDays(2), "Published"),
                    new AssignmentEntity("asn-2", "class-2", "teacher-1", "Ecosystems Reflection", "Write a short paragraph explaining the role of decomposers in an ecosystem.", LocalDate.now().plusDays(5), LocalDateTime.now().minusDays(1), "Published")
            ));

            submissionRepository.save(
                    new SubmissionEntity("sub-1", "asn-1", "learner-2", "I solved the fractions using number lines and simplified each answer.", LocalDateTime.now().minusHours(15), 82, "Clear method. Double-check question 4 next time.", "Reviewed")
            );

            AttendanceEntity attendanceOne = new AttendanceEntity("att-1", "class-1", "teacher-1", LocalDate.now().minusDays(1), LocalDateTime.now().minusDays(1));
            attendanceOne.addEntry(new AttendanceEntryEntity("att-entry-1", "learner-1", AttendanceStatus.ABSENT));
            attendanceOne.addEntry(new AttendanceEntryEntity("att-entry-2", "learner-2", AttendanceStatus.PRESENT));

            AttendanceEntity attendanceTwo = new AttendanceEntity("att-2", "class-2", "teacher-1", LocalDate.now().minusDays(2), LocalDateTime.now().minusDays(2));
            attendanceTwo.addEntry(new AttendanceEntryEntity("att-entry-3", "learner-2", AttendanceStatus.LATE));

            attendanceRepository.saveAll(List.of(attendanceOne, attendanceTwo));

            announcementRepository.save(
                    new AnnouncementEntity("ann-1", "class-1", "teacher-1", "Friday revision pack", "Please remind learners to bring their completed revision pack on Friday for peer review.", LocalDateTime.now().minusHours(30))
            );

            messageRepository.save(
                    new MessageEntity("msg-1", "class-1", "teacher-1", "parent-1", "Amahle's attendance follow-up", "Amahle missed yesterday's lesson. Please let me know if she needs support catching up with the fractions work.", LocalDateTime.now().minusHours(20))
            );
        };
    }
}
