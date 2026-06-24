package com.studentbag.backend.courses.sync.helper;

import com.studentbag.backend.domain.enums.courses.AcademicLevel;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CourseCodeMetadataHelper {

    private static final Pattern DIGIT_PATTERN = Pattern.compile("(\\d+)");

    public AcademicLevel extractAcademicLevel(String courseCode) {
        String digits = extractDigits(courseCode);

        if (digits == null || digits.isEmpty()) {
            return AcademicLevel.FIRST_YEAR;
        }

        char firstChar = digits.charAt(0);

        return switch (firstChar) {
            case '1' -> AcademicLevel.FIRST_YEAR;
            case '2' -> AcademicLevel.SECOND_YEAR;
            case '3' -> AcademicLevel.THIRD_YEAR;
            case '4' -> AcademicLevel.FOURTH_YEAR;
            case '5' -> AcademicLevel.FIFTH_YEAR;
            case '6' -> AcademicLevel.SIXTH_YEAR;
            case '7' -> AcademicLevel.SEVENTH_YEAR;
            case '8', '9' -> AcademicLevel.EIGHTH_YEAR;
            default -> AcademicLevel.FIRST_YEAR;
        };
    }

    public Integer extractCreditHours(String courseCode) {
        String digits = extractDigits(courseCode);

        if (digits == null || digits.length() < 2) {
            return 3;
        }

        char secondChar = digits.charAt(1);
        int value = Character.getNumericValue(secondChar);

        if (value >= 1 && value <= 6) {
            return value;
        }

        return 3;
    }

    private String extractDigits(String courseCode) {
        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }

        Matcher matcher = DIGIT_PATTERN.matcher(courseCode);

        return matcher.find() ? matcher.group(1) : null;
    }
}
