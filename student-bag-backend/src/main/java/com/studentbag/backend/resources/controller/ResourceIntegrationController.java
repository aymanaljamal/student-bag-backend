package com.studentbag.backend.resources.controller;

import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.resources.dto.response.LinkedNoteSummaryResponse;
import com.studentbag.backend.resources.dto.response.LinkedTaskSummaryResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderDetailsResponse;
import com.studentbag.backend.resources.dto.response.ResourceCourseSummaryResponse;
import com.studentbag.backend.resources.service.ResourceIntegrationService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for integration endpoints between Resource Hub and:
 * <ul>
 *     <li>Active schedule</li>
 *     <li>Notes</li>
 *     <li>Tasks</li>
 *     <li>Course-linked folder details</li>
 * </ul>
 *
 * <p>This controller is useful for dedicated screens that need course context
 * without going through the full personal library workflow.</p>
 */
@RestController
@RequestMapping("/api/resources/integration")
@RequiredArgsConstructor
public class ResourceIntegrationController {

    private final ResourceIntegrationService resourceIntegrationService;
    private final UserRepository userRepository;
    private final TermRepository termRepository;

    /**
     * Resolves the current authenticated user UUID from JWT email.
     */
    private UUID getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return user.getId();
    }

    /**
     * Resolves the current academic term from DB.
     */
    private Long getCurrentTermId() {
        return termRepository.findAll()
                .stream()
                .filter(term -> Boolean.TRUE.equals(term.getIsCurrent()))
                .map(Term::getId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Current term not found"));
    }

    /**
     * Returns active schedule courses for the current student.
     *
     * <p>If termId is provided, it uses it.
     * Otherwise, it uses the current term where is_current = true.</p>
     */
    @GetMapping("/active-courses")
    public ResponseEntity<List<ResourceCourseSummaryResponse>> getActiveScheduleCoursesForLibrary(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long termId
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        Long resolvedTermId = termId != null ? termId : getCurrentTermId();

        return ResponseEntity.ok(
                resourceIntegrationService.getActiveScheduleCoursesForLibrary(
                        currentUserId,
                        resolvedTermId
                )
        );
    }

    /**
     * Returns non-deleted and non-archived notes linked to the specified course.
     */
    @GetMapping("/course/{courseId}/notes")
    public ResponseEntity<List<LinkedNoteSummaryResponse>> getLinkedNotesByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                resourceIntegrationService.getLinkedNotesByCourse(currentUserId, courseId)
        );
    }

    /**
     * Returns non-deleted, non-archived, non-completed tasks linked to the specified course.
     */
    @GetMapping("/course/{courseId}/tasks")
    public ResponseEntity<List<LinkedTaskSummaryResponse>> getLinkedTasksByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                resourceIntegrationService.getLinkedTasksByCourse(currentUserId, courseId)
        );
    }

    /**
     * Returns full course-folder details payload:
     * folder, children, items, linked notes, linked tasks.
     */
    @GetMapping("/folder/{folderId}/details")
    public ResponseEntity<PersonalResourceFolderDetailsResponse> buildCourseFolderDetails(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        return ResponseEntity.ok(
                resourceIntegrationService.buildCourseFolderDetails(folderId, currentUserId)
        );
    }
}