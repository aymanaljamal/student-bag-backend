package com.studentbag.backend.tasks.mapper;

import com.studentbag.backend.tasks.dto.request.CreateTaskAttachmentRequest;
import com.studentbag.backend.tasks.dto.response.DeleteAttachmentResponse;
import com.studentbag.backend.tasks.dto.response.TaskAttachmentResponse;
import com.studentbag.backend.tasks.dto.response.TaskAttachmentUploadResponse;
import com.studentbag.backend.tasks.entity.TaskAttachment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class TaskAttachmentMapper {

    public TaskAttachment toEntity(CreateTaskAttachmentRequest request) {
        if (request == null) {
            return null;
        }

        return TaskAttachment.builder()
                .kind(request.getKind())
                .url(request.getUrl())
                .fileName(request.getFileName())
                .mimeType(request.getMimeType())
                .fileSizeBytes(request.getFileSizeBytes())
                .extension(request.getExtension())
                .isVoiceNote(Boolean.TRUE.equals(request.getIsVoiceNote()))
                .durationSeconds(request.getDurationSeconds())
                .build();
    }

    public TaskAttachmentResponse toResponse(TaskAttachment entity) {
        if (entity == null) {
            return null;
        }

        return TaskAttachmentResponse.builder()
                .id(entity.getId())
                .kind(entity.getKind())
                .url(entity.getUrl())
                .fileName(entity.getFileName())
                .mimeType(entity.getMimeType())
                .fileSizeBytes(entity.getFileSizeBytes())
                .extension(entity.getExtension())
                .isVoiceNote(entity.getIsVoiceNote())
                .durationSeconds(entity.getDurationSeconds())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public List<TaskAttachmentResponse> toResponseList(List<TaskAttachment> entities) {
        if (entities == null) {
            return List.of();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    public TaskAttachmentUploadResponse toUploadResponse(Long taskId, TaskAttachment entity, String message) {
        return TaskAttachmentUploadResponse.builder()
                .taskId(taskId)
                .attachment(toResponse(entity))
                .message(message)
                .build();
    }

    public DeleteAttachmentResponse toDeleteResponse(Long attachmentId, boolean deleted, String message) {
        return DeleteAttachmentResponse.builder()
                .attachmentId(attachmentId)
                .deleted(deleted)
                .message(message)
                .build();
    }
}