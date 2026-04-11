package com.studentbag.backend.tasks.controller;

import com.studentbag.backend.tasks.dto.request.*;
import com.studentbag.backend.tasks.dto.response.*;
import com.studentbag.backend.tasks.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "APIs for managing student tasks, labels, reminders, attachments, and search")
public class TaskController {

    private final TaskService taskService;

    // -------------------------------------------------------------------------
    // Core Task Operations
    // -------------------------------------------------------------------------

    @PostMapping
    @Operation(
            summary = "Create task",
            description = "Create a new task for the authenticated student."
    )
    public ResponseEntity<TaskResponse> createTask(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{taskId}")
    @Operation(
            summary = "Update task",
            description = "Update an existing task for the authenticated student."
    )
    public ResponseEntity<TaskResponse> updateTask(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = taskService.updateTask(studentId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{taskId}")
    @Operation(
            summary = "Get task by ID",
            description = "Retrieve a single task with full details."
    )
    public ResponseEntity<TaskResponse> getTaskById(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId
    ) {
        TaskResponse response = taskService.getTaskById(studentId, taskId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @Operation(
            summary = "Delete task",
            description = "Soft delete a task for the authenticated student."
    )
    public ResponseEntity<TaskActionResponse> deleteTask(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId
    ) {
        TaskActionResponse response = taskService.deleteTask(studentId, taskId);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Task Listing Views
    // -------------------------------------------------------------------------

    @GetMapping
    @Operation(
            summary = "Get all tasks",
            description = "Retrieve all non-deleted tasks for the authenticated student."
    )
    public ResponseEntity<List<TaskSummaryResponse>> getAllTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getAllTasks(studentId));
    }

    @GetMapping("/active")
    @Operation(
            summary = "Get active tasks",
            description = "Retrieve all active tasks."
    )
    public ResponseEntity<List<TaskSummaryResponse>> getActiveTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getActiveTasks(studentId));
    }

    @GetMapping("/completed")
    @Operation(
            summary = "Get completed tasks",
            description = "Retrieve all completed tasks."
    )
    public ResponseEntity<List<TaskSummaryResponse>> getCompletedTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getCompletedTasks(studentId));
    }

    @GetMapping("/archived")
    @Operation(
            summary = "Get archived tasks",
            description = "Retrieve all archived tasks."
    )
    public ResponseEntity<List<TaskSummaryResponse>> getArchivedTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getArchivedTasks(studentId));
    }

    @GetMapping("/today")
    @Operation(
            summary = "Get today's tasks",
            description = "Retrieve tasks due today."
    )
    public ResponseEntity<List<TaskSummaryResponse>> getTodayTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getTodayTasks(studentId));
    }

    @GetMapping("/course/{courseId}")
    @Operation(
            summary = "Get tasks by course",
            description = "Retrieve all tasks associated with a specific course."
    )
    public ResponseEntity<List<TaskSummaryResponse>> getTasksByCourse(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Course ID", example = "10")
            @PathVariable Long courseId
    ) {
        return ResponseEntity.ok(taskService.getTasksByCourse(studentId, courseId));
    }

    // -------------------------------------------------------------------------
    // Task State Changes
    // -------------------------------------------------------------------------

    @PatchMapping("/{taskId}/archive")
    @Operation(
            summary = "Change archive state",
            description = "Archive or unarchive a task."
    )
    public ResponseEntity<TaskActionResponse> changeArchiveState(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody ArchiveTaskRequest request
    ) {
        TaskActionResponse response = taskService.archiveTask(studentId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/complete")
    @Operation(
            summary = "Change completion state",
            description = "Mark a task as completed or uncompleted."
    )
    public ResponseEntity<TaskActionResponse> changeCompletionState(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCompletionRequest request
    ) {
        TaskActionResponse response = taskService.completeTask(studentId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(
            summary = "Change task status",
            description = "Update task status such as ACTIVE, COMPLETED, or CANCELLED."
    )
    public ResponseEntity<TaskActionResponse> changeStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request
    ) {
        TaskActionResponse response = taskService.updateTaskStatus(studentId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/bulk")
    @Operation(
            summary = "Apply bulk action",
            description = "Apply a bulk action to multiple tasks."
    )
    public ResponseEntity<BulkTaskActionResponse> applyBulkAction(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Valid @RequestBody BulkTaskActionRequest request
    ) {
        BulkTaskActionResponse response = taskService.applyBulkAction(studentId, request);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Search and Statistics
    // -------------------------------------------------------------------------

    @PostMapping("/search")
    @Operation(
            summary = "Search tasks",
            description = "Search tasks using filters such as query, priority, status, course, labels, dates, and pagination."
    )
    public ResponseEntity<TaskSearchResponse> searchTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Valid @RequestBody TaskSearchRequest request
    ) {
        TaskSearchResponse response = taskService.searchTasks(studentId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search/advanced")
    @Operation(
            summary = "Advanced search",
            description = "Search tasks and return details about which fields matched the query."
    )
    public ResponseEntity<TaskAdvancedSearchResponse> advancedSearchTasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Valid @RequestBody TaskSearchRequest request
    ) {
        TaskAdvancedSearchResponse response = taskService.advancedSearchTasks(studentId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    @Operation(
            summary = "Get task statistics",
            description = "Retrieve task statistics such as all, active, completed, archived, overdue, and due today counts."
    )
    public ResponseEntity<TaskStatsResponse> getTaskStats(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        TaskStatsResponse response = taskService.getTaskStats(studentId);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Labels
    // -------------------------------------------------------------------------

    @PostMapping("/labels")
    @Operation(
            summary = "Create label",
            description = "Create a new task label for the authenticated student."
    )
    public ResponseEntity<TaskLabelResponse> createLabel(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Valid @RequestBody CreateTaskLabelRequest request
    ) {
        TaskLabelResponse response = taskService.createLabel(studentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/labels/{labelId}")
    @Operation(
            summary = "Update label",
            description = "Update an existing task label."
    )
    public ResponseEntity<TaskLabelResponse> updateLabel(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Label ID", example = "3")
            @PathVariable Long labelId,
            @Valid @RequestBody UpdateTaskLabelRequest request
    ) {
        TaskLabelResponse response = taskService.updateLabel(studentId, labelId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/labels")
    @Operation(
            summary = "Get all labels",
            description = "Retrieve all task labels for the authenticated student."
    )
    public ResponseEntity<List<TaskLabelResponse>> getAllLabels(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getAllLabels(studentId));
    }

    // -------------------------------------------------------------------------
    // Attachments
    // -------------------------------------------------------------------------

    @PostMapping("/{taskId}/attachments")
    @Operation(
            summary = "Add attachment",
            description = "Attach a file or external link to a task. The actual file upload should be handled separately."
    )
    public ResponseEntity<TaskAttachmentUploadResponse> addAttachment(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody CreateTaskAttachmentRequest request
    ) {
        TaskAttachmentUploadResponse response = taskService.addAttachment(studentId, taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{taskId}/attachments/{attachmentId}")
    @Operation(
            summary = "Delete attachment",
            description = "Delete an attachment from a specific task."
    )
    public ResponseEntity<DeleteAttachmentResponse> deleteAttachment(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Parameter(description = "Attachment ID", example = "5")
            @PathVariable Long attachmentId
    ) {
        DeleteAttachmentResponse response = taskService.deleteAttachment(studentId, taskId, attachmentId);
        return ResponseEntity.ok(response);
    }

    // -------------------------------------------------------------------------
    // Reminders
    // -------------------------------------------------------------------------

    @GetMapping("/{taskId}/reminders")
    @Operation(
            summary = "Get task reminders",
            description = "Retrieve all reminders configured for a specific task."
    )
    public ResponseEntity<List<TaskReminderResponse>> getTaskReminders(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId
    ) {
        return ResponseEntity.ok(taskService.getTaskReminders(studentId, taskId));
    }

    @GetMapping("/reminders/upcoming")
    @Operation(
            summary = "Get upcoming reminders",
            description = "Retrieve upcoming reminders for the authenticated student."
    )
    public ResponseEntity<List<TaskReminderPreviewResponse>> getUpcomingReminders(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId
    ) {
        return ResponseEntity.ok(taskService.getUpcomingReminders(studentId));
    }

    // -------------------------------------------------------------------------
    // Subtasks
    // -------------------------------------------------------------------------

    @PatchMapping("/{taskId}/subtasks/reorder")
    @Operation(
            summary = "Reorder subtasks",
            description = "Update the display order of subtasks for a specific task."
    )
    public ResponseEntity<TaskResponse> reorderSubtasks(
            @Parameter(hidden = true)
            @AuthenticationPrincipal(expression = "studentId") Long studentId,
            @Parameter(description = "Task ID", example = "1")
            @PathVariable Long taskId,
            @Valid @RequestBody ReorderSubtasksRequest request
    ) {
        TaskResponse response = taskService.reorderSubtasks(studentId, taskId, request);
        return ResponseEntity.ok(response);
    }
}