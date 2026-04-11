package com.studentbag.backend.tasks.mapper;

import com.studentbag.backend.tasks.dto.request.CreateTaskReminderRequest;
import com.studentbag.backend.tasks.dto.request.UpdateTaskReminderRequest;
import com.studentbag.backend.tasks.dto.response.TaskReminderPreviewResponse;
import com.studentbag.backend.tasks.dto.response.TaskReminderResponse;
import com.studentbag.backend.tasks.entity.Task;
import com.studentbag.backend.tasks.entity.TaskReminder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class TaskReminderMapper {

    public TaskReminder toEntity(CreateTaskReminderRequest request) {
        if (request == null) {
            return null;
        }

        return TaskReminder.builder()
                .remindAt(request.getRemindAt())
                .minutesBefore(request.getMinutesBefore())
                .channel(request.getChannel())
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build();
    }

    public void updateEntity(TaskReminder reminder, UpdateTaskReminderRequest request) {
        if (reminder == null || request == null) {
            return;
        }

        if (request.getRemindAt() != null) {
            reminder.setRemindAt(request.getRemindAt());
        }

        if (request.getMinutesBefore() != null) {
            reminder.setMinutesBefore(request.getMinutesBefore());
        }

        if (request.getChannel() != null) {
            reminder.setChannel(request.getChannel());
        }

        if (request.getEnabled() != null) {
            reminder.setEnabled(request.getEnabled());
        }

        if (request.getSent() != null) {
            reminder.setSent(request.getSent());
        }
    }

    public TaskReminderResponse toResponse(TaskReminder entity) {
        if (entity == null) {
            return null;
        }

        return TaskReminderResponse.builder()
                .id(entity.getId())
                .remindAt(entity.getRemindAt())
                .minutesBefore(entity.getMinutesBefore())
                .channel(entity.getChannel())
                .enabled(entity.getEnabled())
                .sent(entity.getSent())
                .sentAt(entity.getSentAt())
                .build();
    }

    public List<TaskReminderResponse> toResponseList(List<TaskReminder> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TaskReminderPreviewResponse toPreviewResponse(TaskReminder reminder) {
        if (reminder == null || reminder.getTask() == null) {
            return null;
        }

        Task task = reminder.getTask();

        return TaskReminderPreviewResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .dueDateTime(task.getDueDateTime())
                .remindAt(reminder.getRemindAt())
                .minutesBefore(reminder.getMinutesBefore())
                .enabled(reminder.getEnabled())
                .build();
    }

    public List<TaskReminderPreviewResponse> toPreviewResponseList(List<TaskReminder> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toPreviewResponse)
                .collect(Collectors.toList());
    }
}