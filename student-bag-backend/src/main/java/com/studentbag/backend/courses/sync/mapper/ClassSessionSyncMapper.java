package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Component
public class ClassSessionSyncMapper {

    public ClassSession toEntity(RitajClassSessionDto dto, CourseSection section) {
        if (dto == null) {
            throw new IllegalArgumentException("RitajClassSessionDto cannot be null");
        }

        DayOfWeek dayOfWeek = dto.getDayOfWeek();
        LocalTime startTime = dto.getStartTime();
        LocalTime endTime = dto.getEndTime();

        if (dayOfWeek == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException(
                    "Invalid class session time for section: " + section.getExternalId()
            );
        }

        ClassSession session = new ClassSession();
        session.setCourseSection(section);
        session.setDayOfWeek(dayOfWeek);
        session.setStartTime(startTime);
        session.setEndTime(endTime);

        session.setRoom(safeOrDefault(dto.getRoom(), "001"));
        session.setBuilding(safeOrDefault(dto.getBuilding(), "Al-Juraysi"));
        session.setCampus(safeOrDefault(dto.getCampus(), "Main Campus"));

        boolean isOnline = isOnline(dto.getBuilding()) || isOnline(dto.getRoom()) || isOnline(dto.getCampus());
        session.setIsOnline(isOnline);

        return session;
    }

    private boolean isOnline(String value) {
        if (value == null) return false;
        String normalized = value.trim().toUpperCase();
        return normalized.contains("ONLINE") || normalized.contains("عن بعد");
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() || trimmed.equalsIgnoreCase("N/A") ? null : trimmed;
    }

    private String safeOrDefault(String value, String defaultValue) {
        String trimmed = safeTrim(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}
