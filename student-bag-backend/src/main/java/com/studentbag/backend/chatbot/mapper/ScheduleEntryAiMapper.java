package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.ScheduleEntryAiContext;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import org.springframework.stereotype.Component;

@Component
public class ScheduleEntryAiMapper {

    public ScheduleEntryAiContext toContext(ScheduleEntry entry) {
        if (entry == null) return null;

        var section = entry.getCourseSection();
        var course = section != null ? section.getCourse() : null;
        var instructor = section != null ? section.getInstructor() : null;

        return ScheduleEntryAiContext.builder()
                .id(entry.getId())
                .title(entry.getTitle())
                .description(entry.getDescription())
                .location(entry.getLocation())
                .sourceType(entry.getSourceType() != null ? entry.getSourceType().name() : null)
                .startDateTime(entry.getStartDateTime())
                .endDateTime(entry.getEndDateTime())
                .isAllDay(entry.getIsAllDay())

                .courseCode(course != null ? course.getCode() : null)
                .courseName(course != null ? course.getNameEnglish() : null)
                .sectionNumber(section != null ? section.getSectionNumber() : null)

                .instructorName(instructor != null && instructor.getUser() != null
                        ? instructor.getUser().getFullName()
                        : null)

                .room(entry.getLocation())
                .building(null)
                .campus(null)
                .build();
    }
}