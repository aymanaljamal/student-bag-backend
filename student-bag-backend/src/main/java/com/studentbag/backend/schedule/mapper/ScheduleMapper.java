package com.studentbag.backend.schedule.mapper;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.schedule.dto.CourseSectionDTO;
import com.studentbag.backend.schedule.dto.ClassSessionDTO;
import com.studentbag.backend.schedule.dto.ScheduleEntryDTO;
import com.studentbag.backend.schedule.dto.response.StudentScheduleResponseDTO;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ScheduleMapper {

    // --- Schedule & Entry Mappings ---

    public StudentScheduleResponseDTO toResponseDTO(StudentSchedule schedule) {
        if (schedule == null) return null;

        return StudentScheduleResponseDTO.builder()
                .id(schedule.getId())
                .termId(schedule.getTerm().getId())
                .termName(schedule.getTerm().getName())
                .status(schedule.getStatus())
                .entries(schedule.getEntries().stream()
                        .map(this::toEntryDTO)
                        .collect(Collectors.toList()))
                .build();
    }

    public ScheduleEntryDTO toEntryDTO(ScheduleEntry entry) {
        ScheduleEntryDTO dto = new ScheduleEntryDTO();
        dto.setId(entry.getId());
        dto.setTitle(entry.getTitle());
        dto.setLocation(entry.getLocation());
        dto.setStart(entry.getStartDateTime());
        dto.setEnd(entry.getEndDateTime());
        dto.setColorHex(entry.getColorHex());
        dto.setSourceType(entry.getSourceType().name());
        dto.setIsLocked(entry.getIsLocked());
        return dto;
    }

    // --- Course & Section Mappings (Needed by TimetableMapper) ---

    public CourseSectionDTO toSectionDTO(CourseSection section) {
        if (section == null) return null;

        CourseSectionDTO dto = new CourseSectionDTO();
        dto.setSectionId(section.getId());
        dto.setSectionNumber(section.getSectionNumber());

        if (section.getCourse() != null) {
            dto.setCourseCode(section.getCourse().getCode());
            dto.setCourseName(section.getCourse().getNameEnglish());
        }

        dto.setInstructorName(section.getInstructor() != null ?
                section.getInstructor().getFullNameEnglish(): "TBA");

        if (section.getClassSessions() != null) {
            dto.setSessions(section.getClassSessions().stream()
                    .map(this::toSessionDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public ClassSessionDTO toSessionDTO(ClassSession session) {
        ClassSessionDTO dto = new ClassSessionDTO();
        dto.setDay(session.getDayOfWeek());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setRoom(session.getRoom());
        dto.setBuilding(session.getBuilding());
        return dto;
    }
}