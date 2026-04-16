package com.studentbag.backend.domain.enums.grades;

public enum GradeInputType {
    MARK_OUT_OF_100,   // الطالب يدخل 85 / 100
    GPA_OUT_OF_4,      // الطالب يدخل 3.25 / 4
    GPA_OUT_OF_5,      // لو بدك توسع مستقبلاً
    LETTER_GRADE       // A, B+, ...
}