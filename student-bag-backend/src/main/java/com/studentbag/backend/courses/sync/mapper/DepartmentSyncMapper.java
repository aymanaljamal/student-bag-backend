package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import org.springframework.stereotype.Component;

@Component
public class DepartmentSyncMapper {

    public void map(RitajCourseDto dto, Department department, Faculty faculty) {
        department.setFaculty(faculty);

        // استخدام اسم القسم العربي كمعرف خارجي (يفضل إضافة اسم الكلية لضمان الفرادة)
        String deptNameAr = safeOrDefault(dto.getDepartmentNameArabic(), "Unknown Department");
        department.setExternalId(deptNameAr);

        department.setNameArabic(deptNameAr);

        // إذا لم يتوفر الاسم الإنجليزي، نستخدم العربي كبديل مؤقت بدلاً من null
        department.setNameEnglish(safeOrDefault(dto.getDepartmentNameEnglish(), deptNameAr));

        department.setProgramNameArabic(safeTrim(dto.getProgramNameArabic()));
        department.setProgramNameEnglish(safeTrim(dto.getProgramNameEnglish()));

        department.setIsActive(true);
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