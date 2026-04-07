package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.ClassSessionRequestDTO;
import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;
import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between ClassSession entity and DTOs
 */
@Component
public class ClassSessionMapper {

    /**
     * Convert Request DTO to Entity
     */
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

    /**
     * Convert Entity to Response DTO
     */
    public ClassSessionResponseDTO toResponse(ClassSession session) {
        return ClassSessionResponseDTO.builder()
                .id(session.getId())
                .courseSectionId(session.getCourseSection().getId())
                .dayOfWeek(session.getDayOfWeek())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .room(session.getRoom())
                .building(session.getBuilding())
                .campus(session.getCampus())
                .isOnline(session.getIsOnline())
                .durationMinutes(session.getDurationMinutes())
                .build();
    }
}