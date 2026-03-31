package com.thuto.backend_thutoLink.api;

import com.thuto.backend_thutoLink.auth.AuthenticatedUser;
import com.thuto.backend_thutoLink.service.PlatformService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PlatformController {
    private final PlatformService platformService;

    public PlatformController(PlatformService platformService) {
        this.platformService = platformService;
    }

    @GetMapping("/dashboard")
    public PlatformService.DashboardResponse dashboard(@AuthenticationPrincipal AuthenticatedUser user) {
        return platformService.dashboardFor(user.account());
    }

    @PostMapping("/assignments")
    public PlatformService.AssignmentView createAssignment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody PlatformService.CreateAssignmentRequest request
    ) {
        return platformService.createAssignment(user.account(), request);
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public PlatformService.SubmissionView submitAssignment(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String assignmentId,
            @RequestBody PlatformService.SubmitAssignmentRequest request
    ) {
        return platformService.submitAssignment(user.account(), assignmentId, request);
    }

    @PostMapping("/submissions/{submissionId}/grade")
    public PlatformService.SubmissionView gradeSubmission(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String submissionId,
            @RequestBody PlatformService.GradeSubmissionRequest request
    ) {
        return platformService.gradeSubmission(user.account(), submissionId, request);
    }

    @PostMapping("/classes/{classId}/attendance")
    public PlatformService.AttendanceView recordAttendance(
            @AuthenticationPrincipal AuthenticatedUser user,
            @PathVariable String classId,
            @RequestBody PlatformService.RecordAttendanceRequest request
    ) {
        return platformService.recordAttendance(user.account(), classId, request);
    }

    @PostMapping("/announcements")
    public PlatformService.AnnouncementView createAnnouncement(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody PlatformService.CreateAnnouncementRequest request
    ) {
        return platformService.createAnnouncement(user.account(), request);
    }

    @PostMapping("/messages")
    public PlatformService.MessageView createMessage(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody PlatformService.CreateMessageRequest request
    ) {
        return platformService.createMessage(user.account(), request);
    }
}
