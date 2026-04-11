package com.studentbag.backend.tasks.service.Impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.domain.enums.TaskStatus;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.tasks.dto.request.*;
import com.studentbag.backend.tasks.dto.response.*;
import com.studentbag.backend.tasks.entity.*;
import com.studentbag.backend.tasks.mapper.*;
import com.studentbag.backend.tasks.repository.*;
import com.studentbag.backend.tasks.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskLabelRepository taskLabelRepository;
    private final SubtaskRepository subtaskRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final TaskReminderRepository taskReminderRepository;

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    private final TaskMapper taskMapper;
    private final TaskLabelMapper taskLabelMapper;
    private final SubtaskMapper subtaskMapper;
    private final TaskAttachmentMapper taskAttachmentMapper;
    private final TaskReminderMapper taskReminderMapper;
    private final SubtaskReorderMapper subtaskReorderMapper;

    @Override
    public TaskResponse createTask(Long studentId, CreateTaskRequest request) {
        Student student = getStudent(studentId);
        Course course = resolveCourse(request.getCourseId());

        Task task = taskMapper.toEntity(request);
        task.setStudent(student);
        task.setCourse(course);

        if (task.getLabels() == null) {
            task.setLabels(new HashSet<>());
        }

        if (request.getLabelIds() != null && !request.getLabelIds().isEmpty()) {
            Set<TaskLabel> labels = resolveLabels(studentId, request.getLabelIds());
            task.setLabels(labels);
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    public TaskResponse updateTask(Long studentId, Long taskId, UpdateTaskRequest request) {
        Task task = getTaskForStudent(studentId, taskId);

        Course course = null;
        if (request.getCourseId() != null) {
            course = resolveCourse(request.getCourseId());
        }

        taskMapper.updateEntity(task, request, course);

        if (request.getLabelIds() != null) {
            Set<TaskLabel> labels = resolveLabels(studentId, request.getLabelIds());
            task.setLabels(labels);
        }

        if (request.getSubtasks() != null) {
            replaceSubtasks(task, request.getSubtasks());
        }

        if (request.getReminders() != null) {
            replaceReminders(task, request.getReminders());
        }

        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse getTaskById(Long studentId, Long taskId) {
        Task task = getTaskForStudent(studentId, taskId);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public List<TaskSummaryResponse> getAllTasks(Long studentId) {
        return taskMapper.toSummaryResponseList(
                taskRepository.findByStudentIdAndIsDeletedFalse(studentId)
        );
    }

    @Override
    @Transactional
    public List<TaskSummaryResponse> getActiveTasks(Long studentId) {
        List<Task> tasks = taskRepository.findByStudentIdAndStatusAndIsDeletedFalse(
                studentId,
                TaskStatus.ACTIVE
        );

        return taskMapper.toSummaryResponseList(
                tasks.stream()
                        .filter(task -> !Boolean.TRUE.equals(task.getArchived()))
                        .toList()
        );
    }

    @Override
    @Transactional
    public List<TaskSummaryResponse> getCompletedTasks(Long studentId) {
        return taskMapper.toSummaryResponseList(
                taskRepository.findByStudentIdAndStatusAndIsDeletedFalse(
                        studentId,
                        TaskStatus.COMPLETED
                )
        );
    }

    @Override
    @Transactional
    public List<TaskSummaryResponse> getArchivedTasks(Long studentId) {
        List<Task> tasks = taskRepository.findByStudentIdAndIsDeletedFalse(studentId)
                .stream()
                .filter(task -> Boolean.TRUE.equals(task.getArchived()))
                .toList();

        return taskMapper.toSummaryResponseList(tasks);
    }

    @Override
    @Transactional
    public List<TaskSummaryResponse> getTodayTasks(Long studentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay().minusNanos(1);

        return taskMapper.toSummaryResponseList(
                taskRepository.findByStudentIdAndDueDateTimeBetweenAndIsDeletedFalse(studentId, start, end)
        );
    }

    @Override
    @Transactional
    public List<TaskSummaryResponse> getTasksByCourse(Long studentId, Long courseId) {
        return taskMapper.toSummaryResponseList(
                taskRepository.findByStudentIdAndCourseIdAndIsDeletedFalse(studentId, courseId)
        );
    }

    @Override
    public TaskActionResponse deleteTask(Long studentId, Long taskId) {
        Task task = getTaskForStudent(studentId, taskId);
        task.setIsDeleted(true);
        taskRepository.save(task);

        return taskMapper.toActionResponse(taskId, "DELETE", "Task deleted successfully", true);
    }

    @Override
    public TaskActionResponse archiveTask(Long studentId, Long taskId, ArchiveTaskRequest request) {
        Task task = getTaskForStudent(studentId, taskId);
        taskMapper.applyArchive(task, request);
        taskRepository.save(task);

        return taskMapper.toActionResponse(taskId, "ARCHIVE", "Task archive status updated", true);
    }

    @Override
    public TaskActionResponse completeTask(Long studentId, Long taskId, TaskCompletionRequest request) {
        Task task = getTaskForStudent(studentId, taskId);
        taskMapper.applyCompletion(task, request);
        taskRepository.save(task);

        return taskMapper.toActionResponse(taskId, "COMPLETE", "Task completion updated", true);
    }

    @Override
    public TaskActionResponse updateTaskStatus(Long studentId, Long taskId, UpdateTaskStatusRequest request) {
        Task task = getTaskForStudent(studentId, taskId);
        taskMapper.applyStatusChange(task, request);
        taskRepository.save(task);

        return taskMapper.toActionResponse(taskId, "STATUS_CHANGE", "Task status updated", true);
    }

    @Override
    public BulkTaskActionResponse applyBulkAction(Long studentId, BulkTaskActionRequest request) {
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();

        if (request.getTaskIds() == null || request.getTaskIds().isEmpty()) {
            return taskMapper.toBulkActionResponse(
                    request.getAction(),
                    List.of(),
                    List.of(),
                    "No task ids provided"
            );
        }

        for (Long taskId : request.getTaskIds()) {
            try {
                Task task = getTaskForStudent(studentId, taskId);
                applyBulkActionToTask(task, request);
                taskRepository.save(task);
                successIds.add(taskId);
            } catch (Exception ex) {
                failedIds.add(taskId);
            }
        }

        return taskMapper.toBulkActionResponse(
                request.getAction(),
                successIds,
                failedIds,
                "Bulk action processed"
        );
    }

    @Override
    public TaskAttachmentUploadResponse addAttachment(Long studentId, Long taskId, CreateTaskAttachmentRequest request) {
        Task task = getTaskForStudent(studentId, taskId);

        TaskAttachment attachment = taskAttachmentMapper.toEntity(request);
        attachment.setTask(task);

        TaskAttachment saved = taskAttachmentRepository.save(attachment);
        return taskAttachmentMapper.toUploadResponse(taskId, saved, "Attachment added successfully");
    }

    @Override
    public DeleteAttachmentResponse deleteAttachment(Long studentId, Long taskId, Long attachmentId) {
        Task task = getTaskForStudent(studentId, taskId);

        TaskAttachment attachment = taskAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment not found"));

        if (attachment.getTask() == null || !Objects.equals(attachment.getTask().getId(), task.getId())) {
            throw new IllegalArgumentException("Attachment does not belong to the specified task");
        }

        taskAttachmentRepository.delete(attachment);
        return taskAttachmentMapper.toDeleteResponse(attachmentId, true, "Attachment deleted successfully");
    }

    @Override
    @Transactional
    public List<TaskReminderResponse> getTaskReminders(Long studentId, Long taskId) {
        Task task = getTaskForStudent(studentId, taskId);
        return taskReminderMapper.toResponseList(
                taskReminderRepository.findByTaskIdOrderByRemindAtAsc(task.getId())
        );
    }

    @Override
    @Transactional
    public List<TaskReminderPreviewResponse> getUpcomingReminders(Long studentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusDays(7);

        List<TaskReminder> reminders = taskRepository.findByStudentIdAndIsDeletedFalse(studentId).stream()
                .flatMap(task -> task.getReminders().stream())
                .filter(Objects::nonNull)
                .filter(reminder -> Boolean.TRUE.equals(reminder.getEnabled()))
                .filter(reminder -> !Boolean.TRUE.equals(reminder.getSent()))
                .filter(reminder -> reminder.getRemindAt() != null)
                .filter(reminder -> !reminder.getRemindAt().isBefore(now))
                .filter(reminder -> !reminder.getRemindAt().isAfter(until))
                .sorted(Comparator.comparing(TaskReminder::getRemindAt))
                .toList();

        return taskReminderMapper.toPreviewResponseList(reminders);
    }

    @Override
    @Transactional
    public TaskSearchResponse searchTasks(Long studentId, TaskSearchRequest request) {
        List<Task> filtered = filterTasks(studentId, request);
        List<Task> paged = paginate(filtered, request.getPage(), request.getSize());

        return taskMapper.toSearchResponse(
                paged,
                defaultPage(request.getPage()),
                defaultSize(request.getSize()),
                filtered.size()
        );
    }

    @Override
    @Transactional
    public TaskAdvancedSearchResponse advancedSearchTasks(Long studentId, TaskSearchRequest request) {
        List<Task> filtered = filterTasks(studentId, request);

        List<TaskAdvancedSearchItemResponse> matchedItems = filtered.stream()
                .map(task -> taskMapper.toAdvancedSearchItemResponse(task, buildSearchMatches(task, request.getQuery())))
                .toList();

        int page = defaultPage(request.getPage());
        int size = defaultSize(request.getSize());
        int from = Math.min(page * size, matchedItems.size());
        int to = Math.min(from + size, matchedItems.size());

        List<TaskAdvancedSearchItemResponse> paged = matchedItems.subList(from, to);

        return taskMapper.toAdvancedSearchResponse(paged, page, size, matchedItems.size());
    }

    @Override
    @Transactional
    public TaskStatsResponse getTaskStats(Long studentId) {
        List<Task> tasks = taskRepository.findByStudentIdAndIsDeletedFalse(studentId);
        LocalDate today = LocalDate.now();

        long allCount = tasks.size();
        long activeCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.ACTIVE).count();
        long completedCount = tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).count();
        long archivedCount = tasks.stream().filter(t -> Boolean.TRUE.equals(t.getArchived())).count();
        long overdueCount = tasks.stream()
                .filter(t -> t.getDueDateTime() != null)
                .filter(t -> t.getStatus() != TaskStatus.COMPLETED)
                .filter(t -> t.getDueDateTime().isBefore(LocalDateTime.now()))
                .count();

        long dueTodayCount = tasks.stream()
                .filter(t -> t.getDueDateTime() != null)
                .filter(t -> t.getDueDateTime().toLocalDate().isEqual(today))
                .count();

        return taskMapper.toStatsResponse(
                allCount,
                activeCount,
                completedCount,
                archivedCount,
                overdueCount,
                dueTodayCount
        );
    }

    @Override
    public TaskLabelResponse createLabel(Long studentId, CreateTaskLabelRequest request) {
        if (taskLabelRepository.existsByStudentIdAndNameIgnoreCase(studentId, request.getName())) {
            throw new IllegalArgumentException("Label with the same name already exists");
        }

        Student student = getStudent(studentId);
        TaskLabel label = taskLabelMapper.toEntity(request);
        label.setStudent(student);

        TaskLabel saved = taskLabelRepository.save(label);
        return taskLabelMapper.toResponse(saved);
    }

    @Override
    public TaskLabelResponse updateLabel(Long studentId, Long labelId, UpdateTaskLabelRequest request) {
        TaskLabel label = taskLabelRepository.findByIdAndStudentId(labelId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Label not found"));

        taskLabelMapper.updateEntity(label, request);
        TaskLabel saved = taskLabelRepository.save(label);

        return taskLabelMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public List<TaskLabelResponse> getAllLabels(Long studentId) {
        return taskLabelRepository.findByStudentId(studentId).stream()
                .map(taskLabelMapper::toResponse)
                .toList();
    }

    @Override
    public TaskResponse reorderSubtasks(Long studentId, Long taskId, ReorderSubtasksRequest request) {
        Task task = getTaskForStudent(studentId, taskId);

        List<Subtask> subtasks = subtaskRepository.findByTaskIdOrderByOrderIndexAsc(taskId);
        subtaskReorderMapper.applyReorder(subtasks, request);
        subtaskRepository.saveAll(subtasks);

        task.setSubtasks(subtaskRepository.findByTaskIdOrderByOrderIndexAsc(taskId));
        return taskMapper.toResponse(task);
    }

    private Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    private Course resolveCourse(Long courseId) {
        if (courseId == null) {
            return null;
        }

        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    private Set<TaskLabel> resolveLabels(Long studentId, Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return new HashSet<>();
        }

        Set<TaskLabel> labels = new HashSet<>();
        for (Long labelId : labelIds) {
            TaskLabel label = taskLabelRepository.findByIdAndStudentId(labelId, studentId)
                    .orElseThrow(() -> new IllegalArgumentException("Label not found: " + labelId));
            labels.add(label);
        }

        return labels;
    }

    private Task getTaskForStudent(Long studentId, Long taskId) {
        return taskRepository.findByIdAndStudentIdAndIsDeletedFalse(taskId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
    }

    private void replaceSubtasks(Task task, List<UpdateSubtaskRequest> requests) {
        Map<Long, Subtask> existingById = task.getSubtasks() == null
                ? new HashMap<>()
                : task.getSubtasks().stream()
                .filter(subtask -> subtask.getId() != null)
                .collect(Collectors.toMap(Subtask::getId, Function.identity()));

        List<Subtask> updatedList = new ArrayList<>();

        for (UpdateSubtaskRequest request : requests) {
            if (request == null) {
                continue;
            }

            Subtask subtask;
            if (request.getId() != null && existingById.containsKey(request.getId())) {
                subtask = existingById.get(request.getId());
                subtaskMapper.updateEntity(subtask, request);
            } else {
                subtask = Subtask.builder()
                        .title(request.getTitle())
                        .isCompleted(Boolean.TRUE.equals(request.getIsCompleted()))
                        .orderIndex(request.getOrderIndex() != null ? request.getOrderIndex() : 0)
                        .build();
            }

            subtask.setTask(task);
            updatedList.add(subtask);
        }

        task.getSubtasks().clear();
        task.getSubtasks().addAll(updatedList);
    }

    private void replaceReminders(Task task, List<UpdateTaskReminderRequest> requests) {
        Map<Long, TaskReminder> existingById = task.getReminders() == null
                ? new HashMap<>()
                : task.getReminders().stream()
                .filter(reminder -> reminder.getId() != null)
                .collect(Collectors.toMap(TaskReminder::getId, Function.identity()));

        List<TaskReminder> updatedList = new ArrayList<>();

        for (UpdateTaskReminderRequest request : requests) {
            if (request == null) {
                continue;
            }

            TaskReminder reminder;
            if (request.getId() != null && existingById.containsKey(request.getId())) {
                reminder = existingById.get(request.getId());
                taskReminderMapper.updateEntity(reminder, request);
            } else {
                reminder = TaskReminder.builder()
                        .remindAt(request.getRemindAt())
                        .minutesBefore(request.getMinutesBefore())
                        .channel(request.getChannel())
                        .enabled(request.getEnabled() == null || request.getEnabled())
                        .sent(request.getSent() != null && request.getSent())
                        .build();
            }

            reminder.setTask(task);
            updatedList.add(reminder);
        }

        task.getReminders().clear();
        task.getReminders().addAll(updatedList);
    }

    private void applyBulkActionToTask(Task task, BulkTaskActionRequest request) {
        if (request.getAction() == null) {
            throw new IllegalArgumentException("Bulk action is required");
        }

        switch (request.getAction().trim().toUpperCase()) {
            case "COMPLETE" -> {
                task.setStatus(TaskStatus.COMPLETED);
                task.setCompletedAt(LocalDateTime.now());
            }
            case "UNCOMPLETE" -> {
                task.setStatus(TaskStatus.ACTIVE);
                task.setCompletedAt(null);
            }
            case "DELETE" -> task.setIsDeleted(true);
            case "ARCHIVE" -> task.setArchived(true);
            case "UNARCHIVE" -> task.setArchived(false);
            case "CHANGE_PRIORITY" -> {
                if (request.getPriority() == null) {
                    throw new IllegalArgumentException("Priority is required for CHANGE_PRIORITY");
                }
                task.setPriority(request.getPriority());
            }
            case "CHANGE_STATUS" -> {
                if (request.getStatus() == null) {
                    throw new IllegalArgumentException("Status is required for CHANGE_STATUS");
                }
                task.setStatus(request.getStatus());
                if (request.getStatus() == TaskStatus.COMPLETED) {
                    task.setCompletedAt(LocalDateTime.now());
                } else {
                    task.setCompletedAt(null);
                }
            }
            default -> throw new IllegalArgumentException("Unsupported bulk action: " + request.getAction());
        }
    }

    private List<Task> filterTasks(Long studentId, TaskSearchRequest request) {
        List<Task> tasks = taskRepository.findByStudentIdAndIsDeletedFalse(studentId);

        return tasks.stream()
                .filter(task -> matchesQuery(task, request.getQuery()))
                .filter(task -> request.getStatus() == null || task.getStatus() == request.getStatus())
                .filter(task -> request.getPriority() == null || task.getPriority() == request.getPriority())
                .filter(task -> request.getCourseId() == null ||
                        (task.getCourse() != null && Objects.equals(task.getCourse().getId(), request.getCourseId())))
                .filter(task -> request.getArchived() == null || Objects.equals(task.getArchived(), request.getArchived()))
                .filter(task -> request.getCompleted() == null ||
                        Objects.equals(task.getStatus() == TaskStatus.COMPLETED, request.getCompleted()))
                .filter(task -> request.getOverdue() == null ||
                        Objects.equals(isOverdue(task), request.getOverdue()))
                .filter(task -> request.getDueToday() == null ||
                        Objects.equals(isDueToday(task), request.getDueToday()))
                .filter(task -> matchesDueRange(task, request.getDueFrom(), request.getDueTo()))
                .filter(task -> matchesLabels(task, request.getLabelIds()))
                .sorted(buildComparator(request.getSortBy(), request.getSortDirection()))
                .toList();
    }

    private boolean matchesQuery(Task task, String query) {
        if (query == null || query.isBlank()) {
            return true;
        }

        String q = query.trim().toLowerCase();

        boolean idMatch = task.getId() != null && String.valueOf(task.getId()).contains(q);

        boolean titleMatch = containsIgnoreCase(task.getTitle(), q);
        boolean descriptionMatch = containsIgnoreCase(task.getDescription(), q);

        boolean subtaskMatch = task.getSubtasks() != null && task.getSubtasks().stream()
                .anyMatch(subtask -> containsIgnoreCase(subtask.getTitle(), q));

        boolean attachmentMatch = task.getAttachments() != null && task.getAttachments().stream()
                .anyMatch(attachment ->
                        containsIgnoreCase(attachment.getFileName(), q) ||
                                containsIgnoreCase(attachment.getExtension(), q) ||
                                containsIgnoreCase(attachment.getMimeType(), q)
                );

        return idMatch || titleMatch || descriptionMatch || subtaskMatch || attachmentMatch;
    }

    private List<TaskSearchMatchResponse> buildSearchMatches(Task task, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        String q = query.trim().toLowerCase();
        List<TaskSearchMatchResponse> matches = new ArrayList<>();

        if (task.getId() != null && String.valueOf(task.getId()).contains(q)) {
            matches.add(taskMapper.toSearchMatchResponse("TASK_ID", String.valueOf(task.getId())));
        }

        if (containsIgnoreCase(task.getTitle(), q)) {
            matches.add(taskMapper.toSearchMatchResponse("TITLE", task.getTitle()));
        }

        if (containsIgnoreCase(task.getDescription(), q)) {
            matches.add(taskMapper.toSearchMatchResponse("DESCRIPTION", task.getDescription()));
        }

        if (task.getSubtasks() != null) {
            task.getSubtasks().stream()
                    .filter(subtask -> containsIgnoreCase(subtask.getTitle(), q))
                    .forEach(subtask ->
                            matches.add(taskMapper.toSearchMatchResponse("SUBTASK", subtask.getTitle()))
                    );
        }

        if (task.getAttachments() != null) {
            task.getAttachments().stream()
                    .filter(attachment -> containsIgnoreCase(attachment.getFileName(), q))
                    .forEach(attachment ->
                            matches.add(taskMapper.toSearchMatchResponse("ATTACHMENT_FILE_NAME", attachment.getFileName()))
                    );
        }

        return matches;
    }

    private Comparator<Task> buildComparator(String sortBy, String sortDirection) {
        Comparator<Task> comparator;

        String field = sortBy == null ? "createdAt" : sortBy.trim().toLowerCase();
        switch (field) {
            case "duedatetime", "due_date", "due" ->
                    comparator = Comparator.comparing(Task::getDueDateTime, Comparator.nullsLast(LocalDateTime::compareTo));
            case "priority" ->
                    comparator = Comparator.comparing(Task::getPriority, Comparator.nullsLast(Enum::compareTo));
            case "title" ->
                    comparator = Comparator.comparing(Task::getTitle, Comparator.nullsLast(String::compareToIgnoreCase));
            case "course" ->
                    comparator = Comparator.comparing(
                            task -> task.getCourse() != null ? task.getCourse().getCode() : null,
                            Comparator.nullsLast(String::compareToIgnoreCase)
                    );
            default ->
                    comparator = Comparator.comparing(Task::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo));
        }

        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }

        return comparator;
    }

    private List<Task> paginate(List<Task> tasks, Integer page, Integer size) {
        int safePage = defaultPage(page);
        int safeSize = defaultSize(size);

        int from = Math.min(safePage * safeSize, tasks.size());
        int to = Math.min(from + safeSize, tasks.size());

        return tasks.subList(from, to);
    }

    private int defaultPage(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    private int defaultSize(Integer size) {
        return size == null || size <= 0 ? 20 : size;
    }

    private boolean matchesDueRange(Task task, LocalDate dueFrom, LocalDate dueTo) {
        if (task.getDueDateTime() == null) {
            return dueFrom == null && dueTo == null;
        }

        LocalDate dueDate = task.getDueDateTime().toLocalDate();

        boolean afterOrEqualFrom = dueFrom == null || !dueDate.isBefore(dueFrom);
        boolean beforeOrEqualTo = dueTo == null || !dueDate.isAfter(dueTo);

        return afterOrEqualFrom && beforeOrEqualTo;
    }

    private boolean matchesLabels(Task task, Set<Long> labelIds) {
        if (labelIds == null || labelIds.isEmpty()) {
            return true;
        }

        if (task.getLabels() == null || task.getLabels().isEmpty()) {
            return false;
        }

        Set<Long> taskLabelIds = task.getLabels().stream()
                .map(TaskLabel::getId)
                .collect(Collectors.toSet());

        return labelIds.stream().allMatch(taskLabelIds::contains);
    }

    private boolean containsIgnoreCase(String value, String query) {
        return value != null && query != null && value.toLowerCase().contains(query.toLowerCase());
    }

    private boolean isOverdue(Task task) {
        return task.getDueDateTime() != null
                && task.getStatus() != TaskStatus.COMPLETED
                && task.getDueDateTime().isBefore(LocalDateTime.now());
    }

    private boolean isDueToday(Task task) {
        return task.getDueDateTime() != null
                && task.getDueDateTime().toLocalDate().isEqual(LocalDate.now());
    }
}