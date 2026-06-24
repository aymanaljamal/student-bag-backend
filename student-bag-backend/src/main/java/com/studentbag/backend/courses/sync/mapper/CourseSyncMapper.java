package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.helper.CourseCodeMetadataHelper;
import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

@Component
public class CourseSyncMapper {

    private final CourseCodeMetadataHelper courseCodeMetadataHelper;

    public CourseSyncMapper(CourseCodeMetadataHelper courseCodeMetadataHelper) {
        this.courseCodeMetadataHelper = courseCodeMetadataHelper;
    }

    public void map(RitajCourseDto dto, Course course, Institution institution, Department department) {
        String originalCode = safeOrDefault(dto.getCode(), "UNKNOWN");
        String storageCode = safeOrDefault(dto.getStorageCode(), safeOrDefault(dto.getCourseInternalId(), originalCode));

        course.setInstitution(institution);
        course.setDepartment(department);

        /*
         * Important:
         * - course.code has a unique constraint with institution_id.
         * - duplicated special-topic courses such as COMP438 cannot all be saved as COMP438.
         * - therefore storageCode is used for the DB code.
         * - the original Ritaj code is preserved in externalId.
         */
        course.setExternalId(originalCode);
        course.setCode(storageCode);

        course.setNameArabic(safeOrDefault(dto.getNameArabic(), originalCode));
        course.setNameEnglish(safeOrDefault(dto.getNameEnglish(), originalCode));

        course.setDescription(firstNonBlank(
                dto.getDescription(),
                dto.getDescriptionEnglish(),
                dto.getDescriptionArabic()
        ));

        course.setProgramNameArabic(safeTrim(dto.getProgramNameArabic()));
        course.setProgramNameEnglish(safeTrim(dto.getProgramNameEnglish()));

        Integer creditHours = dto.getCreditHours();
        if (creditHours == null || creditHours <= 0) {
            creditHours = courseCodeMetadataHelper.extractCreditHours(originalCode);
        }
        course.setCreditHours(creditHours);

        course.setLevel(mapAcademicLevel(dto.getAcademicLevel(), originalCode));
        course.setIsActive(true);
    }

    private AcademicLevel mapAcademicLevel(String rawLevel, String courseCode) {
        if (rawLevel != null && !rawLevel.isBlank()) {
            try {
                return AcademicLevel.valueOf(rawLevel.trim().toUpperCase());
            } catch (Exception ignored) {
                // fallback below
            }
        }

        AcademicLevel fromCode = courseCodeMetadataHelper.extractAcademicLevel(courseCode);
        return fromCode == null ? AcademicLevel.FIRST_YEAR : fromCode;
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
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
