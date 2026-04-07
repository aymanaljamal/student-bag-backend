package com.studentbag.backend.courses.sync.service;

import com.studentbag.backend.courses.sync.dto.RitajCourseDto;

import java.util.List;

public interface RitajParserService {

    /**
     * يقوم بتحليل مساقات ريتاج من ملفين (عربي وإنجليزي) لدمج البيانات والأسماء
     * @param arContent محتوى ملف Course_ar.txt
     * @param enContent محتوى ملف Course_en.txt
     * @return قائمة بالمساقات المجهزة للادخال في قاعدة البيانات
     */
    List<RitajCourseDto> parseCourses(String arContent, String enContent);
}