package com.studentbag.backend.domain.enums.grades;

public enum GradeCalculationType {
    SEMESTER_GPA,          // معدل فصل
    CUMULATIVE_GPA,        // معدل تراكمي
    WEIGHTED_PERCENTAGE,   // معدل موزون من 100
    SIMPLE_PERCENTAGE,     // معدل بسيط غير موزون
    CUSTOM                 // توسعة مستقبلية
}