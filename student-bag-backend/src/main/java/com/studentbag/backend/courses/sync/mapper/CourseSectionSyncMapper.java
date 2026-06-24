package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import com.studentbag.backend.domain.enums.courses.SectionType;
import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class CourseSectionSyncMapper {

    public void map(
            RitajSectionDto dto,
            CourseSection section,
            Course course,
            Term term,
            Instructor instructor
    ) {
        section.setCourse(course);
        section.setTerm(term);

        section.setExternalId(firstNonBlank(
                dto.getSectionInternalId(),
                dto.getExternalId(),
                course.getCode() + "-" + safeOrDefault(dto.getSectionType(), "Lecture") + "-" + safeOrDefault(dto.getSectionNumber(), "1")
        ));

        section.setSectionNumber(safeOrDefault(dto.getSectionNumber(), "1"));
        section.setSectionType(mapSectionType(dto.getSectionType()));
        section.setInstructor(instructor);

        section.setCapacity(dto.getCapacity() == null || dto.getCapacity() <= 0 ? 40 : dto.getCapacity());
        section.setEnrolled(dto.getEnrolled() == null ? 0 : dto.getEnrolled());

        section.setIsOfficial(true);
    }

    public SectionType mapSectionType(String value) {
        if (value == null || value.isBlank()) {
            return SectionType.LECTURE;
        }

        String normalized = value.trim().toUpperCase();

        if (normalized.contains("DISCUSSION")
                || normalized.contains("DISC")
                || normalized.contains("نقاش")
                || normalized.contains("مناقشة")) {
            return SectionType.DISCUSSION;
        }

        if (normalized.contains("LAB") || normalized.contains("مختبر")) {
            return SectionType.LAB;
        }

        if (normalized.contains("SEMINAR") || normalized.contains("ندوة")) {
            return SectionType.SEMINAR;
        }

        if (normalized.contains("PRACTICAL")
                || normalized.contains("عملي")
                || normalized.contains("تدريب")) {
            return SectionType.PRACTICAL;
        }

        return SectionType.LECTURE;
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = safeTrim(value);
            if (trimmed != null) return trimmed;
        }

        return null;
    }
}
