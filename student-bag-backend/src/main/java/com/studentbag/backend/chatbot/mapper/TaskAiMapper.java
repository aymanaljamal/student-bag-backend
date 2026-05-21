package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.AttachmentAiContext;
import com.studentbag.backend.chatbot.dto.context.SubtaskAiContext;
import com.studentbag.backend.chatbot.dto.context.TaskAiContext;
import com.studentbag.backend.tasks.entity.Task;
import com.studentbag.backend.tasks.entity.TaskAttachment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskAiMapper {

    public TaskAiContext toContext(Task task) {
        if (task == null) return null;

        var course = task.getCourse();

        return TaskAiContext.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDateTime(task.getDueDateTime())
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .estimatedMinutes(task.getEstimatedMinutes())

                .courseCode(course != null ? course.getCode() : null)
                .courseName(course != null ? course.getNameEnglish() : null)

                .labels(task.getLabels() == null ? List.of() :
                        task.getLabels().stream()
                                .map(label -> label.getName())
                                .toList())

                .subtasks(task.getSubtasks() == null ? List.of() :
                        task.getSubtasks().stream()
                                .map(subtask -> SubtaskAiContext.builder()
                                        .id(subtask.getId())
                                        .title(subtask.getTitle())
                                        .completed(subtask.getIsCompleted())
                                        .orderIndex(subtask.getOrderIndex())
                                        .build())
                                .toList())

                .attachments(task.getAttachments() == null ? List.of() :
                        task.getAttachments().stream()
                                .map(this::mapAttachment)
                                .toList())

                .build();
    }

    private AttachmentAiContext mapAttachment(TaskAttachment attachment) {
        return AttachmentAiContext.builder()
                .id(attachment.getId())
                .type(attachment.getKind() != null ? attachment.getKind().name() : null)
                .fileName(attachment.getFileName())
                .mimeType(attachment.getMimeType())
                .fileSizeBytes(attachment.getFileSizeBytes())
                .isVoiceNote(attachment.getIsVoiceNote())
                .durationSeconds(attachment.getDurationSeconds())
                .build();
    }
}