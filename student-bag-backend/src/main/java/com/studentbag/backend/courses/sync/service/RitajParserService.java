package com.studentbag.backend.courses.sync.service;

import com.studentbag.backend.courses.sync.dto.RitajCourseDto;

import java.util.List;

public interface RitajParserService {
    /**
     * يحلل محتوى JSON المدمج (courses_v5.json) ويُنتج قائمة من RitajCourseDto.
     *
     * @param jsonContent محتوى الملف نصاً
     */
    List<RitajCourseDto> parseJson(String jsonContent);

    /** للتوافق مع الكود القديم - مهمل */
    @Deprecated
    List<RitajCourseDto> parseCourses(String arContent, String enContent);
}