package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

@Component
public class FacultySyncMapper {

    public void map(RitajCourseDto dto, Faculty faculty, Institution institution) {
        faculty.setInstitution(institution);

        String facultyNameAr = safeOrDefault(dto.getFacultyNameArabic(), "Unknown Faculty");

        // استخدام الاسم العربي كمعرف خارجي فريد
        faculty.setExternalId(facultyNameAr);
        faculty.setNameArabic(facultyNameAr);

        // إذا لم يتوفر الاسم الإنجليزي (عند السحب من الصفحة العربية فقط)، نستخدم العربي كبديل
        faculty.setNameEnglish(safeOrDefault(dto.getFacultyNameEnglish(), facultyNameAr));

        faculty.setIsActive(true);
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