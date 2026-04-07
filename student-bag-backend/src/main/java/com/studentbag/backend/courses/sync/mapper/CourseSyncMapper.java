package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.helper.CourseCodeMetadataHelper;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

@Component
public class CourseSyncMapper {

    private final CourseCodeMetadataHelper courseCodeMetadataHelper;

    public CourseSyncMapper(CourseCodeMetadataHelper courseCodeMetadataHelper) {
        this.courseCodeMetadataHelper = courseCodeMetadataHelper;
    }

    public void map(RitajCourseDto dto, Course course, Institution institution, Department department) {
        String code = safeOrDefault(dto.getCode(), "UNKNOWN");

        course.setInstitution(institution);
        course.setDepartment(department);
        course.setExternalId(safeTrim(dto.getExternalId()));
        course.setCode(code);

        // ضمان وجود أسماء حتى لو لم يتم العثور عليها في الـ HTML
        course.setNameArabic(safeOrDefault(dto.getNameArabic(), code));
        course.setNameEnglish(safeOrDefault(dto.getNameEnglish(), code));

        course.setDescription(safeTrim(dto.getDescription()));
        course.setProgramNameArabic(safeTrim(dto.getProgramNameArabic()));
        course.setProgramNameEnglish(safeTrim(dto.getProgramNameEnglish()));

        // الأولوية للبيانات المستخرجة، ثم الاستنتاج من الكود
        Integer creditHours = dto.getCreditHours();
        if (creditHours == null || creditHours == 0) {
            creditHours = courseCodeMetadataHelper.extractCreditHours(code);
        }
        course.setCreditHours(creditHours);

        // استخراج المستوى الأكاديمي (سنة 1، 2، إلخ)
        course.setLevel(courseCodeMetadataHelper.extractAcademicLevel(code));

        course.setIsActive(true);
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
}