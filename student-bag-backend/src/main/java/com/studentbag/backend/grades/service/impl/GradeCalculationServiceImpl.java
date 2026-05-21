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

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal DEFAULT_GPA_SCALE = new BigDecimal("4.00");

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
        validateCreateRequest(request);

        Student student = findStudent(studentId);

        GradeCalculation calculation = buildBaseCalculation(student, request, null);
        applyRequestedItems(calculation, request.getItems(), true);

        normalizeOrderIndexes(calculation);
        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse createFromActiveSchedule(Long studentId, CreateGradeCalculationRequest request) {
        validateCreateRequest(request);

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

        normalizeOrderIndexes(calculation);
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
    public GradeCalculationResponse updateCalculation(
            Long calculationId,
            Long studentId,
            UpdateGradeCalculationRequest request
    ) {
        validateUpdateRequest(request);

        GradeCalculation calculation = findCalculation(calculationId, studentId);
        ensureCalculationIsEditable(calculation);

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

        normalizeOrderIndexes(calculation);
        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse addItem(Long calculationId, Long studentId, GradeCourseItemRequest request) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        ensureCalculationIsEditable(calculation);

        GradeCourseItem item = buildItemFromRequest(calculation, request, true);

        if (isDuplicateCourseItem(calculation, item)) {
            throw new IllegalStateException("This course already exists in this grade calculation");
        }

        calculation.addItem(item);

        normalizeOrderIndexes(calculation);
        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse deleteItem(Long calculationId, Long studentId, Long itemId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        ensureCalculationIsEditable(calculation);

        GradeCourseItem item = itemRepository.findByIdAndCalculationId(itemId, calculationId)
                .orElseThrow(() -> new EntityNotFoundException("Grade item not found"));

        calculation.removeItem(item);

        normalizeOrderIndexes(calculation);
        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public GradeCalculationResponse recalculate(Long calculationId, Long studentId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);

        /*
         * ملاحظة:
         * لا نمنع recalculate حتى لو الحساب locked.
         * لأن إعادة الحساب لا تعتبر تعديل يدوي من الطالب،
         * بل تحديث للنتائج بناءً على البيانات الموجودة.
         */
        normalizeOrderIndexes(calculation);
        calculatorService.recalculate(calculation);
        calculationRepository.save(calculation);

        return toEnrichedResponse(calculation);
    }

    @Override
    @Transactional
    public void deleteCalculation(Long calculationId, Long studentId) {
        GradeCalculation calculation = findCalculation(calculationId, studentId);
        ensureCalculationIsEditable(calculation);

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
        validateWhatIfRequest(request);

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
            if (course == null) {
                continue;
            }

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

            if (!isDuplicateCourseItem(calculation, item)) {
                calculation.addItem(item);
            }
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

            if (isDuplicateCourseItem(calculation, item)) {
                continue;
            }

            calculation.addItem(item);
        }
    }

    private GradeCourseItem buildItemFromRequest(
            GradeCalculation calculation,
            GradeCourseItemRequest request,
            boolean manualEntries
    ) {
        validateCourseItemRequest(request);

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

        if ((courseName == null || courseName.isBlank())
                && Boolean.TRUE.equals(calculation.getAutoGenerateSubjectNames())) {
            int idx = request.getOrderIndex() != null ? request.getOrderIndex() : calculation.getItems().size() + 1;
            courseName = "Course " + idx;
        }

        if (courseName == null || courseName.isBlank()) {
            courseName = "Unnamed Course";
        }

        if (creditHours == null) {
            creditHours = ZERO;
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
        BigDecimal gpaScale = nz(calculation.getGpaScaleMax()).compareTo(ZERO) > 0
                ? calculation.getGpaScaleMax()
                : DEFAULT_GPA_SCALE;

        String overallLevel;
        String summary;

        if (percentage.compareTo(new BigDecimal("90")) >= 0) {
            overallLevel = "EXCELLENT";
            summary = bi(
                    "أداءك ممتاز جدًا. الهدف الآن هو المحافظة على الاستقرار، خصوصًا في المواد ذات الساعات العالية لأنها يمكن أن تؤثر على المعدل بسرعة إذا انخفضت علامتها.",
                    "Your performance is excellent. The main goal now is to stay consistent, especially in high-credit courses because they can quickly affect your GPA if their marks drop."
            );
        } else if (percentage.compareTo(new BigDecimal("80")) >= 0) {
            overallLevel = "VERY_GOOD";
            summary = bi(
                    "أداءك جيد جدًا، وعندك فرصة واضحة لرفع المعدل أكثر إذا ركزت على المواد ذات التأثير الأعلى: الساعات الأعلى أو العلامات الأقل.",
                    "Your performance is very good, and you have a clear chance to improve more by focusing on the highest-impact courses: higher credits or lower marks."
            );
        } else if (percentage.compareTo(new BigDecimal("70")) >= 0) {
            overallLevel = "GOOD";
            summary = bi(
                    "أداءك جيد، لكن يوجد مواد يمكن أن ترفع المعدل بشكل ملحوظ إذا تحسنت، خصوصًا المواد الثقيلة بالساعات أو التي علامتها الحالية منخفضة.",
                    "Your performance is good, but some courses can noticeably improve your GPA if you improve them, especially high-credit courses or courses with lower current marks."
            );
        } else if (percentage.compareTo(new BigDecimal("60")) >= 0) {
            overallLevel = "NEEDS_IMPROVEMENT";
            summary = bi(
                    "معدلك مقبول لكنه يحتاج خطة واضحة. ابدأ بالمواد ذات الساعات الأعلى والعلامات الأقل لأنها تعطي أكبر فرق عند التحسين.",
                    "Your average is acceptable, but it needs a clear plan. Start with high-credit and low-mark courses because improving them creates the biggest difference."
            );
        } else {
            overallLevel = "AT_RISK";
            summary = bi(
                    "معدلك منخفض حاليًا. الأولوية الآن ليست تحسين كل شيء بنفس الوقت، بل التركيز على المواد الأساسية والثقيلة بالساعات لتقليل الخسارة ورفع المعدل تدريجيًا.",
                    "Your average is currently low. The priority is not to improve everything at once, but to focus on core and high-credit courses to reduce GPA loss and improve gradually."
            );
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
                    .title(bi("أولوية التحسين", "Improvement Priority"))
                    .message(explainWeakness(weakest))
                    .build());
        }

        if (!highestImpactCourses.isEmpty()) {
            CourseImpactDTO highestImpact = highestImpactCourses.get(0);
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.INFO)
                    .title(bi("أعلى تأثير على المعدل", "Highest GPA Impact"))
                    .message(explainImpact(highestImpact))
                    .build());
        }

        long missingGradesCount = calculation.getItems() == null ? 0 : calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(this::isRemainingItem)
                .count();

        if (missingGradesCount > 0) {
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.INFO)
                    .title(bi("فرصة التحسين ما زالت موجودة", "Improvement Chance Still Exists"))
                    .message(bi(
                            "عندك " + missingGradesCount + " مادة/مواد بدون علامة نهائية حتى الآن. هذا مهم لأن المواد المتبقية ما زالت قادرة على تغيير المعدل، ويمكن استخدام تحليل ماذا لو لمعرفة العلامة المطلوبة.",
                            "You still have " + missingGradesCount + " course(s) without a final grade. This matters because remaining courses can still change your GPA, and you can use What-If analysis to know the required mark."
                    ))
                    .build());
        }

        advice.add(GradeAdviceDTO.builder()
                .level(GradeAdviceLevel.INFO)
                .title(bi("كيف يتم التعامل مع المواد المعادة؟", "How repeated courses are handled"))
                .message(repeatPolicyExplanation(calculation))
                .build());

        if (gpa.compareTo(gpaScale.multiply(new BigDecimal("0.50"))) < 0) {
            warnings.add(bi(
                    "معدلك على مقياس GPA ما زال منخفضًا نسبيًا مقارنة بمنتصف المقياس، لذلك ركز على المواد التي تجمع بين ساعات عالية وعلامة منخفضة.",
                    "Your GPA is still relatively low compared with the middle of the scale, so focus on courses that combine high credits with low marks."
            ));
        }

        if (weakestCourses.size() >= 2) {
            warnings.add(bi(
                    "عندك أكثر من مادة منخفضة. لا تبدأ عشوائيًا؛ ابدأ بالمادة ذات الساعات الأعلى لأنها تؤثر أكثر على المعدل.",
                    "You have more than one low course. Do not start randomly; begin with the higher-credit course because it affects the GPA more."
            ));
        }

        if (percentage.compareTo(new BigDecimal("85")) >= 0) {
            advice.add(GradeAdviceDTO.builder()
                    .level(GradeAdviceLevel.SUCCESS)
                    .title(bi("استمر", "Keep Going"))
                    .message(bi(
                            "أداؤك قوي. المحافظة على هذا المستوى في المواد ذات الساعات العالية أهم من محاولة رفع مادة صغيرة التأثير فقط.",
                            "Your performance is strong. Maintaining this level in high-credit courses is more important than only improving a low-impact course."
                    ))
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
        BigDecimal weakness = ONE_HUNDRED.subtract(percentage);

        if (weakness.compareTo(ZERO) < 0) {
            weakness = ZERO;
        }

        return credits.multiply(weakness).setScale(4, RoundingMode.HALF_UP);
    }

    private GradeWhatIfResponse analyzeSemesterWhatIf(GradeCalculation calculation, GradeWhatIfRequest request) {
        WhatIfTargetType targetType = request.getTargetType() != null
                ? request.getTargetType()
                : WhatIfTargetType.PERCENTAGE;

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

        if (totalCredits.compareTo(ZERO) <= 0) {
            return GradeWhatIfResponse.builder()
                    .possible(false)
                    .scope("SEMESTER")
                    .targetType(targetType.name())
                    .targetValue(targetValue)
                    .currentValue(currentValue)
                    .gradedCredits(gradedCredits)
                    .remainingCredits(remainingCredits)
                    .totalCredits(totalCredits)
                    .maxPossibleValue(currentValue)
                    .message(bi(
                            "لا توجد ساعات كافية لإجراء تحليل ماذا لو.",
                            "There are not enough credits to run a What-If analysis."
                    ))
                    .build();
        }

        if (remainingCredits.compareTo(ZERO) <= 0) {
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
                            ? bi(
                            "الهدف محقق أصلًا لأنه لا توجد مواد متبقية لتغيير النتيجة.",
                            "The target is already achieved because there are no remaining courses that can change the result."
                    )
                            : bi(
                            "لا توجد مواد متبقية، لذلك لا يمكن رفع النتيجة أكثر ضمن هذا الفصل.",
                            "There are no remaining courses, so the result cannot be improved further within this semester."
                    ))
                    .build();
        }

        BigDecimal currentWeighted = targetType == WhatIfTargetType.GPA
                ? weightedCurrentGpaPoints(calculation)
                : weightedCurrentPercentagePoints(calculation);

        BigDecimal scaleMax = resolveScaleMax(calculation, targetType);

        BigDecimal requiredAverageOnRemaining = targetValue.multiply(totalCredits)
                .subtract(currentWeighted)
                .divide(remainingCredits, 8, RoundingMode.HALF_UP);

        BigDecimal maxPossible = currentWeighted.add(remainingCredits.multiply(scaleMax))
                .divide(totalCredits, 8, RoundingMode.HALF_UP);

        boolean possible = requiredAverageOnRemaining.compareTo(ZERO) >= 0
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
            notes.add(bi(
                    "الهدف ممكن إذا حصلت تقريبًا على " + fmt(requiredAverageOnRemaining) + " في متوسط المواد المتبقية.",
                    "The target is possible if you score about " + fmt(requiredAverageOnRemaining) + " on average in the remaining courses."
            ));
            notes.add(bi(
                    "المواد ذات الساعات الأعلى تحتاج اهتمامًا أكبر، لأن نفس العلامة فيها تؤثر أكثر على النتيجة النهائية.",
                    "Higher-credit courses need more attention because the same mark in them has a bigger effect on the final result."
            ));
        } else {
            notes.add(bi(
                    "الهدف غير ممكن ضمن هذا الفصل فقط حسب المواد المتبقية.",
                    "The target is not possible within this semester only based on the remaining courses."
            ));
            notes.add(bi(
                    "أعلى نتيجة ممكنة تقريبًا هي " + fmt(maxPossible) + ".",
                    "The approximate maximum possible result is " + fmt(maxPossible) + "."
            ));
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
                        ? bi(
                        "يمكنك الوصول إلى الهدف إذا حققت المتوسط المطلوب في المواد المتبقية.",
                        "You can reach the target if you achieve the required average in the remaining courses."
                )
                        : bi(
                        "هذا الهدف غير ممكن في هذا الفصل فقط.",
                        "This target is not possible within this semester only."
                ))
                .requiredPerCourse(perCourse)
                .notes(notes)
                .build();
    }

    private GradeWhatIfResponse analyzeCumulativeWhatIf(GradeCalculation calculation, GradeWhatIfRequest request) {
        WhatIfTargetType targetType = request.getTargetType() != null
                ? request.getTargetType()
                : WhatIfTargetType.PERCENTAGE;

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
        BigDecimal totalCumulativeCredits = completedCredits.add(semesterCredits);

        if (semesterCredits.compareTo(ZERO) <= 0) {
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
                    .message(bi(
                            "لا توجد ساعات فصلية كافية لإجراء تحليل تراكمي.",
                            "There are not enough semester credits to run a cumulative analysis."
                    ))
                    .build();
        }

        if (completedCredits.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Completed credits cannot be negative");
        }

        if (totalCumulativeCredits.compareTo(ZERO) <= 0) {
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
                    .message(bi(
                            "لا يمكن إجراء تحليل تراكمي بدون ساعات مكتملة أو ساعات فصلية.",
                            "A cumulative analysis cannot be performed without completed or semester credits."
                    ))
                    .build();
        }

        BigDecimal scaleMax = resolveScaleMax(calculation, targetType);

        BigDecimal requiredSemesterValue = targetValue.multiply(totalCumulativeCredits)
                .subtract(currentCumulative.multiply(completedCredits))
                .divide(semesterCredits, 8, RoundingMode.HALF_UP);

        BigDecimal currentSemesterWeighted = targetType == WhatIfTargetType.GPA
                ? weightedCurrentGpaPoints(calculation)
                : weightedCurrentPercentagePoints(calculation);

        BigDecimal currentSemesterValue = semesterCredits.compareTo(ZERO) > 0
                ? currentSemesterWeighted.divide(semesterCredits, 8, RoundingMode.HALF_UP)
                : ZERO;

        BigDecimal requiredAverageOnRemaining;
        if (remainingCredits.compareTo(ZERO) > 0) {
            requiredAverageOnRemaining = requiredSemesterValue.multiply(semesterCredits)
                    .subtract(currentSemesterWeighted)
                    .divide(remainingCredits, 8, RoundingMode.HALF_UP);
        } else {
            requiredAverageOnRemaining = ZERO;
        }

        BigDecimal maxPossibleSemester = currentSemesterWeighted.add(remainingCredits.multiply(scaleMax))
                .divide(semesterCredits, 8, RoundingMode.HALF_UP);

        BigDecimal maxPossibleCumulative = currentCumulative.multiply(completedCredits)
                .add(maxPossibleSemester.multiply(semesterCredits))
                .divide(totalCumulativeCredits, 8, RoundingMode.HALF_UP);

        BigDecimal projectedCurrentCumulative = currentCumulative.multiply(completedCredits)
                .add(currentSemesterValue.multiply(semesterCredits))
                .divide(totalCumulativeCredits, 8, RoundingMode.HALF_UP);

        boolean semesterTargetInScale = requiredSemesterValue.compareTo(ZERO) >= 0
                && requiredSemesterValue.compareTo(scaleMax) <= 0;

        boolean remainingTargetInScale = remainingCredits.compareTo(ZERO) <= 0
                || (requiredAverageOnRemaining.compareTo(ZERO) >= 0
                && requiredAverageOnRemaining.compareTo(scaleMax) <= 0);

        boolean possible = semesterTargetInScale
                && remainingTargetInScale
                && maxPossibleCumulative.compareTo(targetValue) >= 0;

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
        notes.add(bi(
                "هذا التحليل يفترض أن الساعات المكتملة السابقة ثابتة، وأن التحسين المطلوب كله سيأتي من هذا الفصل.",
                "This analysis assumes your previously completed credits are fixed, and the required improvement must come from this semester."
        ));

        notes.add(bi(
                "التراكمي المتوقع حسب العلامات الحالية تقريبًا هو " + fmt(projectedCurrentCumulative) + ".",
                "The projected cumulative result based on current marks is about " + fmt(projectedCurrentCumulative) + "."
        ));

        if (remainingCredits.compareTo(ZERO) <= 0) {
            notes.add(bi(
                    "لا توجد مواد متبقية، لذلك تم حساب النتيجة بناءً على العلامات المدخلة حاليًا فقط.",
                    "There are no remaining courses, so the result is based only on the currently entered marks."
            ));
        }

        if (possible) {
            notes.add(bi(
                    "للوصول إلى الهدف التراكمي، تحتاج نتيجة فصلية تقريبًا " + fmt(requiredSemesterValue)
                            + " ومتوسطًا على المواد المتبقية تقريبًا " + fmt(requiredAverageOnRemaining) + ".",
                    "To reach the cumulative target, you need an approximate semester result of "
                            + fmt(requiredSemesterValue) + " and an average of about "
                            + fmt(requiredAverageOnRemaining) + " in the remaining courses."
            ));
        } else {
            notes.add(bi(
                    "أعلى تراكمي ممكن تقريبًا بعد هذا الفصل هو " + fmt(maxPossibleCumulative) + ".",
                    "The approximate maximum cumulative result after this semester is " + fmt(maxPossibleCumulative) + "."
            ));
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
                        ? bi(
                        "الوصول إلى الهدف التراكمي ممكن إذا حققت المعدل المطلوب هذا الفصل.",
                        "Reaching the cumulative target is possible if you achieve the required semester result."
                )
                        : bi(
                        "الهدف التراكمي غير ممكن من خلال هذا الفصل فقط.",
                        "The cumulative target is not possible through this semester only."
                ))
                .requiredPerCourse(perCourse)
                .notes(notes)
                .build();
    }

    private boolean isRemainingItem(GradeCourseItem item) {
        if (item == null) {
            return false;
        }

        if (!Boolean.TRUE.equals(item.getIncludedInCalculation())) {
            return false;
        }

        if (item.getCourseStatus() == GradeCourseStatus.WITHDRAWN) {
            return false;
        }

        boolean noNumericValue = item.getEnteredValue() == null;
        boolean noLetter = item.getLetterGrade() == null || item.getLetterGrade().isBlank();
        boolean noNormalizedResult = item.getNormalizedPercentage() == null && item.getGradePoints() == null;

        return (item.getCourseStatus() == GradeCourseStatus.REGISTERED)
                || ((noNumericValue && noLetter) || noNormalizedResult);
    }

    private BigDecimal weightedCurrentPercentagePoints(GradeCalculation calculation) {
        return calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getIncludedInCalculation()))
                .filter(item -> !isRemainingItem(item))
                .map(item -> nz(item.getNormalizedPercentage()).multiply(nz(item.getCreditHoursSnapshot())))
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal weightedCurrentGpaPoints(GradeCalculation calculation) {
        return calculation.getItems().stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getIncludedInCalculation()))
                .filter(item -> !isRemainingItem(item))
                .map(item -> nz(item.getGradePoints()).multiply(nz(item.getCreditHoursSnapshot())))
                .reduce(ZERO, BigDecimal::add);
    }

    private BigDecimal sumCredits(List<GradeCourseItem> items) {
        if (items == null || items.isEmpty()) {
            return ZERO;
        }

        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> Boolean.TRUE.equals(item.getIncludedInCalculation()))
                .map(item -> nz(item.getCreditHoursSnapshot()))
                .reduce(ZERO, BigDecimal::add);
    }

    private GradeCalculation findCalculation(Long calculationId, Long studentId) {
        return calculationRepository.findByIdAndStudentId(calculationId, studentId)
                .orElseThrow(() -> new EntityNotFoundException("Grade calculation not found"));
    }

    private Student findStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));
    }

    private void ensureCalculationIsEditable(GradeCalculation calculation) {
        if (Boolean.TRUE.equals(calculation.getIsLocked())) {
            throw new IllegalStateException("This grade calculation is locked");
        }
    }

    private void validateCreateRequest(CreateGradeCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Grade calculation request is required");
        }

        validateScaleValues(request.getGpaScaleMax(), request.getMarkScaleMax());

        if (request.getItems() != null) {
            request.getItems().forEach(this::validateCourseItemRequest);
        }
    }

    private void validateUpdateRequest(UpdateGradeCalculationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Grade calculation update request is required");
        }

        validateScaleValues(request.getGpaScaleMax(), request.getMarkScaleMax());

        if (request.getItems() != null) {
            request.getItems().forEach(this::validateCourseItemRequest);
        }
    }

    private void validateWhatIfRequest(GradeWhatIfRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("What-if request is required");
        }

        if (request.getTargetValue() == null) {
            throw new IllegalArgumentException("Target value is required");
        }

        if (request.getTargetValue().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Target value cannot be negative");
        }

        if (request.getCurrentCumulativeValue() != null
                && request.getCurrentCumulativeValue().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Current cumulative value cannot be negative");
        }

        if (request.getCompletedCredits() != null
                && request.getCompletedCredits().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Completed credits cannot be negative");
        }
    }

    private void validateScaleValues(BigDecimal gpaScaleMax, BigDecimal markScaleMax) {
        if (gpaScaleMax != null && gpaScaleMax.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("GPA scale max must be greater than zero");
        }

        if (markScaleMax != null && markScaleMax.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Mark scale max must be greater than zero");
        }
    }

    private void validateCourseItemRequest(GradeCourseItemRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Course item request is required");
        }

        if (request.getCreditHours() != null && request.getCreditHours().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Credit hours cannot be negative");
        }

        if (request.getEnteredValue() != null && request.getEnteredValue().compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Entered value cannot be negative");
        }

        if (request.getEnteredOutOf() != null && request.getEnteredOutOf().compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("Entered out-of value must be greater than zero");
        }

        if (request.getEnteredValue() != null
                && request.getEnteredOutOf() != null
                && request.getEnteredValue().compareTo(request.getEnteredOutOf()) > 0) {
            throw new IllegalArgumentException("Entered value cannot be greater than entered out-of value");
        }
    }

    private void normalizeOrderIndexes(GradeCalculation calculation) {
        if (calculation == null || calculation.getItems() == null || calculation.getItems().isEmpty()) {
            return;
        }

        int index = 1;

        List<GradeCourseItem> sortedItems = calculation.getItems()
                .stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        GradeCourseItem::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();

        for (GradeCourseItem item : sortedItems) {
            item.setOrderIndex(index++);
        }
    }

    private boolean isDuplicateCourseItem(GradeCalculation calculation, GradeCourseItem candidate) {
        if (calculation == null || calculation.getItems() == null || candidate == null) {
            return false;
        }

        return calculation.getItems()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(existing -> sameCourseIdentity(existing, candidate));
    }

    private boolean sameCourseIdentity(GradeCourseItem a, GradeCourseItem b) {
        if (a == null || b == null) {
            return false;
        }

        Long aCourseId = a.getCourse() != null ? a.getCourse().getId() : null;
        Long bCourseId = b.getCourse() != null ? b.getCourse().getId() : null;

        if (aCourseId != null && bCourseId != null && Objects.equals(aCourseId, bCourseId)) {
            return true;
        }

        Long aSectionId = a.getCourseSection() != null ? a.getCourseSection().getId() : null;
        Long bSectionId = b.getCourseSection() != null ? b.getCourseSection().getId() : null;

        if (aSectionId != null && bSectionId != null && Objects.equals(aSectionId, bSectionId)) {
            return true;
        }

        String aCode = normalizeKey(a.getCourseCodeSnapshot());
        String bCode = normalizeKey(b.getCourseCodeSnapshot());

        if (aCode != null && bCode != null && aCode.equals(bCode)) {
            return true;
        }

        String aName = normalizeKey(a.getCourseNameSnapshot());
        String bName = normalizeKey(b.getCourseNameSnapshot());

        return aName != null && bName != null && aName.equals(bName);
    }

    private String normalizeKey(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal resolveScaleMax(GradeCalculation calculation, WhatIfTargetType targetType) {
        if (targetType == WhatIfTargetType.GPA) {
            BigDecimal scale = nz(calculation.getGpaScaleMax());
            return scale.compareTo(ZERO) > 0 ? scale : DEFAULT_GPA_SCALE;
        }

        return ONE_HUNDRED;
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

    private String bi(String ar, String en) {
        return ar + "\n" + en;
    }

    private String fmt(BigDecimal value) {
        return nz(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String safeCourseName(CourseImpactDTO course) {
        if (course.getCourseName() == null || course.getCourseName().isBlank()) {
            return "Unnamed Course";
        }
        return course.getCourseName();
    }

    private String explainWeakness(CourseImpactDTO course) {
        String name = safeCourseName(course);
        String credits = fmt(course.getCreditHours());
        String percentage = course.getCurrentPercentage() == null
                ? "غير مدخلة / Not entered"
                : fmt(course.getCurrentPercentage()) + "%";

        return bi(
                "ركز أولًا على " + name + " لأنها من أضعف المواد عندك حاليًا. علامتك فيها " + percentage
                        + " وعدد ساعاتها " + credits
                        + ". إذا كانت المادة منخفضة ومعها ساعات أكثر، فإن تحسينها يساعد المعدل أكثر من مادة منخفضة بساعات قليلة.",
                "Focus first on " + name + " because it is one of your weakest courses right now. Your current mark is "
                        + percentage + " and it has " + credits
                        + " credit hours. When a low mark belongs to a higher-credit course, improving it helps the GPA more than improving a low-credit course."
        );
    }

    private String explainImpact(CourseImpactDTO course) {
        String name = safeCourseName(course);
        String credits = fmt(course.getCreditHours());
        String percentage = course.getCurrentPercentage() == null
                ? "غير مدخلة / Not entered"
                : fmt(course.getCurrentPercentage()) + "%";
        String impact = fmt(course.getImpactScore());

        return bi(
                "مادة " + name + " لها أعلى تأثير حاليًا لأن عدد ساعاتها " + credits
                        + " وعلامتك الحالية فيها " + percentage
                        + ". درجة التأثير المحسوبة هي " + impact
                        + ". هذا يعني أن رفعها سيعطي فرقًا أكبر من مواد تأثيرها أقل.",
                name + " currently has the highest impact because it has " + credits
                        + " credit hours and your current mark is " + percentage
                        + ". The calculated impact score is " + impact
                        + ". This means improving it will create a bigger difference than improving lower-impact courses."
        );
    }

    private String repeatPolicyExplanation(GradeCalculation calculation) {
        if (calculation.getRepeatPolicy() == null) {
            return bi(
                    "سياسة المواد المعادة غير محددة، لذلك سيتم التعامل مع المحاولات حسب الإعداد الافتراضي للنظام.",
                    "The repeated-course policy is not specified, so attempts will be handled using the system default."
            );
        }

        return switch (calculation.getRepeatPolicy()) {
            case ALL_ATTEMPTS -> bi(
                    "إذا أعدت مادة، فكل المحاولات تدخل في حساب المعدل. مثال: لو أخذت مادة 4 ساعات مرتين، المحاولتان تدخلان في مجموع الساعات والنقاط.",
                    "If you repeat a course, all attempts are included in the GPA. Example: if you take a 4-credit course twice, both attempts are included in total credits and points."
            );
            case HIGHEST_ATTEMPT -> bi(
                    "إذا أعدت مادة، يتم احتساب أعلى محاولة فقط. مثال: إذا حصلت أول مرة على 55 ثم أعدتها وحصلت على 80، سيتم اعتماد 80 فقط.",
                    "If you repeat a course, only the highest attempt is counted. Example: if you got 55 first and then 80 after repeating it, only 80 will be used."
            );
            case LAST_ATTEMPT -> bi(
                    "إذا أعدت مادة، يتم احتساب آخر محاولة فقط حتى لو كانت أقل من المحاولة السابقة.",
                    "If you repeat a course, only the latest attempt is counted, even if it is lower than the previous attempt."
            );
            case EXCLUDE_REPEATED -> bi(
                    "المواد المعادة يتم استبعادها من الحساب حسب السياسة الحالية، لذلك لن تؤثر على المعدل.",
                    "Repeated courses are excluded from the GPA based on the current policy, so they will not affect the result."
            );
        };
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value == null ? ZERO : BigDecimal.valueOf(value);
    }

    private BigDecimal nz(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}