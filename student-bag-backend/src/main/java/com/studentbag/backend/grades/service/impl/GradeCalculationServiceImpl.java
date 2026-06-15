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
import com.studentbag.backend.grades.dto.response.WhatIfOptionDTO;
import com.studentbag.backend.grades.dto.response.WhatIfRequiredCourseDTO;
import com.studentbag.backend.grades.entity.GradeCalculation;
import com.studentbag.backend.grades.entity.GradeCourseItem;
import com.studentbag.backend.grades.mapper.GradeCalculationMapper;
import com.studentbag.backend.grades.repository.GradeCalculationRepository;
import com.studentbag.backend.grades.repository.GradeCourseItemRepository;
import com.studentbag.backend.grades.service.GradeCalculationService;
import com.studentbag.backend.grades.service.GradeCalculatorService;
import com.studentbag.backend.schedule.entity.CourseRating;
import com.studentbag.backend.schedule.entity.ScheduleEntry;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.repository.CourseRatingRepository;
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
    private static final BigDecimal NEUTRAL_DIFFICULTY = new BigDecimal("3");
    private static final int MAX_WHAT_IF_OPTIONS = 8;

    private final GradeCalculationRepository calculationRepository;
    private final GradeCourseItemRepository itemRepository;
    private final GradeCalculationMapper mapper;
    private final GradeCalculatorService calculatorService;

    private final StudentRepository studentRepository;
    private final StudentScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final CourseRatingRepository courseRatingRepository;

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

        List<GradeCourseItem> remainingItems = calculation.getItems().stream()
                .filter(this::isRemainingItem)
                .toList();

        Map<Long, BigDecimal> difficultyByCourseId = loadStudentDifficultyMap(calculation);

        List<WhatIfOptionDTO> options = buildWhatIfOptions(
                remainingItems,
                requiredAverageOnRemaining,
                scaleMax,
                difficultyByCourseId
        );

        List<WhatIfRequiredCourseDTO> perCourse = options.isEmpty()
                ? remainingItems.stream()
                .map(item -> WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(requiredAverageOnRemaining.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList()
                : options.get(0).getCourses();

        List<String> notes = new ArrayList<>();
        if (possible) {
            notes.add(bi(
                    "الهدف ممكن إذا حصلت تقريبًا على " + fmt(requiredAverageOnRemaining) + " في متوسط المواد المتبقية.",
                    "The target is possible if you score about " + fmt(requiredAverageOnRemaining) + " on average in the remaining courses."
            ));
            notes.add(bi(
                    "الخطط المقترحة ليست مجرد رقم واحد مكرر؛ كل خطة توزع الجهد بطريقة مختلفة حسب الساعات وترتيب المواد ومستوى الضغط.",
                    "The suggested plans are not just one repeated number; each plan distributes effort differently based on credits, course order, and pressure level."
            ));
            notes.add(bi(
                    "إذا كانت تقييمات صعوبة المواد متوفرة، يتم استخدامها لتحسين توزيع الجهد. وإذا لم تكن متوفرة، يتم اعتبار المادة بدرجة صعوبة محايدة بدون التأثير على التحليل.",
                    "If course difficulty ratings are available, they are used to improve effort distribution. If not available, the course is treated as neutral without affecting the analysis."
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
                        "يمكنك الوصول إلى الهدف إذا حققت المتوسط المطلوب في المواد المتبقية. اختر الخطة الأنسب حسب قدرتك: متوازنة، آمنة، تراعي صعوبة المواد، تركيز على المواد الثقيلة، أو تعويض تدريجي.",
                        "You can reach the target if you achieve the required average in the remaining courses. Choose the plan that fits you best: balanced, safe, difficulty-aware, high-credit focus, or gradual recovery."
                )
                        : bi(
                        "هذا الهدف غير ممكن في هذا الفصل فقط.",
                        "This target is not possible within this semester only."
                ))
                .requiredPerCourse(perCourse)
                .options(options)
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

        List<GradeCourseItem> remainingItems = calculation.getItems().stream()
                .filter(this::isRemainingItem)
                .toList();

        Map<Long, BigDecimal> difficultyByCourseId = loadStudentDifficultyMap(calculation);

        List<WhatIfOptionDTO> options = buildWhatIfOptions(
                remainingItems,
                requiredAverageOnRemaining,
                scaleMax,
                difficultyByCourseId
        );

        List<WhatIfRequiredCourseDTO> perCourse = options.isEmpty()
                ? remainingItems.stream()
                .map(item -> WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(requiredAverageOnRemaining.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList()
                : options.get(0).getCourses();

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
            notes.add(bi(
                    "تقييم صعوبة المادة يستخدم فقط لتحسين توزيع الخطط، وليس لتحديد إمكانية الوصول للهدف.",
                    "Course difficulty rating is used only to improve plan distribution, not to decide whether the target is reachable."
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
                        "الوصول إلى الهدف التراكمي ممكن إذا حققت المعدل المطلوب هذا الفصل. تم تجهيز أكثر من خطة حتى تختار توزيع الجهد الأنسب لك.",
                        "Reaching the cumulative target is possible if you achieve the required semester result. Several plans were prepared so you can choose the most suitable effort distribution."
                )
                        : bi(
                        "الهدف التراكمي غير ممكن من خلال هذا الفصل فقط.",
                        "The cumulative target is not possible through this semester only."
                ))
                .requiredPerCourse(perCourse)
                .options(options)
                .notes(notes)
                .build();
    }

    private List<WhatIfOptionDTO> buildWhatIfOptions(
            List<GradeCourseItem> remainingItems,
            BigDecimal requiredAverage,
            BigDecimal scaleMax,
            Map<Long, BigDecimal> difficultyByCourseId
    ) {
        if (remainingItems == null || remainingItems.isEmpty()) {
            return List.of();
        }

        List<GradeCourseItem> validItems = remainingItems.stream()
                .filter(Objects::nonNull)
                .filter(item -> nz(item.getCreditHoursSnapshot()).compareTo(ZERO) > 0)
                .sorted(Comparator.comparing(
                        GradeCourseItem::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .toList();

        if (validItems.isEmpty()) {
            return List.of();
        }

        if (requiredAverage.compareTo(ZERO) < 0 || requiredAverage.compareTo(scaleMax) > 0) {
            return List.of();
        }

        BigDecimal delta = optionDelta(scaleMax);
        List<WhatIfOptionDTO> options = new ArrayList<>();

        addOptionIfUseful(options, buildBalancedOption(validItems, requiredAverage, scaleMax));

        addOptionIfUseful(options, buildFlatOption(
                validItems,
                requiredAverage,
                scaleMax,
                "SAFE_MARGIN",
                bi("خطة آمنة بهامش إضافي", "Safe margin plan"),
                bi(
                        "تعطي كل مادة علامة أعلى قليلًا من الحد الأدنى المطلوب. مناسبة إذا بدك تقلل خطر النزول البسيط في أي مادة.",
                        "Gives every course a slightly higher mark than the minimum required. Useful when you want to reduce the risk of a small drop in any course."
                ),
                delta.multiply(new BigDecimal("0.45"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "DIFFICULTY_AWARE",
                bi("خطة تراعي صعوبة المواد", "Difficulty-aware plan"),
                bi(
                        "تخفف الضغط عن المواد الأعلى صعوبة، وتعوض بشكل محسوب في المواد الأسهل أو المواد غير المصنفة. إذا لم يكن تقييم الصعوبة موجودًا، تعتبر المادة محايدة.",
                        "Reduces pressure on higher-difficulty courses and compensates in easier or unrated courses. If difficulty rating is missing, the course is treated as neutral."
                ),
                buildDifficultyAwareScores(validItems, difficultyByCourseId),
                delta.multiply(new BigDecimal("1.25"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "HIGH_CREDIT_PRIORITY",
                bi("خطة المواد الأعلى ساعات", "High-credit priority plan"),
                bi(
                        "ترفع المطلوب في المواد الأعلى ساعات لأنها تؤثر أكثر على المعدل، وتخفف قليلًا عن المواد الأقل ساعات.",
                        "Raises targets in higher-credit courses because they affect the average more, while slightly reducing pressure on lower-credit courses."
                ),
                buildCreditAndOrderScores(validItems, true),
                delta.multiply(new BigDecimal("1.30"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "LIGHT_COURSE_COMPENSATION",
                bi("خطة التعويض بالمواد الأخف", "Light-course compensation plan"),
                bi(
                        "تجعل المواد الأقل ساعات مجالًا للتعويض بعلامات أعلى، وتخفف الضغط عن المواد الثقيلة.",
                        "Uses lower-credit courses as a compensation area with higher targets, while reducing pressure on heavier courses."
                ),
                buildLightCourseCompensationScores(validItems),
                delta.multiply(new BigDecimal("1.20"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "HEAVY_COURSE_RELIEF",
                bi("خطة تخفيف ضغط المواد الثقيلة", "Heavy-course relief plan"),
                bi(
                        "تخفض المطلوب في المواد الأعلى ساعات بشكل محسوب، وتعوض الفرق في المواد الأقل ساعات حتى تكون الخطة أريح.",
                        "Lowers the requirement in higher-credit courses in a controlled way and compensates through lower-credit courses for a more comfortable plan."
                ),
                buildNegativeCreditAndOrderScores(validItems),
                delta.multiply(new BigDecimal("1.05"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "TWO_MAIN_COURSES_PUSH",
                bi("خطة دفع مادتين أساسيتين", "Two-main-courses push plan"),
                bi(
                        "تركز على أعلى مادتين تأثيرًا وتجعل علاماتهما أعلى، مع إبقاء باقي المواد قريبة من المطلوب.",
                        "Focuses on the two highest-impact courses by giving them higher targets while keeping the rest closer to the required level."
                ),
                buildTopNCreditScores(validItems, 2),
                delta.multiply(new BigDecimal("1.55"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "ONE_KEY_COURSE_PUSH",
                bi("خطة مادة مفتاحية واحدة", "One key-course push plan"),
                bi(
                        "تختار مادة واحدة ذات تأثير عالٍ وترفع المطلوب فيها أكثر، حتى تساعد باقي المواد تبقى أهدأ.",
                        "Chooses one high-impact course and raises its target more, helping the remaining courses stay lighter."
                ),
                buildTopNCreditScores(validItems, 1),
                delta.multiply(new BigDecimal("1.70"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "STRONG_START",
                bi("خطة بداية قوية", "Strong-start plan"),
                bi(
                        "ترفع المطلوب في أول نصف من المواد المتبقية، حتى يبدأ الطالب بقوة ويخف الضغط لاحقًا.",
                        "Raises targets in the first half of the remaining courses so the student starts strong and reduces pressure later."
                ),
                buildHalfScores(validItems, true),
                delta.multiply(new BigDecimal("1.10"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "LATE_RECOVERY",
                bi("خطة تعويض متأخر", "Late-recovery plan"),
                bi(
                        "تخفف بداية الخطة قليلًا، ثم ترفع المطلوب في النصف الثاني من المواد للتعويض التدريجي.",
                        "Makes the beginning slightly lighter, then raises targets in the second half for gradual recovery."
                ),
                buildHalfScores(validItems, false),
                delta.multiply(new BigDecimal("1.10"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "ASCENDING_EFFORT",
                bi("خطة جهد تصاعدي", "Ascending-effort plan"),
                bi(
                        "تبدأ بعلامات أقرب للحد الأدنى، ثم ترفع المطلوب تدريجيًا في المواد اللاحقة.",
                        "Starts closer to the minimum required and gradually increases targets in later courses."
                ),
                buildOrderScores(validItems, false),
                delta.multiply(new BigDecimal("0.95"))
        ));

        addOptionIfUseful(options, buildProfileOption(
                validItems,
                requiredAverage,
                scaleMax,
                "ALTERNATING_LOAD",
                bi("خطة توزيع متناوب", "Alternating-load plan"),
                bi(
                        "توزع الضغط بالتناوب بين مادة أعلى ومادة أخف، حتى لا تظهر كل العلامات بنفس الرقم.",
                        "Alternates pressure between higher and lighter course targets so the marks do not all look identical."
                ),
                buildRotatingScores(validItems, true),
                delta.multiply(new BigDecimal("1.00"))
        ));

        return options.stream()
                .filter(Objects::nonNull)
                .filter(WhatIfOptionDTO::isPossible)
                .limit(MAX_WHAT_IF_OPTIONS)
                .toList();
    }

    private void addOptionIfUseful(List<WhatIfOptionDTO> options, WhatIfOptionDTO option) {
        if (option == null || !option.isPossible()) {
            return;
        }

        String signature = optionSignature(option);
        boolean alreadyExists = options.stream()
                .filter(Objects::nonNull)
                .map(this::optionSignature)
                .anyMatch(signature::equals);

        if (!alreadyExists) {
            options.add(option);
        }
    }

    private String optionSignature(WhatIfOptionDTO option) {
        if (option == null || option.getCourses() == null) {
            return "";
        }

        return option.getCourses().stream()
                .map(course -> nz(course.getRequiredValue()).setScale(2, RoundingMode.HALF_UP).toPlainString())
                .collect(Collectors.joining("|"));
    }

    private WhatIfOptionDTO buildBalancedOption(
            List<GradeCourseItem> items,
            BigDecimal requiredAverage,
            BigDecimal scaleMax
    ) {
        List<WhatIfRequiredCourseDTO> courses = items.stream()
                .map(item -> WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(clamp(requiredAverage, ZERO, scaleMax).setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        return WhatIfOptionDTO.builder()
                .optionCode("BALANCED_MINIMUM")
                .title(bi("خطة الحد الأدنى المتوازن", "Balanced minimum plan"))
                .description(bi(
                        "تعطي كل مادة تقريبًا نفس العلامة المطلوبة. هذه أبسط خطة وتوضح الحد الأدنى اللازم للوصول للهدف.",
                        "Gives every course almost the same required mark. This is the simplest plan and shows the minimum needed to reach the target."
                ))
                .projectedAverage(weightedAverage(courses).setScale(2, RoundingMode.HALF_UP))
                .possible(isOptionValid(courses, requiredAverage, scaleMax))
                .courses(courses)
                .build();
    }

    private WhatIfOptionDTO buildFlatOption(
            List<GradeCourseItem> items,
            BigDecimal requiredAverage,
            BigDecimal scaleMax,
            String optionCode,
            String title,
            String description,
            BigDecimal bonus
    ) {
        BigDecimal value = clamp(requiredAverage.add(nz(bonus)), ZERO, scaleMax);

        List<WhatIfRequiredCourseDTO> courses = items.stream()
                .map(item -> WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(value.setScale(2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        return WhatIfOptionDTO.builder()
                .optionCode(optionCode)
                .title(title)
                .description(description)
                .projectedAverage(weightedAverage(courses).setScale(2, RoundingMode.HALF_UP))
                .possible(isOptionValid(courses, requiredAverage, scaleMax))
                .courses(courses)
                .build();
    }

    private WhatIfOptionDTO buildProfileOption(
            List<GradeCourseItem> items,
            BigDecimal requiredAverage,
            BigDecimal scaleMax,
            String optionCode,
            String title,
            String description,
            List<BigDecimal> scores,
            BigDecimal initialAmplitude
    ) {
        if (items == null || items.isEmpty() || scores == null || scores.size() != items.size()) {
            return null;
        }

        List<WhatIfRequiredCourseDTO> courses = buildProfileCourses(
                items,
                requiredAverage,
                scaleMax,
                scores,
                initialAmplitude
        );

        if (courses == null || courses.isEmpty()) {
            return null;
        }

        return WhatIfOptionDTO.builder()
                .optionCode(optionCode)
                .title(title)
                .description(description)
                .projectedAverage(weightedAverage(courses).setScale(2, RoundingMode.HALF_UP))
                .possible(isOptionValid(courses, requiredAverage, scaleMax))
                .courses(courses)
                .build();
    }

    private List<WhatIfRequiredCourseDTO> buildProfileCourses(
            List<GradeCourseItem> items,
            BigDecimal requiredAverage,
            BigDecimal scaleMax,
            List<BigDecimal> scores,
            BigDecimal initialAmplitude
    ) {
        BigDecimal totalCredits = sumCredits(items);

        if (totalCredits.compareTo(ZERO) <= 0) {
            return List.of();
        }

        BigDecimal weightedScoreSum = ZERO;

        for (int i = 0; i < items.size(); i++) {
            weightedScoreSum = weightedScoreSum.add(
                    nz(scores.get(i)).multiply(nz(items.get(i).getCreditHoursSnapshot()))
            );
        }

        BigDecimal weightedScoreAverage = weightedScoreSum.divide(totalCredits, 8, RoundingMode.HALF_UP);
        BigDecimal amplitude = nz(initialAmplitude);

        for (int attempt = 0; attempt < 16; attempt++) {
            List<WhatIfRequiredCourseDTO> courses = new ArrayList<>();
            boolean insideScale = true;

            for (int i = 0; i < items.size(); i++) {
                GradeCourseItem item = items.get(i);
                BigDecimal centeredScore = nz(scores.get(i)).subtract(weightedScoreAverage);
                BigDecimal value = requiredAverage.add(centeredScore.multiply(amplitude));

                if (value.compareTo(ZERO) < 0 || value.compareTo(scaleMax) > 0) {
                    insideScale = false;
                    break;
                }

                courses.add(WhatIfRequiredCourseDTO.builder()
                        .itemId(item.getId())
                        .courseName(item.getCourseNameSnapshot())
                        .creditHours(nz(item.getCreditHoursSnapshot()))
                        .requiredValue(value.setScale(2, RoundingMode.HALF_UP))
                        .build());
            }

            if (insideScale && isOptionValid(courses, requiredAverage, scaleMax)) {
                return courses;
            }

            amplitude = amplitude.multiply(new BigDecimal("0.70"));
        }

        return null;
    }

    private List<BigDecimal> buildCreditScores(List<GradeCourseItem> items) {
        return items.stream()
                .map(item -> nz(item.getCreditHoursSnapshot()))
                .toList();
    }

    private List<BigDecimal> buildNegativeCreditScores(List<GradeCourseItem> items) {
        return items.stream()
                .map(item -> nz(item.getCreditHoursSnapshot()).negate())
                .toList();
    }

    private List<BigDecimal> buildCreditAndOrderScores(List<GradeCourseItem> items, boolean highCreditPriority) {
        int size = Math.max(1, items.size());
        List<BigDecimal> scores = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            BigDecimal creditScore = nz(items.get(i).getCreditHoursSnapshot());
            BigDecimal orderScore = BigDecimal.valueOf(size - i).divide(BigDecimal.valueOf(size), 8, RoundingMode.HALF_UP);

            BigDecimal mixed = highCreditPriority
                    ? creditScore.add(orderScore.multiply(new BigDecimal("0.35")))
                    : creditScore.negate().add(orderScore.multiply(new BigDecimal("0.35")));

            scores.add(mixed);
        }

        return scores;
    }

    private List<BigDecimal> buildNegativeCreditAndOrderScores(List<GradeCourseItem> items) {
        int size = Math.max(1, items.size());
        List<BigDecimal> scores = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            BigDecimal creditScore = nz(items.get(i).getCreditHoursSnapshot()).negate();
            BigDecimal orderScore = BigDecimal.valueOf(i + 1).divide(BigDecimal.valueOf(size), 8, RoundingMode.HALF_UP);
            scores.add(creditScore.add(orderScore.multiply(new BigDecimal("0.25"))));
        }

        return scores;
    }

    private List<BigDecimal> buildLightCourseCompensationScores(List<GradeCourseItem> items) {
        int size = Math.max(1, items.size());
        List<BigDecimal> scores = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            BigDecimal credits = nz(items.get(i).getCreditHoursSnapshot());

            BigDecimal inverseCredit = credits.compareTo(ZERO) <= 0
                    ? ZERO
                    : BigDecimal.ONE.divide(credits, 8, RoundingMode.HALF_UP);

            BigDecimal orderVariation = BigDecimal.valueOf((i % 2 == 0) ? 1 : -1)
                    .multiply(new BigDecimal("0.20"));

            BigDecimal position = BigDecimal.valueOf(i + 1)
                    .divide(BigDecimal.valueOf(size), 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("0.15"));

            scores.add(inverseCredit.add(orderVariation).add(position));
        }

        return scores;
    }

    private List<BigDecimal> buildDifficultyAwareScores(
            List<GradeCourseItem> items,
            Map<Long, BigDecimal> difficultyByCourseId
    ) {
        return items.stream()
                .map(item -> {
                    BigDecimal difficulty = difficultyOf(item, difficultyByCourseId);

                    /*
                     * Difficulty scale:
                     * 1 = easy
                     * 3 = neutral
                     * 5 = hard
                     *
                     * Higher score inside buildProfileCourses means higher required target.
                     * So we reverse difficulty:
                     * Easy course gets higher target.
                     * Hard course gets lower target.
                     * Unrated course is neutral.
                     */
                    return new BigDecimal("6").subtract(difficulty);
                })
                .toList();
    }

    private List<BigDecimal> buildInverseCreditScores(List<GradeCourseItem> items) {
        return items.stream()
                .map(item -> {
                    BigDecimal credits = nz(item.getCreditHoursSnapshot());
                    if (credits.compareTo(ZERO) <= 0) {
                        return ZERO;
                    }

                    return BigDecimal.ONE.divide(credits, 8, RoundingMode.HALF_UP);
                })
                .toList();
    }

    private List<BigDecimal> buildOrderScores(List<GradeCourseItem> items, boolean earlyFocus) {
        List<BigDecimal> scores = new ArrayList<>();
        int size = items.size();

        for (int i = 0; i < size; i++) {
            int value = earlyFocus ? size - i : i + 1;
            scores.add(BigDecimal.valueOf(value));
        }

        return scores;
    }

    private List<BigDecimal> buildHalfScores(List<GradeCourseItem> items, boolean firstHalfFocus) {
        List<BigDecimal> scores = new ArrayList<>();
        int size = items.size();
        int midpoint = Math.max(1, (int) Math.ceil(size / 2.0));

        for (int i = 0; i < size; i++) {
            boolean inFirstHalf = i < midpoint;
            BigDecimal base = inFirstHalf == firstHalfFocus ? new BigDecimal("1.85") : new BigDecimal("0.65");
            BigDecimal smallVariation = BigDecimal.valueOf(i + 1)
                    .divide(BigDecimal.valueOf(Math.max(1, size)), 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("0.20"));

            scores.add(base.add(smallVariation));
        }

        return scores;
    }

    private List<BigDecimal> buildTopNCreditScores(List<GradeCourseItem> items, int count) {
        List<GradeCourseItem> topItems = items.stream()
                .sorted((a, b) -> nz(b.getCreditHoursSnapshot()).compareTo(nz(a.getCreditHoursSnapshot())))
                .limit(Math.max(1, count))
                .toList();

        int size = Math.max(1, items.size());
        List<BigDecimal> scores = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            GradeCourseItem item = items.get(i);
            BigDecimal base = topItems.contains(item) ? new BigDecimal("2.25") : new BigDecimal("0.85");
            BigDecimal orderVariation = BigDecimal.valueOf(size - i)
                    .divide(BigDecimal.valueOf(size), 8, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("0.18"));

            scores.add(base.add(orderVariation));
        }

        return scores;
    }

    private List<BigDecimal> buildRotatingScores(List<GradeCourseItem> items, boolean startHigh) {
        List<BigDecimal> scores = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            boolean high = startHigh ? i % 2 == 0 : i % 2 != 0;
            BigDecimal base = high ? new BigDecimal("1.75") : new BigDecimal("0.60");
            BigDecimal creditAdjustment = nz(items.get(i).getCreditHoursSnapshot()).multiply(new BigDecimal("0.08"));
            scores.add(base.add(creditAdjustment));
        }

        return scores;
    }

    private boolean isOptionValid(
            List<WhatIfRequiredCourseDTO> courses,
            BigDecimal requiredAverage,
            BigDecimal scaleMax
    ) {
        if (courses == null || courses.isEmpty()) {
            return false;
        }

        boolean valuesInsideScale = courses.stream()
                .allMatch(course ->
                        nz(course.getRequiredValue()).compareTo(ZERO) >= 0
                                && nz(course.getRequiredValue()).compareTo(scaleMax) <= 0
                );

        if (!valuesInsideScale) {
            return false;
        }

        BigDecimal actualAverage = weightedAverage(courses);

        return actualAverage.add(new BigDecimal("0.06")).compareTo(requiredAverage) >= 0
                && actualAverage.compareTo(scaleMax) <= 0;
    }

    private BigDecimal weightedAverage(List<WhatIfRequiredCourseDTO> courses) {
        if (courses == null || courses.isEmpty()) {
            return ZERO;
        }

        BigDecimal totalCredits = courses.stream()
                .map(course -> nz(course.getCreditHours()))
                .reduce(ZERO, BigDecimal::add);

        if (totalCredits.compareTo(ZERO) <= 0) {
            return ZERO;
        }

        BigDecimal weightedSum = courses.stream()
                .map(course -> nz(course.getRequiredValue()).multiply(nz(course.getCreditHours())))
                .reduce(ZERO, BigDecimal::add);

        return weightedSum.divide(totalCredits, 8, RoundingMode.HALF_UP);
    }

    private BigDecimal optionDelta(BigDecimal scaleMax) {
        if (scaleMax.compareTo(new BigDecimal("10")) > 0) {
            return new BigDecimal("6.00");
        }

        return new BigDecimal("0.32");
    }

    private BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        BigDecimal safeValue = nz(value);

        if (safeValue.compareTo(min) < 0) {
            return min;
        }

        if (safeValue.compareTo(max) > 0) {
            return max;
        }

        return safeValue;
    }

    private Map<Long, BigDecimal> loadStudentDifficultyMap(GradeCalculation calculation) {
        if (calculation == null
                || calculation.getStudent() == null
                || calculation.getStudent().getId() == null) {
            return Map.of();
        }

        return courseRatingRepository.findByStudentId(calculation.getStudent().getId())
                .stream()
                .filter(Objects::nonNull)
                .filter(rating -> rating.getCourse() != null)
                .filter(rating -> rating.getCourse().getId() != null)
                .filter(rating -> rating.getDifficultyRating() != null)
                .collect(Collectors.toMap(
                        rating -> rating.getCourse().getId(),
                        rating -> BigDecimal.valueOf(rating.getDifficultyRating()),
                        (first, second) -> first
                ));
    }

    private BigDecimal difficultyOf(
            GradeCourseItem item,
            Map<Long, BigDecimal> difficultyByCourseId
    ) {
        if (item == null || item.getCourse() == null || item.getCourse().getId() == null) {
            return NEUTRAL_DIFFICULTY;
        }

        if (difficultyByCourseId == null || difficultyByCourseId.isEmpty()) {
            return NEUTRAL_DIFFICULTY;
        }

        BigDecimal difficulty = difficultyByCourseId.get(item.getCourse().getId());

        if (difficulty == null) {
            return NEUTRAL_DIFFICULTY;
        }

        return clamp(difficulty, BigDecimal.ONE, new BigDecimal("5"));
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