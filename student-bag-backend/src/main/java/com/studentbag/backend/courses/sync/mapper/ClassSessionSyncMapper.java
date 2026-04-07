package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import org.springframework.stereotype.Component;

@Component
public class ClassSessionSyncMapper {

    public ClassSession toEntity(RitajClassSessionDto dto, CourseSection section) {
        ClassSession session = new ClassSession();
        session.setCourseSection(section);
        session.setDayOfWeek(dto.getDayOfWeek());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());

        // انقل البيانات مباشرة من الـ DTO لأنها حُللت مسبقاً في السيرفس
        session.setBuilding(dto.getBuilding()); // سيأخذ "A.Shaheen152"
        session.setRoom(dto.getRoom());         // سيأخذ "152"

        // التحقق من الأونلاين بناءً على القيمة المخزنة في الـ Building
        boolean isOnline = "ONLINE".equalsIgnoreCase(dto.getBuilding()) ||
                (dto.getBuilding() != null && dto.getBuilding().contains("عن بعد"));
        session.setIsOnline(isOnline);

        session.setCampus(safeTrim(dto.getCampus()));

        return session;
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return (trimmed.isEmpty() || trimmed.equalsIgnoreCase("N/A")) ? null : trimmed;
    }
}