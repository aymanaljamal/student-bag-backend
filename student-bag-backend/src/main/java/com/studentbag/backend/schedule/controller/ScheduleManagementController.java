package com.studentbag.backend.schedule.controller;

import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.schedule.dto.request.UpdateScheduleRequest;
import com.studentbag.backend.schedule.dto.response.ActiveScheduleCourseDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleViewerResponseDTO;
import com.studentbag.backend.schedule.dto.response.UpdateScheduleResponseDTO;
import com.studentbag.backend.schedule.service.ScheduleManagementService;
import com.studentbag.backend.schedule.service.ScheduleViewerService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleManagementController {

    private final ScheduleManagementService managementService;
    private final ScheduleViewerService viewerService;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TermRepository termRepository;

    private Long getCurrentStudentId(UserDetails userDetails) {
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        UUID userId = user.getId();

        Student student = studentRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Student not found for user id: " + userId));

        return student.getId();
    }

    private Long getCurrentTermId() {
        return termRepository.findAll()
                .stream()
                .filter(term -> Boolean.TRUE.equals(term.getIsCurrent()))
                .map(Term::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current term not found"));
    }

    @GetMapping("/viewer")
    public ResponseEntity<List<StudentScheduleViewerResponseDTO>> getAll(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(viewerService.getStudentSchedulesViewer(studentId));
    }

    @PostMapping("/{scheduleId}/activate")
    public ResponseEntity<StudentScheduleViewerResponseDTO> activate(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        managementService.activateSchedule(scheduleId, studentId);
        return ResponseEntity.ok(viewerService.getScheduleViewer(scheduleId, studentId));
    }

    @PostMapping("/{scheduleId}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        managementService.archiveSchedule(scheduleId, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        managementService.deleteSchedule(scheduleId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{scheduleId}/entries")
    public ResponseEntity<UpdateScheduleResponseDTO> updateEntries(
            @PathVariable Long scheduleId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateScheduleRequest request
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                managementService.updateScheduleEntries(scheduleId, studentId, request)
        );
    }

    /**
     * Old endpoint:
     * Uses a termId sent from Flutter.
     */
    @GetMapping("/term/{termId}/active/courses")
    public ResponseEntity<List<ActiveScheduleCourseDTO>> getActiveScheduleCourses(
            @PathVariable Long termId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);

        return ResponseEntity.ok(
                managementService.getActiveScheduleCourses(studentId, termId)
        );
    }

    /**
     * New endpoint:
     * Uses the current term from DB where is_current = true.
     */
    @GetMapping("/current/active/courses")
    public ResponseEntity<List<ActiveScheduleCourseDTO>> getCurrentTermActiveScheduleCourses(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        Long currentTermId = getCurrentTermId();

        return ResponseEntity.ok(
                managementService.getActiveScheduleCourses(studentId, currentTermId)
        );
    }
}