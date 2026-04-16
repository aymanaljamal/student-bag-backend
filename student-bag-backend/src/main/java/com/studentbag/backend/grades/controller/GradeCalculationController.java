package com.studentbag.backend.grades.controller;

import com.studentbag.backend.domain.enums.grades.GradeCalculationSource;
import com.studentbag.backend.grades.dto.request.CreateGradeCalculationRequest;
import com.studentbag.backend.grades.dto.request.GradeCourseItemRequest;
import com.studentbag.backend.grades.dto.request.GradeWhatIfRequest;
import com.studentbag.backend.grades.dto.request.UpdateGradeCalculationRequest;
import com.studentbag.backend.grades.dto.response.GradeCalculationResponse;
import com.studentbag.backend.grades.dto.response.GradeInsightsResponse;
import com.studentbag.backend.grades.dto.response.GradeWhatIfResponse;
import com.studentbag.backend.grades.service.GradeCalculationService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades/calculations")
@RequiredArgsConstructor
@Tag(
        name = "Grades / GPA Calculations",
        description = "APIs for managing saved GPA calculations, insights, and what-if analysis"
)
@SecurityRequirement(name = "bearerAuth")
public class GradeCalculationController {

    private final GradeCalculationService gradeCalculationService;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @PostMapping("/manual")
    @Operation(
            summary = "Create manual grade calculation",
            description = "Creates a new grade calculation for the authenticated student using manually entered courses/items."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Calculation created successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> createManual(
            @Valid @RequestBody CreateGradeCalculationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        request.setSourceType(GradeCalculationSource.MANUAL);

        GradeCalculationResponse response =
                gradeCalculationService.createManual(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/from-active-schedule")
    @Operation(
            summary = "Create calculation from active schedule",
            description = "Creates a new grade calculation by importing unique courses from the authenticated student's active schedule."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Calculation created successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Student or active schedule not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> createFromActiveSchedule(
            @Valid @RequestBody CreateGradeCalculationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        request.setSourceType(GradeCalculationSource.ACTIVE_SCHEDULE);

        GradeCalculationResponse response =
                gradeCalculationService.createFromActiveSchedule(studentId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all calculations",
            description = "Returns all saved grade calculations for the authenticated student."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculations retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    public ResponseEntity<List<GradeCalculationResponse>> getMyCalculations(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(gradeCalculationService.getStudentCalculations(studentId));
    }

    @GetMapping("/{calculationId}")
    @Operation(
            summary = "Get one calculation",
            description = "Returns one saved grade calculation belonging to the authenticated student."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculation retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> getCalculation(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.getCalculation(calculationId, studentId)
        );
    }

    @PutMapping("/{calculationId}")
    @Operation(
            summary = "Update calculation",
            description = "Updates metadata and/or items of an existing saved grade calculation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculation updated successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> updateCalculation(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @Valid @RequestBody UpdateGradeCalculationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.updateCalculation(calculationId, studentId, request)
        );
    }

    @PostMapping("/{calculationId}/items")
    @Operation(
            summary = "Add item to calculation",
            description = "Adds one course/item to an existing calculation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation or course not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> addItem(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @Valid @RequestBody GradeCourseItemRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.addItem(calculationId, studentId, request)
        );
    }

    @DeleteMapping("/{calculationId}/items/{itemId}")
    @Operation(
            summary = "Delete item from calculation",
            description = "Deletes one item from an existing calculation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item deleted successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation or item not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> deleteItem(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @Parameter(description = "Item ID", example = "5")
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.deleteItem(calculationId, studentId, itemId)
        );
    }

    @PostMapping("/{calculationId}/recalculate")
    @Operation(
            summary = "Recalculate calculation",
            description = "Forces recalculation of GPA, percentage, credits, and related analysis."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calculation recalculated successfully",
                    content = @Content(schema = @Schema(implementation = GradeCalculationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation not found", content = @Content)
    })
    public ResponseEntity<GradeCalculationResponse> recalculate(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.recalculate(calculationId, studentId)
        );
    }

    @GetMapping("/{calculationId}/insights")
    @Operation(
            summary = "Get calculation insights",
            description = "Returns advice, weakest courses, highest impact courses, warnings, and summary for the calculation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Insights retrieved successfully",
                    content = @Content(schema = @Schema(implementation = GradeInsightsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation not found", content = @Content)
    })
    public ResponseEntity<GradeInsightsResponse> getInsights(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.getInsights(calculationId, studentId)
        );
    }

    @PostMapping("/{calculationId}/what-if")
    @Operation(
            summary = "Analyze what-if scenario",
            description = "Performs a what-if analysis for semester or cumulative target without saving the scenario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "What-if analysis completed successfully",
                    content = @Content(schema = @Schema(implementation = GradeWhatIfResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation not found", content = @Content)
    })
    public ResponseEntity<GradeWhatIfResponse> analyzeWhatIf(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @Valid @RequestBody GradeWhatIfRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        return ResponseEntity.ok(
                gradeCalculationService.analyzeWhatIf(calculationId, studentId, request)
        );
    }

    @DeleteMapping("/{calculationId}")
    @Operation(
            summary = "Delete calculation",
            description = "Deletes a saved grade calculation belonging to the authenticated student."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Calculation deleted successfully", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
            @ApiResponse(responseCode = "404", description = "Calculation not found", content = @Content)
    })
    public ResponseEntity<Void> deleteCalculation(
            @Parameter(description = "Calculation ID", example = "1")
            @PathVariable Long calculationId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getCurrentStudentId(userDetails);
        gradeCalculationService.deleteCalculation(calculationId, studentId);
        return ResponseEntity.noContent().build();
    }

    private Long getCurrentStudentId(UserDetails userDetails) {
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found for email: " + email));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found for user id: " + user.getId()));

        return student.getId();
    }
}