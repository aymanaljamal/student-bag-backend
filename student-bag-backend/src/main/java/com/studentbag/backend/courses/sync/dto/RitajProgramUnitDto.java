package com.studentbag.backend.courses.sync.dto;

import lombok.Builder;
import lombok.Data;

/**
 * يمثّل وحدة برنامج واحدة مستخرجة من شجرة صفحة الـ index.
 *
 * كل وحدة تحتوي على:
 *  - اسم الكلية   (فارغ إذا كان البرنامج مباشرةً تحت الكلية)
 *  - اسم الدائرة  (فارغ إذا لم يكن هناك دائرة)
 *  - اسم البرنامج
 *  - bu_id        (يُستخدم لجلب الكورسات عبر /hemis/bu-courses-list?bu=<buId>)
 */
@Data
@Builder
public class RitajProgramUnitDto {

    /** اسم الكلية — مثال: "كلية الهندسة والتكنولوجيا" */
    private String facultyName;

    /** اسم الدائرة — مثال: "دائرة هندسة الحاسوب" */
    private String departmentName;

    /** اسم البرنامج — مثال: "مساقات مستوى درجة البكالوريوس في هندسة الحاسوب COMP" */
    private String programName;

    /**
     * معرّف الوحدة الأكاديمية في نظام Ritaj/HEMIS.
     * يُستخدم في URL: /hemis/bu-courses-list?term=<termId>&bu=<buId>
     */
    private String buId;
}