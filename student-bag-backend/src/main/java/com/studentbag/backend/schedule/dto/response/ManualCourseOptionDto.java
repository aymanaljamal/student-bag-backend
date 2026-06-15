package com.studentbag.backend.schedule.dto.response;

public record ManualCourseOptionDto(
        Long courseId,
        String courseCode,
        String courseNameArabic,
        String courseNameEnglish,
        Integer creditHours
) {
}