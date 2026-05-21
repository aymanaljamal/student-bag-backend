package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.AttachmentAiContext;
import com.studentbag.backend.chatbot.dto.context.NoteAiContext;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;
import java.util.List;
@Component
public class NoteAiMapper {

    public NoteAiContext toContext(Note note) {
        if (note == null) return null;

        var course = note.getCourse();

        return NoteAiContext.builder()
                .id(note.getId())
                .title(note.getTitle())
                .contentText(cleanHtml(note.getContentHtml()))
                .important(note.getIsImportant())
                .pinned(note.getIsPinned())
                .priority(note.getPriority() != null ? note.getPriority().name() : null)
                .noteType(note.getNoteType() != null ? note.getNoteType().name() : null)
                .tags(note.getTags())

                .courseCode(course != null ? course.getCode() : null)
                .courseName(course != null ? course.getNameEnglish() : null)

                .attachments(note.getAttachments() == null ? List.of() :
                        note.getAttachments().stream()
                                .map(this::mapAttachment)
                                .toList())
                .build();
    }

    private AttachmentAiContext mapAttachment(NoteAttachment attachment) {
        return AttachmentAiContext.builder()
                .id(attachment.getId())
                .type(attachment.getType())
                .fileName(attachment.getFileName())
                .fileSizeBytes(attachment.getFileSizeBytes())
                .durationSeconds(attachment.getDurationSeconds())
                .build();
    }

    private String cleanHtml(String html) {
        if (html == null || html.isBlank()) return null;
        return Jsoup.parse(html).text();
    }
}