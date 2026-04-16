package com.studentbag.backend.tasks.mapper;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.domain.enums.tasks.TaskPriority;
import com.studentbag.backend.domain.enums.tasks.TaskRecurrenceType;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.tasks.dto.request.ArchiveTaskRequest;
import com.studentbag.backend.tasks.dto.request.CreateTaskRequest;
import com.studentbag.backend.tasks.dto.request.TaskCompletionRequest;
import com.studentbag.backend.tasks.dto.request.TaskStatusChangeRequest;
import com.studentbag.backend.tasks.dto.request.UpdateTaskRequest;
import com.studentbag.backend.tasks.dto.request.UpdateTaskStatusRequest;
import com.studentbag.backend.tasks.dto.response.*;
import com.studentbag.backend.tasks.entity.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    private final TaskLabelMapper taskLabelMapper;
    private final SubtaskMapper subtaskMapper;
    private final TaskAttachmentMapper taskAttachmentMapper;
    private final TaskReminderMapper taskReminderMapper;

    public TaskMapper(
            TaskLabelMapper taskLabelMapper,
            SubtaskMapper subtaskMapper,
            TaskAttachmentMapper taskAttachmentMapper,
            TaskReminderMapper taskReminderMapper
    ) {
        this.taskLabelMapper = taskLabelMapper;
        this.subtaskMapper = subtaskMapper;
        this.taskAttachmentMapper = taskAttachmentMapper;
        this.taskReminderMapper = taskReminderMapper;
    }

    public Task toEntity(CreateTaskRequest request) {
        if (request == null) {
            return null;
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDateTime(request.getDueDateTime())
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .status(TaskStatus.ACTIVE)
                .estimatedMinutes(request.getEstimatedMinutes())
                .recurrenceType(request.getRecurrenceType() != null ? request.getRecurrenceType() : TaskRecurrenceType.NONE)
                .recurrenceInterval(request.getRecurrenceInterval() != null ? request.getRecurrenceInterval() : 1)
                .notificationsEnabled(request.getNotificationsEnabled() != null ? request.getNotificationsEnabled() : true)
                .build();

        if (request.getSubtasks() != null) {
            List<Subtask> subtasks = request.getSubtasks().stream()
                    .filter(Objects::nonNull)
                    .map(subtaskMapper::toEntity)
                    .collect(Collectors.toList());

            subtasks.forEach(subtask -> subtask.setTask(task));
            task.setSubtasks(subtasks);
        }

        if (request.getAttachments() != null) {
            List<TaskAttachment> attachments = request.getAttachments().stream()
                    .filter(Objects::nonNull)
                    .map(taskAttachmentMapper::toEntity)
                    .collect(Collectors.toList());

            attachments.forEach(attachment -> attachment.setTask(task));
            task.setAttachments(attachments);
        }

        if (request.getReminders() != null) {
            List<TaskReminder> reminders = request.getReminders().stream()
                    .filter(Objects::nonNull)
                    .map(taskReminderMapper::toEntity)
                    .collect(Collectors.toList());

            reminders.forEach(reminder -> reminder.setTask(task));
            task.setReminders(reminders);
        }

        return task;
    }

    public void updateEntity(Task task, UpdateTaskRequest request, Course courseOrNull) {
        if (task == null || request == null) {
            return;
        }

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getDueDateTime() != null) {
            task.setDueDateTime(request.getDueDateTime());
        }

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getEstimatedMinutes() != null) {
            task.setEstimatedMinutes(request.getEstimatedMinutes());
        }

        if (request.getRecurrenceType() != null) {
            task.setRecurrenceType(request.getRecurrenceType());
        }

        if (request.getRecurrenceInterval() != null) {
            task.setRecurrenceInterval(request.getRecurrenceInterval());
        }

        if (request.getNotificationsEnabled() != null) {
            task.setNotificationsEnabled(request.getNotificationsEnabled());
        }

        if (request.getCourseId() != null || courseOrNull == null) {
            task.setCourse(courseOrNull);
        }
    }

    public void applyStatusChange(Task task, TaskStatusChangeRequest request) {
        if (task == null || request == null || request.getStatus() == null) {
            return;
        }

        task.setStatus(request.getStatus());

        if (request.getStatus() == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    public void applyStatusChange(Task task, UpdateTaskStatusRequest request) {
        if (task == null || request == null || request.getStatus() == null) {
            return;
        }

        task.setStatus(request.getStatus());

        if (request.getStatus() == TaskStatus.COMPLETED) {
            if (task.getCompletedAt() == null) {
                task.setCompletedAt(LocalDateTime.now());
            }
        } else {
            task.setCompletedAt(null);
        }
    }

    public void applyCompletion(Task task, TaskCompletionRequest request) {
        if (task == null || request == null || request.getCompleted() == null) {
            return;
        }

        if (request.getCompleted()) {
            task.setStatus(TaskStatus.COMPLETED);
            task.setCompletedAt(LocalDateTime.now());
        } else {
            task.setStatus(TaskStatus.ACTIVE);
            task.setCompletedAt(null);
        }
    }

    public void applyArchive(Task task, ArchiveTaskRequest request) {
        if (task == null || request == null || request.getArchived() == null) {
            return;
        }

        task.setArchived(request.getArchived());
    }

    public TaskCourseSummaryResponse toCourseSummaryResponse(Course course) {
        if (course == null) {
            return null;
        }

        return TaskCourseSummaryResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .nameArabic(course.getNameArabic())
                .nameEnglish(course.getNameEnglish())
                .build();
    }

    public TaskSummaryResponse toSummaryResponse(Task entity) {
        if (entity == null) {
            return null;
        }

        int totalSubtasks = entity.getSubtasks() != null ? entity.getSubtasks().size() : 0;
        int completedSubtasks = entity.getSubtasks() != null
                ? (int) entity.getSubtasks().stream().filter(sub -> Boolean.TRUE.equals(sub.getIsCompleted())).count()
                : 0;

        int attachmentCount = entity.getAttachments() != null ? entity.getAttachments().size() : 0;
        int reminderCount = entity.getReminders() != null ? entity.getReminders().size() : 0;

        return TaskSummaryResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDateTime(entity.getDueDateTime())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .archived(entity.getArchived())
                .completed(entity.getStatus() == TaskStatus.COMPLETED)
                .overdue(isOverdue(entity))
                .estimatedMinutes(entity.getEstimatedMinutes())
                .totalSubtasks(totalSubtasks)
                .completedSubtasks(completedSubtasks)
                .attachmentCount(attachmentCount)
                .reminderCount(reminderCount)
                .course(toCourseSummaryResponse(entity.getCourse()))
                .labels(entity.getLabels() == null
                        ? List.of()
                        : entity.getLabels().stream()
                        .map(taskLabelMapper::toResponse)
                        .collect(Collectors.toList()))
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<TaskSummaryResponse> toSummaryResponseList(List<Task> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse toResponse(Task entity) {
        if (entity == null) {
            return null;
        }

        int subtaskCount = entity.getSubtasks() != null ? entity.getSubtasks().size() : 0;
        int completedSubtaskCount = entity.getSubtasks() != null
                ? (int) entity.getSubtasks().stream()
                .filter(subtask -> Boolean.TRUE.equals(subtask.getIsCompleted()))
                .count()
                : 0;

        int attachmentCount = entity.getAttachments() != null ? entity.getAttachments().size() : 0;
        int reminderCount = entity.getReminders() != null ? entity.getReminders().size() : 0;

        return TaskResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDateTime(entity.getDueDateTime())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .archived(entity.getArchived())
                .deleted(entity.getIsDeleted())
                .completed(entity.getStatus() == TaskStatus.COMPLETED)
                .overdue(isOverdue(entity))
                .completedAt(entity.getCompletedAt())
                .estimatedMinutes(entity.getEstimatedMinutes())
                .recurrenceType(entity.getRecurrenceType())
                .recurrenceInterval(entity.getRecurrenceInterval())
                .recurrenceLastGeneratedAt(entity.getRecurrenceLastGeneratedAt())
                .nextOccurrenceAt(entity.getNextOccurrenceAt())
                .notificationsEnabled(entity.getNotificationsEnabled())
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .courseCode(entity.getCourse() != null ? entity.getCourse().getCode() : null)
                .courseNameArabic(entity.getCourse() != null ? entity.getCourse().getNameArabic() : null)
                .courseNameEnglish(entity.getCourse() != null ? entity.getCourse().getNameEnglish() : null)
                .labels(entity.getLabels() == null
                        ? List.of()
                        : entity.getLabels().stream()
                        .map(taskLabelMapper::toResponse)
                        .toList())
                .subtasks(subtaskMapper.toResponseList(entity.getSubtasks()))
                .attachments(taskAttachmentMapper.toResponseList(entity.getAttachments()))
                .reminders(taskReminderMapper.toResponseList(entity.getReminders()))
                .subtaskCount(subtaskCount)
                .completedSubtaskCount(completedSubtaskCount)
                .attachmentCount(attachmentCount)
                .reminderCount(reminderCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TaskDetailsResponse toDetailsResponse(Task entity) {
        if (entity == null) {
            return null;
        }

        return TaskDetailsResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .dueDateTime(entity.getDueDateTime())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .archived(entity.getArchived())
                .deleted(entity.getIsDeleted())
                .completed(entity.getStatus() == TaskStatus.COMPLETED)
                .overdue(isOverdue(entity))
                .estimatedMinutes(entity.getEstimatedMinutes())
                .recurrenceType(entity.getRecurrenceType())
                .recurrenceInterval(entity.getRecurrenceInterval())
                .recurrenceLastGeneratedAt(entity.getRecurrenceLastGeneratedAt())
                .nextOccurrenceAt(entity.getNextOccurrenceAt())
                .notificationsEnabled(entity.getNotificationsEnabled())
                .completedAt(entity.getCompletedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .course(toCourseSummaryResponse(entity.getCourse()))
                .labels(entity.getLabels() == null
                        ? List.of()
                        : entity.getLabels().stream()
                        .map(taskLabelMapper::toResponse)
                        .collect(Collectors.toList()))
                .subtasks(subtaskMapper.toResponseList(entity.getSubtasks()))
                .attachments(taskAttachmentMapper.toResponseList(entity.getAttachments()))
                .reminders(taskReminderMapper.toResponseList(entity.getReminders()))
                .build();
    }

    public TaskActionResponse toActionResponse(Long taskId, String action, String message, boolean success) {
        return TaskActionResponse.builder()
                .taskId(taskId)
                .action(action)
                .message(message)
                .success(success)
                .build();
    }

    public BulkTaskActionResponse toBulkActionResponse(
            String action,
            List<Long> successTaskIds,
            List<Long> failedTaskIds,
            String message
    ) {
        int successCount = successTaskIds != null ? successTaskIds.size() : 0;
        int failedCount = failedTaskIds != null ? failedTaskIds.size() : 0;

        return BulkTaskActionResponse.builder()
                .action(action)
                .requestedCount(successCount + failedCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .successTaskIds(successTaskIds != null ? successTaskIds : List.of())
                .failedTaskIds(failedTaskIds != null ? failedTaskIds : List.of())
                .message(message)
                .build();
    }

    public TaskSearchResponse toSearchResponse(
            List<Task> tasks,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = size <= 0 ? 1 : (int) Math.ceil((double) totalElements / size);

        return TaskSearchResponse.builder()
                .items(toSummaryResponseList(tasks))
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .hasPrevious(page > 0)
                .build();
    }

    public TaskAdvancedSearchItemResponse toAdvancedSearchItemResponse(
            Task task,
            List<TaskSearchMatchResponse> matches
    ) {
        return TaskAdvancedSearchItemResponse.builder()
                .task(toSummaryResponse(task))
                .matches(matches != null ? matches : List.of())
                .build();
    }

    public TaskAdvancedSearchResponse toAdvancedSearchResponse(
            List<TaskAdvancedSearchItemResponse> items,
            int page,
            int size,
            long totalElements
    ) {
        int totalPages = size <= 0 ? 1 : (int) Math.ceil((double) totalElements / size);

        return TaskAdvancedSearchResponse.builder()
                .items(items != null ? items : List.of())
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNext(page + 1 < totalPages)
                .hasPrevious(page > 0)
                .build();
    }

    public TaskSearchMatchResponse toSearchMatchResponse(String matchType, String matchedValue) {
        return TaskSearchMatchResponse.builder()
                .matchType(matchType)
                .matchedValue(matchedValue)
                .build();
    }

    public TaskStatsResponse toStatsResponse(
            long allCount,
            long activeCount,
            long completedCount,
            long archivedCount,
            long overdueCount,
            long dueTodayCount
    ) {
        return TaskStatsResponse.builder()
                .allCount(allCount)
                .activeCount(activeCount)
                .completedCount(completedCount)
                .archivedCount(archivedCount)
                .overdueCount(overdueCount)
                .dueTodayCount(dueTodayCount)
                .build();
    }

    private boolean isOverdue(Task task) {
        return task != null
               && task.getDueDateTime() != null
               && task.getStatus() != TaskStatus.COMPLETED
               && task.getDueDateTime().isBefore(LocalDateTime.now());
    }

    public boolean isDueToday(Task task) {
        if (task == null || task.getDueDateTime() == null) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return task.getDueDateTime().toLocalDate().isEqual(today);
    }
}