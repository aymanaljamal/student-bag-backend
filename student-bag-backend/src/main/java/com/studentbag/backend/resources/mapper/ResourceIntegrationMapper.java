package com.studentbag.backend.resources.mapper;

import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.resources.dto.response.LinkedNoteAttachmentResponse;
import com.studentbag.backend.resources.dto.response.LinkedNoteSummaryResponse;
import com.studentbag.backend.resources.dto.response.LinkedTaskAttachmentResponse;
import com.studentbag.backend.resources.dto.response.LinkedTaskSummaryResponse;
import com.studentbag.backend.tasks.entity.Task;
import com.studentbag.backend.tasks.entity.TaskAttachment;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ResourceIntegrationMapper {

    public LinkedNoteSummaryResponse toLinkedNoteResponse(Note note, List<NoteAttachment> attachments) {
        if (note == null) {
            return null;
        }

        return LinkedNoteSummaryResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .contentHtml(note.getContentHtml())
                .isImportant(note.getIsImportant())
                .isPinned(note.getIsPinned())
                .color(note.getColor())
                .tags(note.getTags())
                .priority(note.getPriority() != null ? note.getPriority().name() : null)
                .noteType(note.getNoteType() != null ? note.getNoteType().name() : null)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .attachments(
                        attachments == null ? List.of() :
                                attachments.stream().map(ResourceIntegrationMapper::toLinkedNoteAttachmentResponse).toList()
                )
                .build();
    }

    public LinkedNoteAttachmentResponse toLinkedNoteAttachmentResponse(NoteAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        return LinkedNoteAttachmentResponse.builder()
                .id(attachment.getId())
                .type(attachment.getType())
                .url(attachment.getUrl())
                .fileName(attachment.getFileName())
                .durationSeconds(attachment.getDurationSeconds())
                .fileSizeBytes(attachment.getFileSizeBytes())
                .build();
    }

    public LinkedTaskSummaryResponse toLinkedTaskResponse(Task task) {
        if (task == null) {
            return null;
        }

        return LinkedTaskSummaryResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus() != null ? task.getStatus().name() : null)
                .priority(task.getPriority() != null ? task.getPriority().name() : null)
                .dueDateTime(task.getDueDateTime())
                .archived(task.getArchived())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .attachments(
                        task.getAttachments() == null ? List.of() :
                                task.getAttachments().stream().map(ResourceIntegrationMapper::toLinkedTaskAttachmentResponse).toList()
                )
                .build();
    }

    public LinkedTaskAttachmentResponse toLinkedTaskAttachmentResponse(TaskAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        return LinkedTaskAttachmentResponse.builder()
                .id(attachment.getId())
                .url(attachment.getUrl())
                .fileName(attachment.getFileName())
                .mimeType(attachment.getMimeType())
                .extension(attachment.getExtension())
                .fileSizeBytes(attachment.getFileSizeBytes())
                .build();
    }
}