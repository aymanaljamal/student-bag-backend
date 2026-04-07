package com.studentbag.backend.courses.sync.service;

import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;

public interface RitajFetchService {

    /**
     * يقرأ محتوى ملف نصي من الـ static folder داخل المشروع.
     * @param fileName اسم الملف (مثلاً: Course_en.txt)
     * @return محتوى الملف كنص
     */
    String fetchText(String fileName);

    /**
     * يقرأ نسختي الملف (العربية والإنجليزية) بناءً على اسم أساسي.
     * @param baseFileName الاسم الأساسي للملف (مثلاً: Course)
     * @return DTO يحتوي على النصوص من الملفين
     */
    RitajFetchedPagesDto fetchArabicAndEnglishPages(String baseFileName);
}