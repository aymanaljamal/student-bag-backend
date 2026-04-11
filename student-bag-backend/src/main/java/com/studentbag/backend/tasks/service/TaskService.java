package com.studentbag.backend.tasks.service;

import com.studentbag.backend.tasks.dto.request.*;
import com.studentbag.backend.tasks.dto.response.*;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long studentId, CreateTaskRequest request);

    TaskResponse updateTask(Long studentId, Long taskId, UpdateTaskRequest request);

    TaskResponse getTaskById(Long studentId, Long taskId);

    List<TaskSummaryResponse> getAllTasks(Long studentId);

    List<TaskSummaryResponse> getActiveTasks(Long studentId);

    List<TaskSummaryResponse> getCompletedTasks(Long studentId);

    List<TaskSummaryResponse> getArchivedTasks(Long studentId);

    List<TaskSummaryResponse> getTodayTasks(Long studentId);

    List<TaskSummaryResponse> getTasksByCourse(Long studentId, Long courseId);

    TaskActionResponse deleteTask(Long studentId, Long taskId);

    TaskActionResponse archiveTask(Long studentId, Long taskId, ArchiveTaskRequest request);

    TaskActionResponse completeTask(Long studentId, Long taskId, TaskCompletionRequest request);

    TaskActionResponse updateTaskStatus(Long studentId, Long taskId, UpdateTaskStatusRequest request);

    BulkTaskActionResponse applyBulkAction(Long studentId, BulkTaskActionRequest request);

    TaskAttachmentUploadResponse addAttachment(Long studentId, Long taskId, CreateTaskAttachmentRequest request);

    DeleteAttachmentResponse deleteAttachment(Long studentId, Long taskId, Long attachmentId);

    List<TaskReminderResponse> getTaskReminders(Long studentId, Long taskId);

    List<TaskReminderPreviewResponse> getUpcomingReminders(Long studentId);

    TaskSearchResponse searchTasks(Long studentId, TaskSearchRequest request);

    TaskAdvancedSearchResponse advancedSearchTasks(Long studentId, TaskSearchRequest request);

    TaskStatsResponse getTaskStats(Long studentId);

    TaskLabelResponse createLabel(Long studentId, CreateTaskLabelRequest request);

    TaskLabelResponse updateLabel(Long studentId, Long labelId, UpdateTaskLabelRequest request);

    List<TaskLabelResponse> getAllLabels(Long studentId);

    TaskResponse reorderSubtasks(Long studentId, Long taskId, ReorderSubtasksRequest request);
}