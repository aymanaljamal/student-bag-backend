package com.studentbag.backend.courses.sync.helper;

import com.studentbag.backend.domain.enums.AcademicLevel;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CourseCodeMetadataHelper {

    // نمط لاستخراج الأرقام فقط من رمز المساق (مثل ARCH131 يستخرج 131)
    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    /**
     * يستخرج المستوى الأكاديمي بناءً على الرقم الأول من رمز المساق.
     */
    public AcademicLevel extractAcademicLevel(String courseCode) {
        String digits = extractDigits(courseCode);
        if (digits == null || digits.isEmpty()) {
            return AcademicLevel.FIRST_YEAR; // قيمة افتراضية
        }

        char firstChar = digits.charAt(0);

        return switch (firstChar) {
            case '1' -> AcademicLevel.FIRST_YEAR;
            case '2' -> AcademicLevel.SECOND_YEAR;
            case '3' -> AcademicLevel.THIRD_YEAR;
            case '4' -> AcademicLevel.FOURTH_YEAR;
            case '5' -> AcademicLevel.FIFTH_YEAR;
            // يمكن إضافة حالات أخرى للدراسات العليا إذا كان نظامك يدعمها
            default -> AcademicLevel.FIRST_YEAR;
        };
    }

    /**
     * يستخرج عدد الساعات المعتمدة.
     * ملاحظة: في ريتاج، الرقم الثاني في الرمز غالباً ما يشير لعدد الساعات في بعض التخصصات،
     * لكن إذا لم ينجح ذلك، نعتمد 3 ساعات كقيمة افتراضية أغلبية.
     */
    public Integer extractCreditHours(String courseCode) {
        String digits = extractDigits(courseCode);

        // إذا كان طول الأرقام أقل من 2، لا يمكننا استنتاج الساعات بدقة
        if (digits == null || digits.length() < 2) {
            return 3;
        }

        char secondChar = digits.charAt(1);
        int value = Character.getNumericValue(secondChar);

        // نتحقق أن القيمة منطقية (بين 1 و 6 ساعات مثلاً)
        if (value >= 1 && value <= 6) {
            return value;
        }

        return 3; // القيمة الافتراضية للمساقات الجامعية
    }

    private String extractDigits(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        Matcher matcher = DIGIT_PATTERN.matcher(courseCode);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }
}