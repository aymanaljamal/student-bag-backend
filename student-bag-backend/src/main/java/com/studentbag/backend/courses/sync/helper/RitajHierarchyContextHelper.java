package com.studentbag.backend.courses.sync.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class RitajHierarchyContextHelper {

    /**
     * في ملفات الـ txt، السياق يظهر كأسطر تسبق المساقات.
     * هذا الميثود سيحاول استخراج الكلية أو القسم من سطر نصي معين.
     */
    public HierarchyContext extractContextFromLine(String line, HierarchyContext currentContext) {
        if (line == null || line.isBlank()) return currentContext;

        String text = normalize(line);

        // إذا وجدنا سطرًا يبدو ككلية، نحدث السياق
        if (looksLikeFaculty(text)) {
            currentContext.setFacultyName(text);
            // عند تغيير الكلية، نصفر القسم والبرنامج لضمان الدقة
            currentContext.setDepartmentName(null);
            currentContext.setProgramName(null);
        }
        // إذا وجدنا سطرًا يبدو كدائرة أو قسم
        else if (looksLikeDepartment(text)) {
            currentContext.setDepartmentName(text);
        }
        // إذا وجدنا سطرًا يبدو كبرنامج دراسي
        else if (looksLikeProgram(text)) {
            currentContext.setProgramName(text);
        }

        return currentContext;
    }

    private boolean looksLikeFaculty(String text) {
        if (text == null || text.length() > 100) return false;
        String lower = text.toLowerCase();
        return text.contains("كلية") || lower.contains("faculty of");
    }

    private boolean looksLikeDepartment(String text) {
        if (text == null || text.length() > 100) return false;
        String lower = text.toLowerCase();
        return text.contains("دائرة") || text.contains("قسم") || lower.contains("department of");
    }

    private boolean looksLikeProgram(String text) {
        if (text == null || text.length() > 150) return false;
        String lower = text.toLowerCase();
        return text.contains("برنامج") || lower.contains("program") || lower.contains("bachelor");
    }

    private String normalize(String value) {
        if (value == null) return null;
        // تنظيف المسافات والرموز الغريبة
        return value.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class HierarchyContext {
        private String facultyName;
        private String departmentName;
        private String programName;
    }
}