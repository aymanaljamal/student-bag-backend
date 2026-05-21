package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.ResourceAiContext;
import com.studentbag.backend.resources.entity.PersonalResourceItem;
import org.springframework.stereotype.Component;

@Component
public class ResourceAiMapper {

    public ResourceAiContext toContext(PersonalResourceItem item) {
        if (item == null) return null;

        var course = item.getCourse();
        var folder = item.getFolder();

        return ResourceAiContext.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .resourceType(item.getResourceType() != null ? item.getResourceType().name() : null)
                .category(item.getCategory() != null ? item.getCategory().name() : null)

                .courseCode(course != null ? course.getCode() : null)
                .courseName(course != null ? course.getNameEnglish() : null)

                .fileName(item.getFileName())
                .mimeType(item.getMimeType())
                .fileSizeBytes(item.getFileSizeBytes())

                .hasFile(item.getFileUrl() != null && !item.getFileUrl().isBlank())
                .hasExternalLink(item.getExternalLink() != null && !item.getExternalLink().isBlank())

                .folderName(folder != null ? folder.getName() : null)
                .linkedNoteId(item.getLinkedNoteId())
                .linkedTaskId(item.getLinkedTaskId())
                .build();
    }
}