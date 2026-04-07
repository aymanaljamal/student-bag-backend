package com.studentbag.backend.courses.sync.helper;

import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RitajCourseMergeHelper {

    public List<RitajCourseDto> mergeArabicAndEnglish(List<RitajCourseDto> arabicCourses, List<RitajCourseDto> englishCourses) {
        // فهرسة المساقات العربية
        Map<String, RitajCourseDto> arabicMap = arabicCourses.stream()
                .filter(c -> c.getCode() != null)
                .collect(Collectors.toMap(
                        c -> c.getCode().trim().toUpperCase(),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        // فهرسة المساقات الإنجليزية
        Map<String, RitajCourseDto> englishMap = englishCourses.stream()
                .filter(c -> c.getCode() != null)
                .collect(Collectors.toMap(
                        c -> c.getCode().trim().toUpperCase(),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        Set<String> allCodes = new LinkedHashSet<>();
        allCodes.addAll(arabicMap.keySet());
        allCodes.addAll(englishMap.keySet());

        List<RitajCourseDto> merged = new ArrayList<>();

        for (String code : allCodes) {
            RitajCourseDto ar = arabicMap.get(code);
            RitajCourseDto en = englishMap.get(code);

            RitajCourseDto target = new RitajCourseDto();
            target.setCode(code);
            target.setExternalId(code);

            // دمج الأسماء (الأولوية للعربي في الحقل العربي وللإنجليزي في الحقل الإنجليزي)
            target.setNameArabic(firstNonBlank(get(ar, RitajCourseDto::getNameArabic), get(en, RitajCourseDto::getNameArabic)));
            target.setNameEnglish(firstNonBlank(get(en, RitajCourseDto::getNameEnglish), get(ar, RitajCourseDto::getNameEnglish)));

            // دمج الكليات والأقسام
            target.setFacultyNameArabic(firstNonBlank(get(ar, RitajCourseDto::getFacultyNameArabic), "كلية عامة"));
            target.setFacultyNameEnglish(firstNonBlank(get(en, RitajCourseDto::getFacultyNameEnglish), "General Faculty"));

            target.setDepartmentNameArabic(firstNonBlank(get(ar, RitajCourseDto::getDepartmentNameArabic), "دائرة عامة"));
            target.setDepartmentNameEnglish(firstNonBlank(get(en, RitajCourseDto::getDepartmentNameEnglish), "General Department"));

            // دمج الشعب (Sections)
            target.setSections(mergeSections(
                    ar == null ? List.of() : ar.getSections(),
                    en == null ? List.of() : en.getSections()
            ));

            merged.add(target);
        }

        return merged;
    }

    private List<RitajSectionDto> mergeSections(List<RitajSectionDto> arabicSections, List<RitajSectionDto> englishSections) {
        Map<String, RitajSectionDto> arabicMap = indexSections(arabicSections);
        Map<String, RitajSectionDto> englishMap = indexSections(englishSections);

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(arabicMap.keySet());
        allKeys.addAll(englishMap.keySet());

        List<RitajSectionDto> merged = new ArrayList<>();

        for (String key : allKeys) {
            RitajSectionDto ar = arabicMap.get(key);
            RitajSectionDto en = englishMap.get(key);

            RitajSectionDto target = new RitajSectionDto();
            String secNum = firstNonBlank(get(ar, RitajSectionDto::getSectionNumber), get(en, RitajSectionDto::getSectionNumber));
            target.setSectionNumber(secNum);
            target.setSectionType(firstNonBlank(get(ar, RitajSectionDto::getSectionType), get(en, RitajSectionDto::getSectionType)));

            // أسماء المدرسين
            target.setInstructorNameArabic(firstNonBlank(get(ar, RitajSectionDto::getInstructorNameArabic), get(en, RitajSectionDto::getInstructorNameArabic)));
            target.setInstructorNameEnglish(firstNonBlank(get(en, RitajSectionDto::getInstructorNameEnglish), get(ar, RitajSectionDto::getInstructorNameEnglish)));

            target.setEnrolled(firstNonNull(get(ar, RitajSectionDto::getEnrolled), get(en, RitajSectionDto::getEnrolled)));

            // دمج المواعيد (Sessions): نفضل النسخة الإنجليزية لأن أسماء القاعات فيها أدق برمجياً
            target.setSessions(mergeSessions(
                    ar == null ? List.of() : ar.getSessions(),
                    en == null ? List.of() : en.getSessions()
            ));

            merged.add(target);
        }
        return merged;
    }

    private List<RitajClassSessionDto> mergeSessions(List<RitajClassSessionDto> arSessions, List<RitajClassSessionDto> enSessions) {
        // إذا توفرت النسخة الإنجليزية نأخذها لأن القاعات تكون مكتوبة بترميز أوضح (مثل Khoury010)
        if (!enSessions.isEmpty()) {
            return enSessions;
        }
        return arSessions;
    }

    private Map<String, RitajSectionDto> indexSections(List<RitajSectionDto> sections) {
        Map<String, RitajSectionDto> map = new LinkedHashMap<>();
        for (RitajSectionDto section : sections) {
            // المفتاح هو رقم الشعبة (لأنه فريد داخل المساق الواحد في ريتاج)
            String key = normalize(section.getSectionNumber());
            if (key != null) map.putIfAbsent(key, section);
        }
        return map;
    }

    private String normalize(String value) {
        if (value == null) return null;
        return value.trim().toUpperCase();
    }

    private <T, R> R get(T source, Function<T, R> fn) {
        return source == null ? null : fn.apply(source);
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) if (value != null) return value;
        return null;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) if (value != null && !value.isBlank()) return value.trim();
        return null;
    }
}