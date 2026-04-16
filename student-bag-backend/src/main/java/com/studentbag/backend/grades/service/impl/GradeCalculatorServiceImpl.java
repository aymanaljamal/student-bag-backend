package com.studentbag.backend.grades.service.impl;

import com.studentbag.backend.domain.enums.grades.*;
import com.studentbag.backend.grades.entity.GradeCalculation;
import com.studentbag.backend.grades.entity.GradeCourseItem;
import com.studentbag.backend.grades.service.GradeCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GradeCalculatorServiceImpl implements GradeCalculatorService {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal FOUR = new BigDecimal("4.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Override
    public void recalculate(GradeCalculation calculation) {
        if (calculation.getItems() == null) {
            calculation.setCalculatedGpa(ZERO);
            calculation.setCalculatedPercentage(ZERO);
            calculation.setTotalQualityPoints(ZERO);
            calculation.setTotalCredits(ZERO);
            calculation.setSubjectCount(0);
            return;
        }

        applyRepeatPolicy(calculation);

        BigDecimal totalCredits = ZERO;
        BigDecimal totalQualityPoints = ZERO;
        BigDecimal weightedPercentageSum = ZERO;
        int subjectCount = 0;

        for (GradeCourseItem item : calculation.getItems()) {
            normalizeItem(calculation, item);

            if (!shouldIncludeItem(calculation, item)) {
                continue;
            }

            BigDecimal credits = nz(item.getCreditHoursSnapshot());
            BigDecimal qualityPoints = nz(item.getQualityPoints());
            BigDecimal percentage = nz(item.getNormalizedPercentage());

            totalCredits = totalCredits.add(credits);
            totalQualityPoints = totalQualityPoints.add(qualityPoints);
            weightedPercentageSum = weightedPercentageSum.add(percentage.multiply(credits));
            subjectCount++;
        }

        calculation.setTotalCredits(scale(totalCredits, 2));
        calculation.setTotalQualityPoints(scale(totalQualityPoints, 4));
        calculation.setSubjectCount(subjectCount);

        if (totalCredits.compareTo(ZERO) > 0) {
            calculation.setCalculatedGpa(scale(totalQualityPoints.divide(totalCredits, 8, RoundingMode.HALF_UP), 4));
            calculation.setCalculatedPercentage(scale(weightedPercentageSum.divide(totalCredits, 8, RoundingMode.HALF_UP), 4));
        } else {
            calculation.setCalculatedGpa(ZERO);
            calculation.setCalculatedPercentage(ZERO);
        }
    }

    private void applyRepeatPolicy(GradeCalculation calculation) {
        List<GradeCourseItem> allItems = calculation.getItems();
        if (allItems == null || allItems.isEmpty()) return;

        for (GradeCourseItem item : allItems) {
            if (item.getIncludedInCalculation() == null) {
                item.setIncludedInCalculation(true);
            }
        }

        if (calculation.getRepeatPolicy() == null || calculation.getRepeatPolicy() == GradeRepeatPolicy.ALL_ATTEMPTS) {
            return;
        }

        var grouped = allItems.stream()
                .filter(Objects::nonNull)
                .filter(item -> item.getCourseCodeSnapshot() != null && !item.getCourseCodeSnapshot().isBlank())
                .collect(Collectors.groupingBy(item -> item.getCourseCodeSnapshot().trim().toUpperCase(Locale.ROOT)));

        grouped.values().forEach(items -> {
            if (items.size() <= 1) return;

            items.forEach(i -> i.setIncludedInCalculation(false));

            GradeCourseItem winner = switch (calculation.getRepeatPolicy()) {
                case HIGHEST_ATTEMPT -> items.stream()
                        .max(Comparator.comparing(i -> nz(i.getNormalizedPercentage())))
                        .orElse(items.get(0));
                case LAST_ATTEMPT -> items.stream()
                        .max(Comparator.comparing(i -> i.getId() == null ? 0L : i.getId()))
                        .orElse(items.get(items.size() - 1));
                case EXCLUDE_REPEATED -> null;
                default -> items.get(0);
            };

            if (winner != null) {
                winner.setIncludedInCalculation(true);
            }
        });
    }

    private boolean shouldIncludeItem(GradeCalculation calculation, GradeCourseItem item) {
        if (!Boolean.TRUE.equals(item.getIncludedInCalculation())) {
            return false;
        }

        if (item.getCourseStatus() == GradeCourseStatus.WITHDRAWN
                && !Boolean.TRUE.equals(calculation.getIncludeWithdrawnCourses())) {
            return false;
        }

        if (item.getCourseStatus() == GradeCourseStatus.PASS_FAIL
                && !Boolean.TRUE.equals(calculation.getIncludePassFailCourses())) {
            return false;
        }

        return item.getCourseStatus() != GradeCourseStatus.INCOMPLETE;
    }

    private void normalizeItem(GradeCalculation calculation, GradeCourseItem item) {
        BigDecimal credits = nz(item.getCreditHoursSnapshot());
        BigDecimal gpaScaleMax = nz(calculation.getGpaScaleMax()).compareTo(ZERO) > 0
                ? calculation.getGpaScaleMax()
                : FOUR;

        BigDecimal percentage;
        BigDecimal gradePoints;

        switch (calculation.getInputType()) {
            case MARK_OUT_OF_100 -> {
                BigDecimal enteredValue = nz(item.getEnteredValue());
                BigDecimal outOf = nz(item.getEnteredOutOf()).compareTo(ZERO) > 0
                        ? item.getEnteredOutOf()
                        : nz(calculation.getMarkScaleMax()).compareTo(ZERO) > 0
                        ? calculation.getMarkScaleMax()
                        : ONE_HUNDRED;

                percentage = enteredValue.multiply(ONE_HUNDRED).divide(outOf, 8, RoundingMode.HALF_UP);
                gradePoints = percentageToGpa(percentage, gpaScaleMax, calculation.getPercentageToGpaPolicy());
            }

            case GPA_OUT_OF_4, GPA_OUT_OF_5 -> {
                BigDecimal enteredValue = nz(item.getEnteredValue());
                BigDecimal outOf = nz(item.getEnteredOutOf()).compareTo(ZERO) > 0
                        ? item.getEnteredOutOf()
                        : (calculation.getInputType() == GradeInputType.GPA_OUT_OF_5
                        ? new BigDecimal("5.00")
                        : new BigDecimal("4.00"));

                gradePoints = enteredValue.multiply(gpaScaleMax).divide(outOf, 8, RoundingMode.HALF_UP);
                percentage = gradePoints.multiply(ONE_HUNDRED).divide(gpaScaleMax, 8, RoundingMode.HALF_UP);
            }

            case LETTER_GRADE -> {
                gradePoints = letterToGpa(item.getLetterGrade(), gpaScaleMax);
                percentage = gradePoints.multiply(ONE_HUNDRED).divide(gpaScaleMax, 8, RoundingMode.HALF_UP);
            }

            default -> {
                percentage = ZERO;
                gradePoints = ZERO;
            }
        }

        item.setNormalizedPercentage(scale(percentage, 4));
        item.setGradePoints(scale(gradePoints, 4));
        item.setQualityPoints(scale(gradePoints.multiply(credits), 4));
    }

    private BigDecimal percentageToGpa(
            BigDecimal percentage,
            BigDecimal gpaScaleMax,
            PercentageToGpaPolicy policy
    ) {
        if (policy == null) {
            policy = PercentageToGpaPolicy.PALESTINIAN_DEFAULT;
        }

        return switch (policy) {
            case LINEAR -> scale(percentage.multiply(gpaScaleMax).divide(ONE_HUNDRED, 8, RoundingMode.HALF_UP), 4);
            case AMERICAN_DEFAULT -> scale(mapAmericanPercentageToBaseFour(percentage).multiply(gpaScaleMax).divide(FOUR, 8, RoundingMode.HALF_UP), 4);
            case PALESTINIAN_DEFAULT -> scale(mapPalestinianPercentageToBaseFour(percentage).multiply(gpaScaleMax).divide(FOUR, 8, RoundingMode.HALF_UP), 4);
        };
    }

    private BigDecimal mapPalestinianPercentageToBaseFour(BigDecimal p) {
        if (p.compareTo(new BigDecimal("95")) >= 0) return new BigDecimal("4.00");
        if (p.compareTo(new BigDecimal("90")) >= 0) return new BigDecimal("3.75");
        if (p.compareTo(new BigDecimal("85")) >= 0) return new BigDecimal("3.50");
        if (p.compareTo(new BigDecimal("80")) >= 0) return new BigDecimal("3.00");
        if (p.compareTo(new BigDecimal("75")) >= 0) return new BigDecimal("2.50");
        if (p.compareTo(new BigDecimal("70")) >= 0) return new BigDecimal("2.00");
        if (p.compareTo(new BigDecimal("65")) >= 0) return new BigDecimal("1.50");
        if (p.compareTo(new BigDecimal("60")) >= 0) return new BigDecimal("1.00");
        return BigDecimal.ZERO;
    }

    private BigDecimal mapAmericanPercentageToBaseFour(BigDecimal p) {
        if (p.compareTo(new BigDecimal("93")) >= 0) return new BigDecimal("4.00");
        if (p.compareTo(new BigDecimal("90")) >= 0) return new BigDecimal("3.70");
        if (p.compareTo(new BigDecimal("87")) >= 0) return new BigDecimal("3.30");
        if (p.compareTo(new BigDecimal("83")) >= 0) return new BigDecimal("3.00");
        if (p.compareTo(new BigDecimal("80")) >= 0) return new BigDecimal("2.70");
        if (p.compareTo(new BigDecimal("77")) >= 0) return new BigDecimal("2.30");
        if (p.compareTo(new BigDecimal("73")) >= 0) return new BigDecimal("2.00");
        if (p.compareTo(new BigDecimal("70")) >= 0) return new BigDecimal("1.70");
        if (p.compareTo(new BigDecimal("67")) >= 0) return new BigDecimal("1.30");
        if (p.compareTo(new BigDecimal("63")) >= 0) return new BigDecimal("1.00");
        if (p.compareTo(new BigDecimal("60")) >= 0) return new BigDecimal("0.70");
        return BigDecimal.ZERO;
    }

    private BigDecimal letterToGpa(String letter, BigDecimal gpaScaleMax) {
        if (letter == null || letter.isBlank()) return ZERO;

        String v = letter.trim().toUpperCase(Locale.ROOT);
        BigDecimal baseFour = switch (v) {
            case "A", "A+" -> new BigDecimal("4.00");
            case "A-" -> new BigDecimal("3.70");
            case "B+" -> new BigDecimal("3.30");
            case "B" -> new BigDecimal("3.00");
            case "B-" -> new BigDecimal("2.70");
            case "C+" -> new BigDecimal("2.30");
            case "C" -> new BigDecimal("2.00");
            case "C-" -> new BigDecimal("1.70");
            case "D+" -> new BigDecimal("1.30");
            case "D" -> new BigDecimal("1.00");
            case "D-" -> new BigDecimal("0.70");
            default -> ZERO;
        };

        return scale(baseFour.multiply(gpaScaleMax).divide(FOUR, 8, RoundingMode.HALF_UP), 4);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? ZERO : value;
    }

    private BigDecimal scale(BigDecimal value, int scale) {
        return value == null ? ZERO : value.setScale(scale, RoundingMode.HALF_UP);
    }
}