package com.studentbag.backend.courses.sync.service;

import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;

public interface RitajFetchService {
    /**
     * يقرأ ملف JSON المدمج (courses_v5.json) من مجلد resources/static
     *
     * @param baseFileName اسم الملف بدون امتداد، مثل "courses_v5"
     */
    RitajFetchedPagesDto fetchJsonFile(String baseFileName);

    /** للتوافق مع الكود القديم - مهمل */
    @Deprecated
    RitajFetchedPagesDto fetchArabicAndEnglishPages(String baseFileName);
}