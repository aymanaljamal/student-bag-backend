package com.studentbag.backend.grades.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.domain.enums.grades.GradeAdviceLevel;
import com.studentbag.backend.domain.enums.grades.GradeCalculationSource;
import com.studentbag.backend.domain.enums.grades.GradeCourseStatus;
import com.studentbag.backend.domain.enums.grades.WhatIfScope;
import com.studentbag.backend.domain.enums.grades.WhatIfTargetType;
import com.studentbag.backend.domain.enums.schedule.ScheduleSourceType;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.grades.dto.request.CreateGradeCalculationRequest;
import com.studentbag.backend.grades.dto.request.GradeCourseItemRequest;
import com.studentbag.backend.grades.dto.request.GradeWhatIfRequest;
import com.studentbag.backend.grades.dto.request.UpdateGradeCalculationRequest;
import com.studentbag.backend.grades.dto.response.CourseImpactDTO;
import com.studentbag.backend.grades.dto.response.GradeAdviceDTO;
import com.studentbag.backend.grades.dto.response.GradeCalculationResponse;
import com.studentbag.backend.grades.dto.response.GradeInsightsResponse;
import com.studentbag.backend.grades.dto.response.GradeWhatIfResponse;
import com.studentbag.backend.grades.dto.response.WhatIfRequiredCourseDTO;
import com.studentbag.backend.grades.entity.GradeCalculation;
import com.studentbag.backend.grades.entity.GradeCourseItem;
import com.studentbag.backend.grades.mapper.GradeCalculationMapper;
import com.studentbag.backend.grades.repository.GradeCalculationRepository;
import com.studentbag.backend.grades.repository.GradeCourseItemRepository;
import com.studentbag.backend.grades.service.GradeCalculationService;
import com.studentbag.backend.grades.service.GradeCalculatorService;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeCalculationServiceImpl implements GradeCalculationService {

    private final GradeCalculationRepository calculationRepository;
    private final GradeCourseItemRepository itemRepository;
    private final GradeCalculationMapper mapper;
    private final GradeCalculatorService calculatorService;

    private final StudentRepository studentRepository;
    private final StudentScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;

    @Override
    @Transactional
    public GradeCalculationResponse createManual(Long studentId, CreateGradeCalculationRequest request) {
        Student student = findStudent(studentId);

        GradeCalculation calculation = buildBaseCalculation(student, request, null);
        applyRequestedItems(calculation, request.getItems(), true);

        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse createFromActiveSchedule(Long studentId, CreateGradeCalculationRequest request) {
        Student student = findStudent(studentId);

        StudentSchedule activeSchedule = scheduleRepository
                .findFirstByStudentIdAndStatusOrderByIdDesc(studentId, ScheduleStatus.ACTIVE)
                .orElseThrow(() -> new EntityNotFoundException("No active schedule found for this student"));

        GradeCalculation calculation = buildBaseCalculation(student, request, activeSchedule);

        importUniqueCoursesFromActiveSchedule(calculation, activeSchedule);

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            applyRequestedItems(calculation, request.getItems(), true);
            calculation.setSourceType(GradeCalculationSource.MIXED);
        }

        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GradeCalculationResponse> getStudentCalculations(Long studentId) {
        return calculationRepository.findAllByStudentIdOrderByUpdatedAtDesc(studentId)
                .stream()
                .map(this::toEnrichedResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GradeCalculationResponse getCalculation(Long calculationId, Long studentId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        calculatorService.recalculate(calculation);
        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse updateCalculation(Long calculationId, Long studentId, UpdateGradeCalculationRequest request) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);

        calculation.setTitle(request.getTitle() != null ? request.getTitle() : calculation.getTitle());
        calculation.setDescription(request.getDescription() != null ? request.getDescription() : calculation.getDescription());
        calculation.setInputType(request.getInputType() != null ? request.getInputType() : calculation.getInputType());
        calculation.setCalculationType(request.getCalculationType() != null ? request.getCalculationType() : calculation.getCalculationType());
        calculation.setRepeatPolicy(request.getRepeatPolicy() != null ? request.getRepeatPolicy() : calculation.getRepeatPolicy());
        calculation.setPercentageToGpaPolicy(
                request.getPercentageToGpaPolicy() != null
                        ? request.getPercentageToGpaPolicy()
                        : calculation.getPercentageToGpaPolicy()
        );
        calculation.setGpaScaleMax(request.getGpaScaleMax() != null ? request.getGpaScaleMax() : calculation.getGpaScaleMax());
        calculation.setMarkScaleMax(request.getMarkScaleMax() != null ? request.getMarkScaleMax() : calculation.getMarkScaleMax());
        calculation.setAutoGenerateSubjectNames(
                request.getAutoGenerateSubjectNames() != null
                        ? request.getAutoGenerateSubjectNames()
                        : calculation.getAutoGenerateSubjectNames()
        );
        calculation.setIncludePassFailCourses(
                request.getIncludePassFailCourses() != null
                        ? request.getIncludePassFailCourses()
                        : calculation.getIncludePassFailCourses()
        );
        calculation.setIncludeWithdrawnCourses(
                request.getIncludeWithdrawnCourses() != null
                        ? request.getIncludeWithdrawnCourses()
                        : calculation.getIncludeWithdrawnCourses()
        );
        calculation.setIsLocked(request.getIsLocked() != null ? request.getIsLocked() : calculation.getIsLocked());

        if (request.getItems() != null) {
            calculation.getItems().clear();
            applyRequestedItems(calculation, request.getItems(), true);
        }

        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse addItem(Long calculationId, Long studentId, GradeCourseItemRequest request) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);

        if (Boolean.TRUE.equals(calculation.getIsLocked())) {
            throw new IllegalStateException("This grade calculation is locked");
        }

        GradeCourseItem item = buildItemFromRequest(calculation, request, true);
        calculation.addItem(item);

        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse deleteItem(Long calculationId, Long studentId, Long itemId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);

        GradeCourseItem item = itemRepository.findByIdAndCalculationId(itemId, calculationId)
                .orElseThrow(() -> new EntityNotFoundException("Grade item not found"));

        calculation.removeItem(item);

        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse recalculate(Long calculationId, Long studentId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);

        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public void deleteCalculation(Long calculationId, Long studentId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        calculationRepository.delete(calculation);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeInsightsResponse getInsights(Long calculationId, Long studentId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        calculatorService.recalculate(calculation);
        return buildInsights(calculation);
    }

    @Override
    @Transactional(readOnly = true)
    public GradeWhatIfResponse analyzeWhatIf(Long calculationId, Long studentId, GradeWhatIfRequest request) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        calculatorService.recalculate(calculation);

        WhatIfScope scope = request.getScope() != null ? request.getScope() : WhatIfScope.SEMESTER;

        return switch (scope) {
            case SEMESTER -> analyzeSemesterWhatIf(calculation, request);
            case CUMULATIVE -> analyzeCumulativeWhatIf(calculation, request);
        };
    }

    private GradeCalculation buildBaseCalculation(
            Student student,
            CreateGradeCalculationRequest request,
            StudentSchedule sourceSchedule
    ) {
        return GradeCalculation.builder()
                .student(student)
                .sourceSchedule(sourceSchedule)
                .title(request.getTitle() != null && !request.getTitle().isBlank()
                        ? request.getTitle()
                        : defaultTitle(request.getSourceType()))
                .description(request.getDescription())
                .sourceType(request.getSourceType())
                .inputType(request.getInputType())
                .calculationType(request.getCalculationType())
                .repeatPolicy(request.getRepeatPolicy())
                .percentageToGpaPolicy(request.getPercentageToGpaPolicy())
                .gpaScaleMax(request.getGpaScaleMax())
                .markScaleMax(request.getMarkScaleMax())
                .autoGenerateSubjectNames(Boolean.TRUE.equals(request.getAutoGenerateSubjectNames()))
                .includePassFailCourses(Boolean.TRUE.equals(request.getIncludePassFailCourses()))
                .includeWithdrawnCourses(Boolean.TRUE.equals(request.getIncludeWithdrawnCourses()))
                .isLocked(false)
                .build();
    }

    private void importUniqueCoursesFromActiveSchedule(GradeCalculation calculation, StudentSchedule schedule) {
        if (schedule.getEntries() == null || schedule.getEntries().isEmpty()) {
            return;
        }

        Map<Long, CourseSection> uniqueSectionsByCourseId = schedule.getEntries().stream()
                .filter(Objects::nonNull)
                .filter(entry -> entry.getSourceType() == ScheduleSourceType.COURSE)
                .map(ScheduleEntry::getCourseSection)
                .filter(Objects::nonNull)
                .filter(section -> section.getCourse() != null && section.getCourse().getId() != null)
                .collect(Collectors.toMap(
                        section -> section.getCourse().getId(),
                        section -> section,
                        (first, second) -> first,
                        LinkedHashMap::new
                ));

        int order = calculation.getItems().size() + 1;

        for (CourseSection section : uniqueSectionsByCourseId.values()) {
            Course course = section.getCourse();
            if (course == null) continue;

            GradeCourseItem item = GradeCourseItem.builder()
                    .calculation(calculation)
                    .course(course)
                    .courseSection(section)
                    .courseCodeSnapshot(course.getCode())
                    .courseNameSnapshot(resolveCourseName(course, order, calculation.getAutoGenerateSubjectNames()))
                    .creditHoursSnapshot(toBigDecimal(course.getCreditHours()))
                    .orderIndex(order++)
                    .courseStatus(GradeCourseStatus.REGISTERED)
                    .includedInCalculation(true)
                    .isManualEntry(false)
                    .isFromSchedule(true)
                    .isRepeatedCourse(false)
                    .build();

            calculation.addItem(item);
        }
    }

    private void applyRequestedItems(
            GradeCalculation calculation,
            List<GradeCourseItemRequest> requests,
            boolean manualEntries
    ) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        int startOrder = calculation.getItems().size() + 1;

        for (GradeCourseItemRequest request : requests) {
            if (request.getOrderIndex() == null) {
                request.setOrderIndex(startOrder++);
            }

            GradeCourseItem item = buildItemFromRequest(calculation, request, manualEntries);
            calculation.addItem(item);
        }
    }

    private GradeCourseItem buildItemFromRequest(
            GradeCalculation calculation,
            GradeCourseItemRequest request,
            boolean manualEntries
    ) {
        CourseSection section = null;
        Course course = null;

        if (request.getCourseSectionId() != null) {
            section = courseSectionRepository.findById(request.getCourseSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("Course section not found"));
            course = section.getCourse();
        } else if (request.getCourseId() != null) {
            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        }

        String courseCode = request.getCourseCode();
        String courseName = request.getCourseName();
        BigDecimal creditHours = request.getCreditHours();

        if (course != null) {
            if (courseCode == null || courseCode.isBlank()) {
                courseCode = course.getCode();
            }

            if (courseName == null || courseName.isBlank()) {
                courseName = resolveCourseName(
                        course,
                        request.getOrderIndex() != null ? request.getOrderIndex() : calculation.getItems().size() + 1,
                        calculation.getAutoGenerateSubjectNames()
                );
            }

            if (creditHours == null) {
                creditHours = toBigDecimal(course.getCreditHours());
            }
        }

        if ((courseName == null || courseName.isBlank()) && Boolean.TRUE.equals(calculation.getAutoGenerateSubjectNames())) {
            int idx = request.getOrderIndex() != null ? request.getOrderIndex() : calculation.getItems().size() + 1;
            courseName = "Course " + idx;
        }

        if (courseName == null || courseName.isBlank()) {
            courseName = "Unnamed Course";
        }

        if (creditHours == null) {
            creditHours = BigDecimal.ZERO;
        }

        return GradeCourseItem.builder()
                .calculation(calculation)
                .course(course)
                .courseSection(section)
                .courseCodeSnapshot(courseCode)
                .courseNameSnapshot(courseName)
                .creditHoursSnapshot(creditHours)
                .enteredValue(request.getEnteredValue())
                .enteredOutOf(request.getEnteredOutOf())
                .letterGrade(request.getLetterGrade())
                .orderIndex(request.getOrderIndex())
                .courseStatus(request.getCourseStatus() != null ? request.getCourseStatus() : GradeCourseStatus.COMPLETED)
                .includedInCalculation(request.getIncludedInCalculation() != null ? request.getIncludedInCalculation() : true)
                .isManualEntry(manualEntries)
                .isFromSchedule(!manualEntries)
                .isRepeatedCourse(request.getIsRepeatedCourse() != null ? request.getIsRepeatedCourse() : false)
                .notes(request.getNotes())
                .build();
    }

    private GradeCalculationResponse toEnrichedResponse(GradeCalculation calculation) {
        GradeCalculationResponse response = mapper.toResponse(calculation);
        response.setInsights(buildInsights(calculation));
        return response;
    }

    private GradeInsightsResponse buildInsights(GradeCalculation calculation) {
        List<GradeCourseItem> consideredItems = calculation.getItems() == null
                ? List.of()
                : calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getIncludedInCalculation()))
                .filter(item -> item.getNormalizedPercentage() != null || item.getGradePoints() != null)
                .toList();

        List<GradeAdviceDTO> advice = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        BigDecimal percentage = nz(calculation.getCalculatedPercentage());
        BigDecimal gpa = nz(calculation.getCalculatedGpa());
        BigDecimal gpaScale = nz(calculation.getGpaScaleMax()).compareTo(BigDecimal.ZERO) > 0
                ? calculation.getGpaScaleMax()
                : new BigDecimal("4.00");

        String overallLevel;
        String summary;

        if (percentage.compareTo(new BigDecimal("90")) >= 0) {
            overallLevel = "EXCELLENT";
            summary = "أداءك ممتاز جدًا، استمر بنفس المستوى.";
        } else if (percentage.compareTo(new BigDecimal("80")) >= 0) {
            overallLevel = "VERY_GOOD";
            summary = "أداءك جيد جدًا، وعندك فرصة واضحة ترفعه أكثر.";
        } else if (percentage.compareTo(new BigDecimal("70")) >= 0) {
            overallLevel = "GOOD";
            summary = "أداءك جيد، لكن يوجد مواد يمكن تحسينها بشكل مؤثر.";
        } else if (percentage.compareTo(new BigDecimal("60")) >= 0) {
            overallLevel = "NEEDS_IMPROVEMENT";
            summary = "معدلك مقبول، لكن يحتاج تركيز أكبر في المواد الأضعف.";
        } else {
            overallLevel = "AT_RISK";
            summary = "معدلك منخفض حاليًا، والأولوية الآن لرفع المواد الأساسية والثقيلة.";
        }

        List<CourseImpactDTO> weakestCourses = consideredItems.stream()
                .filter(item -> item.getNormalizedPercentage() != null)
                .sorted(Comparator.comparing(item -> nz(item.getNormalizedPercentage())))
                .limit(3)
                .map(this::toImpactDto)
                .toList();

        List<CourseImpactDTO> highestImpactCourses = consideredItems.stream()
                .sorted((a, b) -> impactScore(b).compareTo(impactScore(a)))
                .limit(3)
                .map(this::toImpactDto)
                .toList();

        if (!weakestCourses.isEmpty()) {
            CourseImpactDTO weakest = weakestCourses.get(0);
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.WARNING)
                    .title("أولوية التحسين")
                    .message("ركز أولًا على " + weakest.getCourseName() + " لأنها من أضعف المواد عندك حاليًا.")
                    .build());
        }

        if (!highestImpactCourses.isEmpty()) {
            CourseImpactDTO highestImpact = highestImpactCourses.get(0);
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.INFO)
                    .title("أعلى تأثير على المعدل")
                    .message("رفع مادة " + highestImpact.getCourseName()
                            + " سيفرق أكثر، لأنها تحمل ساعات أعلى أو علامتها الحالية منخفضة.")
                    .build());
        }

        long missingGradesCount = calculation.getItems() == null ? 0 : calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(this::isRemainingItem)
                .count();

        if (missingGradesCount > 0) {
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.INFO)
                    .title("فرصة التحسين ما زالت موجودة")
                    .message("عندك " + missingGradesCount + " مادة/مواد بدون علامة نهائية حتى الآن، وهذا ممتاز لتحليل ماذا لو.")
                    .build());
        }

        if (gpa.compareTo(gpaScale.multiply(new BigDecimal("0.50"))) < 0) {
            warnings.add("معدلك على مقياس GPA ما زال منخفضًا نسبيًا مقارنة بالحد المتوسط.");
        }

        if (weakestCourses.size() >= 2) {
            warnings.add("عندك أكثر من مادة منخفضة، فالأفضل تبدأ بالمواد ذات الساعات الأعلى أولًا.");
        }

        if (percentage.compareTo(new BigDecimal("85")) >= 0) {
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.SUCCESS)
                    .title("استمر")
                    .message("أداؤك قوي، والمحافظة على هذا المستوى أهم من التشتت بين المواد.")
                    .build());
        }

        return GradeInsightsResponse.builder()
                .summary(summary)
                .overallLevel(overallLevel)
                .advice(advice)
                .weakestCourses(weakestCourses)
                .highestImpactCourses(highestImpactCourses)
                .warnings(warnings)
                .build();
    }

    private CourseImpactDTO toImpactDto(GradeCourseItem item) {
        return CourseImpactDTO.builder()
                .itemId(item.getId())
                .courseName(item.getCourseNameSnapshot())
                .creditHours(nz(item.getCreditHoursSnapshot()))
                .currentPercentage(item.getNormalizedPercentage())
                .currentGpa(item.getGradePoints())
                .impactScore(impactScore(item))
                .build();
    }

    private BigDecimal impactScore(GradeCourseItem item) {
        BigDecimal credits = nz(item.getCreditHoursSnapshot());
        BigDecimal percentage = nz(item.getNormalizedPercentage());
        BigDecimal weakness = new BigDecimal("100").subtract(percentage);

        if (weakness.compareTo(BigDecimal.ZERO) < 0) {
            weakness = BigDecimal.ZERO;
        }

        return credits.multiply(weakness).setScale(4, RoundingMode.HALF_UP);
    }

    private GradeWhatIfResponse analyzeSemesterWhatIf(GradeCalculation calculation, GradeWhatIfRequest request) {
        WhatIfTargetType targetType = request.getTargetType() != null ? request.getTargetType() : WhatIfTargetType.PERCENTAGE;

        BigDecimal totalCredits = sumCredits(calculation.getItems());
        BigDecimal gradedCredits = sumCredits(
                calculation.getItems().stream().filter(item -> !isRemainingItem(item)).toList()
        );
        BigDecimal remainingCredits = sumCredits(
                calculation.getItems().stream().filter(this::isRemainingItem).toList()
        );

        BigDecimal targetValue = nz(request.getTargetValue());
        BigDecimal currentValue = targetType == WhatIfTargetType.GPA
                ? nz(calculation.getCalculatedGpa())
                : nz(calculation.getCalculatedPercentage());

        if (remainingCredits.compareTo(BigDecimal.ZERO) <= 0) {
            boolean possible = currentValue.compareTo(targetValue) >= 0;
            return GradeWhatIfResponse.builder()
                    .possible(possible)
                    .scope("SEMESTER")
                    .targetType(targetType.name())
                    .targetValue(targetValue)
                    .currentValue(currentValue)
                    .gradedCredits(gradedCredits)
                    .remainingCredits(remainingCredits)
                    .totalCredits(totalCredits)
                    .maxPossibleValue(currentValue)
                    .message(possible
                            ? "الهدف محقق أصلًا لأنه لا توجد مواد متبقية."
                            : "لا توجد مواد متبقية، لذلك لا يمكن رفع النتيجة أكثر ضمن هذا الفصل.")
                    .build();
        }

        BigDecimal currentWeighted = targetType == WhatIfTargetType.GPA
                ? weightedCurrentGpaPoints(calculation)
                : weightedCurrentPercentagePoints(calculation);

        BigDecimal scaleMax = targetType == WhatIfTargetType.GPA
                ? nz(calculation.getGpaScaleMax())
                : new BigDecimal("100");

        BigDecimal requiredAverageOnRemaining = targetValue.multiply(totalCredits)
                .subtract(currentWeighted)
                .divide(remainingCredits, 8, RoundingMode.HALF_UP);

        BigDecimal maxPossible = currentWeighted.add(remainingCredits.multiply(scaleMax))
                .divide(totalCredits, 8, RoundingMode.HALF_UP);

        boolean possible = requiredAverageOnRemaining.compareTo(BigDecimal.ZERO) >= 0
                && requiredAverageOnRemaining.compareTo(scaleMax) <= 0;

        List<WhatIfRequiredCourseDTO> perCourse = calculation.getItems().stream()
                .filter(this::isRemainingItem)
                .map(item -> WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(requiredAverageOnRemaining.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        List<String> notes = new ArrayList<>();
        if (possible) {
            notes.add("الهدف ممكن إذا حافظت تقريبًا على هذا المتوسط في المواد المتبقية.");
        } else {
            notes.add("الهدف غير ممكن ضمن هذا الفصل فقط حسب المواد المتبقية.");
            notes.add("أعلى نتيجة ممكنة تقريبًا هي " + maxPossible.setScale(2, RoundingMode.HALF_UP));
        }

        return GradeWhatIfResponse.builder()
                .possible(possible)
                .scope("SEMESTER")
                .targetType(targetType.name())
                .targetValue(targetValue)
                .currentValue(currentValue.setScale(2, RoundingMode.HALF_UP))
                .requiredAverageOnRemaining(requiredAverageOnRemaining.setScale(2, RoundingMode.HALF_UP))
                .maxPossibleValue(maxPossible.setScale(2, RoundingMode.HALF_UP))
                .gradedCredits(gradedCredits)
                .remainingCredits(remainingCredits)
                .totalCredits(totalCredits)
                .message(possible
                        ? "يمكنك الوصول إلى الهدف إذا حققت المتوسط المطلوب في المواد المتبقية."
                        : "هذا الهدف غير ممكن في هذا الفصل فقط.")
                .requiredPerCourse(perCourse)
                .notes(notes)
                .build();
    }

    private GradeWhatIfResponse analyzeCumulativeWhatIf(GradeCalculation calculation, GradeWhatIfRequest request) {
        WhatIfTargetType targetType = request.getTargetType() != null ? request.getTargetType() : WhatIfTargetType.PERCENTAGE;

        BigDecimal semesterCredits = sumCredits(calculation.getItems());
        BigDecimal gradedCredits = sumCredits(
                calculation.getItems().stream().filter(item -> !isRemainingItem(item)).toList()
        );
        BigDecimal remainingCredits = sumCredits(
                calculation.getItems().stream().filter(this::isRemainingItem).toList()
        );

        BigDecimal targetValue = nz(request.getTargetValue());
        BigDecimal currentCumulative = nz(request.getCurrentCumulativeValue());
        BigDecimal completedCredits = nz(request.getCompletedCredits());

        if (semesterCredits.compareTo(BigDecimal.ZERO) <= 0) {
            return GradeWhatIfResponse.builder()
                    .possible(false)
                    .scope("CUMULATIVE")
                    .targetType(targetType.name())
                    .targetValue(targetValue)
                    .currentValue(currentCumulative)
                    .completedCredits(completedCredits)
                    .gradedCredits(gradedCredits)
                    .remainingCredits(remainingCredits)
                    .totalCredits(semesterCredits)
                    .message("لا توجد ساعات فصلية كافية لإجراء تحليل تراكمي.")
                    .build();
        }

        BigDecimal scaleMax = targetType == WhatIfTargetType.GPA
                ? nz(calculation.getGpaScaleMax())
                : new BigDecimal("100");

        BigDecimal requiredSemesterValue = targetValue.multiply(completedCredits.add(semesterCredits))
                .subtract(currentCumulative.multiply(completedCredits))
                .divide(semesterCredits, 8, RoundingMode.HALF_UP);

        BigDecimal currentSemesterWeighted = targetType == WhatIfTargetType.GPA
                ? weightedCurrentGpaPoints(calculation)
                : weightedCurrentPercentagePoints(calculation);

        BigDecimal requiredAverageOnRemaining;
        if (remainingCredits.compareTo(BigDecimal.ZERO) > 0) {
            requiredAverageOnRemaining = requiredSemesterValue.multiply(semesterCredits)
                    .subtract(currentSemesterWeighted)
                    .divide(remainingCredits, 8, RoundingMode.HALF_UP);
        } else {
            requiredAverageOnRemaining = BigDecimal.ZERO;
        }

        BigDecimal maxPossibleSemester = currentSemesterWeighted.add(remainingCredits.multiply(scaleMax))
                .divide(semesterCredits, 8, RoundingMode.HALF_UP);

        BigDecimal maxPossibleCumulative = currentCumulative.multiply(completedCredits)
                .add(maxPossibleSemester.multiply(semesterCredits))
                .divide(completedCredits.add(semesterCredits), 8, RoundingMode.HALF_UP);

        boolean possible = requiredSemesterValue.compareTo(BigDecimal.ZERO) >= 0
                && requiredSemesterValue.compareTo(scaleMax) <= 0
                && (remainingCredits.compareTo(BigDecimal.ZERO) <= 0
                || requiredAverageOnRemaining.compareTo(scaleMax) <= 0);

        List<WhatIfRequiredCourseDTO> perCourse = calculation.getItems().stream()
                .filter(this::isRemainingItem)
                .map(item -> WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(requiredAverageOnRemaining.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        List<String> notes = new ArrayList<>();
        notes.add("هذا التحليل يفترض أن الساعات المكتملة السابقة ثابتة، والتحسين المطلوب كله من هذا الفصل.");

        if (!possible) {
            notes.add("أعلى تراكمي ممكن تقريبًا بعد هذا الفصل هو "
                    + maxPossibleCumulative.setScale(2, RoundingMode.HALF_UP));
        }

        return GradeWhatIfResponse.builder()
                .possible(possible)
                .scope("CUMULATIVE")
                .targetType(targetType.name())
                .targetValue(targetValue)
                .currentValue(currentCumulative.setScale(2, RoundingMode.HALF_UP))
                .requiredSemesterValue(requiredSemesterValue.setScale(2, RoundingMode.HALF_UP))
                .requiredAverageOnRemaining(requiredAverageOnRemaining.setScale(2, RoundingMode.HALF_UP))
                .maxPossibleValue(maxPossibleCumulative.setScale(2, RoundingMode.HALF_UP))
                .completedCredits(completedCredits)
                .gradedCredits(gradedCredits)
                .remainingCredits(remainingCredits)
                .totalCredits(semesterCredits)
                .message(possible
                        ? "الوصول إلى الهدف التراكمي ممكن إذا حققت المعدل المطلوب هذا الفصل."
                        : "الهدف التراكمي غير ممكن من خلال هذا الفصل فقط.")
                .requiredPerCourse(perCourse)
                .notes(notes)
                .build();
    }

    private boolean isRemainingItem(GradeCourseItem item) {
        if (item == null) return false;

        boolean noNumericValue = item.getEnteredValue() == null;
        boolean noLetter = item.getLetterGrade() == null || item.getLetterGrade().isBlank();
        boolean noNormalizedResult = item.getNormalizedPercentage() == null && item.getGradePoints() == null;

        return Boolean.TRUE.equals(item.getIncludedInCalculation())
                && ((noNumericValue && noLetter) || noNormalizedResult);
    }

    private BigDecimal weightedCurrentPercentagePoints(GradeCalculation calculation) {
        return calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(item -> !isRemainingItem(item))
                .map(item -> nz(item.getNormalizedPercentage()).multiply(nz(item.getCreditHoursSnapshot())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal weightedCurrentGpaPoints(GradeCalculation calculation) {
        return calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(item -> !isRemainingItem(item))
                .map(item -> nz(item.getGradePoints()).multiply(nz(item.getCreditHoursSnapshot())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumCredits(List<GradeCourseItem> items) {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getIncludedInCalculation()))
                .map(item -> nz(item.getCreditHoursSnapshot()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private GradeCalculation findCalculation(Long calculationId, Long studentId) {
        return calculationRepository.findByIdAndStudentId(calculationId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("Grade calculation not found"));
    }

    private Student findStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
    }

    private String defaultTitle(GradeCalculationSource sourceType) {
        GradeCalculationSource safeSource = sourceType != null ? sourceType : GradeCalculationSource.MANUAL;

        return switch (safeSource) {
            case ACTIVE_SCHEDULE -> "Active Schedule GPA";
            case MIXED -> "Mixed GPA Calculation";
            case MANUAL -> "Manual GPA Calculation";
        };
    }

    private String resolveCourseName(Course course, int order, Boolean autoGenerate) {
        if (course != null) {
            if (course.getNameEnglish() != null && !course.getNameEnglish().isBlank()) {
                return course.getNameEnglish();
            }
            if (course.getNameArabic() != null && !course.getNameArabic().isBlank()) {
                return course.getNameArabic();
            }
            if (course.getCode() != null && !course.getCode().isBlank()) {
                return "Course " + course.getCode().trim().toUpperCase(Locale.ROOT);
            }
        }

        if (Boolean.TRUE.equals(autoGenerate)) {
            return "Course " + order;
        }

        return "Unnamed Course";
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value == null ? BigDecimal.ZERO : BigDecimal.valueOf(value);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}