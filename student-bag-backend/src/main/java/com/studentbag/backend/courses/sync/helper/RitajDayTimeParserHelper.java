package com.studentbag.backend.courses.sync.helper;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * هيلبر متخصص لتحليل نصوص الأيام والأوقات المستخرجة من نظام ريتاج (ملفات نصية).
 */
@Component
public class RitajDayTimeParserHelper {

    /**
     * يحول نص الأيام (مثل "S, M, W" أو "ن، ث، ر") إلى قائمة من DayOfWeek.
     */
    public List<DayOfWeek> parseDays(String rawDays) {
        List<DayOfWeek> result = new ArrayList<>();
        if (rawDays == null || rawDays.isBlank() || rawDays.equalsIgnoreCase("N/A")) {
            return result;
        }

        // التقسيم بناءً على الفواصل أو المسافات
        String[] parts = rawDays.split("[,\\s/]+");

        for (String part : parts) {
            DayOfWeek day = mapDayToken(part.trim());
            if (day != null && !result.contains(day)) {
                result.add(day);
            }
        }

        return result;
    }

    /**
     * يحول نص النطاق الزمني (مثل "13:00 - 13:50") إلى كائن TimeRange.
     */
    public TimeRange parseTimeRange(String rawTime) {
        if (rawTime == null || rawTime.isBlank() || rawTime.equalsIgnoreCase("N/A")) {
            return null;
        }

        // توحيد كافة أشكال الفواصل (بما في ذلك رموز Unicode للشرطات)
        String cleaned = rawTime.replace("–", "-")
                .replace("—", "-")
                .replace("−", "-")
                .replace("إلى", "-");

        String[] parts = cleaned.split("-");
        if (parts.length != 2) return null;

        LocalTime t1 = parseSingleTime(parts[0]);
        LocalTime t2 = parseSingleTime(parts[1]);

        if (t1 == null || t2 == null) return null;

        // معالجة مشكلة الوقت المقلوب (RTL): نضمن دائماً أن start هو الوقت الأبكر
        return t1.isAfter(t2) ? new TimeRange(t2, t1) : new TimeRange(t1, t2);
    }

    private LocalTime parseSingleTime(String value) {
        if (value == null || value.isBlank()) return null;

        try {
            // إبقاء الأرقام والنقطتين فقط
            String clean = value.trim().replaceAll("[^0-9:]", "");
            if (!clean.contains(":")) return null;

            String[] hm = clean.split(":");
            int hour = Integer.parseInt(hm[0].trim());
            int minute = Integer.parseInt(hm[1].trim());

            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null;
        }
    }

    private DayOfWeek mapDayToken(String token) {
        if (token == null || token.isBlank()) return null;

        String clean = token.toUpperCase();

        // دعم رموز ريتاج الإنجليزية (مهم جداً للملف النصي)
        return switch (clean) {
            case "S", "SAT", "السبت" -> DayOfWeek.SATURDAY;
            case "U", "SUN", "الأحد" -> DayOfWeek.SUNDAY;
            case "M", "MON", "الاثنين" -> DayOfWeek.MONDAY;
            case "T", "TUE", "الثلاثاء" -> DayOfWeek.TUESDAY;
            case "W", "WED", "الأربعاء" -> DayOfWeek.WEDNESDAY;
            case "R", "THU", "الخميس" -> DayOfWeek.THURSDAY; // R هي الشائعة للخميس في ريتاج لتجنب التكرار مع الثلاثاء
            case "F", "FRI", "الجمعة" -> DayOfWeek.FRIDAY;

            // دعم الاختصارات العربية المنفردة
            case "س" -> DayOfWeek.SATURDAY;
            case "ح" -> DayOfWeek.SUNDAY;
            case "ن" -> DayOfWeek.MONDAY;
            case "ث" -> DayOfWeek.TUESDAY;
            case "ر" -> DayOfWeek.WEDNESDAY;
            case "خ" -> DayOfWeek.THURSDAY;
            case "ج" -> DayOfWeek.FRIDAY;

            default -> null;
        };
    }

    public record TimeRange(LocalTime start, LocalTime end) {}
}