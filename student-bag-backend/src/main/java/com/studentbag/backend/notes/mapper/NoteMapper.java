package com.studentbag.backend.notes.mapper;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.notes.dto.request.NoteAttachmentRequest;
import com.studentbag.backend.notes.dto.request.NoteRequest;
import com.studentbag.backend.notes.dto.response.NoteAttachmentResponse;
import com.studentbag.backend.notes.dto.response.NoteResponse;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.student.entity.Student;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NoteMapper {

    public Note toEntity(NoteRequest request, Student student, Course course) {
        Note note = new Note();
        note.setStudent(student);
        note.setCourse(course);
        note.setTitle(request.getTitle());
        note.setContentHtml(request.getContentHtml());
        note.setIsImportant(request.getIsImportant() != null ? request.getIsImportant() : false);
        note.setIsDeleted(request.getIsDeleted() != null ? request.getIsDeleted() : false);
        note.setTags(request.getTags());
        return note;
    }

    public void updateEntity(Note note, NoteRequest request, Student student, Course course) {
        note.setStudent(student);
        note.setCourse(course);
        note.setTitle(request.getTitle());
        note.setContentHtml(request.getContentHtml());
        note.setIsImportant(request.getIsImportant() != null ? request.getIsImportant() : note.getIsImportant());
        note.setIsDeleted(request.getIsDeleted() != null ? request.getIsDeleted() : note.getIsDeleted());
        note.setTags(request.getTags());
    }

    public NoteAttachment toAttachmentEntity(NoteAttachmentRequest request, Note note) {
        NoteAttachment attachment = new NoteAttachment();
        attachment.setNote(note);
        attachment.setType(request.getType());
        attachment.setUrl(request.getUrl());
        attachment.setFileName(request.getFileName());
        attachment.setDurationSeconds(request.getDurationSeconds());
        attachment.setFileSizeBytes(request.getFileSizeBytes());
        return attachment;
    }

    public NoteAttachmentResponse toAttachmentResponse(NoteAttachment attachment) {
        return NoteAttachmentResponse.builder()
                .id(attachment.getId())
                .noteId(attachment.getNote() != null ? attachment.getNote().getId() : null)
                .type(attachment.getType())
                .url(attachment.getUrl())
                .fileName(attachment.getFileName())
                .durationSeconds(attachment.getDurationSeconds())
                .fileSizeBytes(attachment.getFileSizeBytes())
                .createdAt(attachment.getCreatedAt())
                .updatedAt(attachment.getUpdatedAt())
                .build();
    }

    public NoteResponse toResponse(Note note, List<NoteAttachment> attachments) {
        return NoteResponse.builder()
                .id(note.getId())
                .studentId(note.getStudent() != null ? note.getStudent().getId() : null)
                .courseId(note.getCourse() != null ? note.getCourse().getId() : null)
                .title(note.getTitle())
                .contentHtml(note.getContentHtml())
                .isImportant(note.getIsImportant())
                .isDeleted(note.getIsDeleted())
                .tags(note.getTags())
                .attachments(
                        attachments == null
                                ? List.of()
                                : attachments.stream().map(this::toAttachmentResponse).toList()
                )
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}