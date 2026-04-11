package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.ClassSessionRequestDTO;
import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;
import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import org.springframework.stereotype.Component;

@Component
public class ClassSessionMapper {

    public void toEntity(ClassSessionRequestDTO request, ClassSession session, CourseSection section) {
        session.setCourseSection(section);
        session.setDayOfWeek(request.getDayOfWeek());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setRoom(request.getRoom());
        session.setBuilding(request.getBuilding());
        session.setCampus(request.getCampus());
        session.setIsOnline(request.getIsOnline());
    }

    public ClassSessionResponseDTO toResponse(ClassSession session) {
        if (session == null) {
            return null;
        }

        CourseSection sourceSection = session.getCourseSection();
        boolean isLab = sourceSection != null && sourceSection.getParentLectureSection() != null;

        return ClassSessionResponseDTO.builder()
                .id(session.getId())
                .courseSectionId(sourceSection != null ? sourceSection.getId() : null)
                .dayOfWeek(session.getDayOfWeek())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .room(session.getRoom())
                .building(session.getBuilding())
                .campus(session.getCampus())
                .isOnline(session.getIsOnline())
                .durationMinutes(session.getDurationMinutes())

                .isLab(isLab)
                .sourceSectionId(sourceSection != null ? sourceSection.getId() : null)
                .sourceSectionNumber(sourceSection != null ? sourceSection.getSectionNumber() : null)
                .sourceSectionType(
                        sourceSection != null && sourceSection.getSectionType() != null
                                ? sourceSection.getSectionType().name()
                                : null
                )
                .parentLectureSectionId(
                        sourceSection != null && sourceSection.getParentLectureSection() != null
                                ? sourceSection.getParentLectureSection().getId()
                                : null
                )
                .build();
    }
}