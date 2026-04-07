package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;
import com.studentbag.backend.courses.sync.service.RitajDualLanguageParseService;
import com.studentbag.backend.courses.sync.service.RitajFetchService;
import com.studentbag.backend.courses.sync.service.RitajParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor // استخدمنا اللومبوك لتقليل الكود الممل (Constructor)
public class RitajDualLanguageParseServiceImpl implements RitajDualLanguageParseService {

    private final RitajFetchService ritajFetchService;
    private final RitajParserService ritajParserService;
    // ملاحظة: قمنا بحذف RitajCourseMergeHelper لأن الـ Parser صار يقوم بالدمج تلقائياً

    @Override
    public List<RitajCourseDto> fetchParseAndMerge(String baseFileName) {
        log.info("[Dual Parser] بدء جلب وتحليل الملفات الموحدة للملف: {}", baseFileName);

        // 1. جلب محتوى الملفات النصية (Arabic & English)
        RitajFetchedPagesDto pages = ritajFetchService.fetchArabicAndEnglishPages(baseFileName);

        List<RitajCourseDto> mergedCourses = new ArrayList<>();
        try {
            // 2. السحر هنا: نرسل الملفين معاً للـ Parser المطور
            // الـ Parser الآن هو المسؤول عن الـ Mapping والدمج والأسماء الإنجليزية
            mergedCourses = ritajParserService.parseCourses(
                    pages.getArabicContent(),
                    pages.getEnglishContent()
            );

            log.info("[Dual Parser] تم التحليل والدمج التلقائي لـ {} مساق بنجاح", mergedCourses.size());
        } catch (Exception e) {
            log.error("[Dual Parser] فشل في عملية التحليل الموحدة: {}", e.getMessage());
        }

        return mergedCourses;
    }
}