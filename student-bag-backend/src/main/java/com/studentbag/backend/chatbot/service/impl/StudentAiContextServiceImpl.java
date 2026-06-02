package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.analytics.service.StudentDashboardAnalyticsService;
import com.studentbag.backend.chatbot.dto.context.AiFileContentContext;
import com.studentbag.backend.chatbot.dto.context.DashboardAiContext;
import com.studentbag.backend.chatbot.dto.context.EventAiContext;
import com.studentbag.backend.chatbot.dto.context.GradeAiContext;
import com.studentbag.backend.chatbot.dto.context.NoteAiContext;
import com.studentbag.backend.chatbot.dto.context.ResourceAiContext;
import com.studentbag.backend.chatbot.dto.context.ScheduleEntryAiContext;
import com.studentbag.backend.chatbot.dto.context.StudentAiContextDto;
import com.studentbag.backend.chatbot.dto.context.TaskAiContext;
import com.studentbag.backend.chatbot.exception.AiContextBuildException;
import com.studentbag.backend.chatbot.exception.AiStudentNotFoundException;
import com.studentbag.backend.chatbot.file.AiFileContentReaderService;
import com.studentbag.backend.chatbot.mapper.DashboardAiMapper;
import com.studentbag.backend.chatbot.mapper.EventAiMapper;
import com.studentbag.backend.chatbot.mapper.GradeAiMapper;
import com.studentbag.backend.chatbot.mapper.NoteAiMapper;
import com.studentbag.backend.chatbot.mapper.ResourceAiMapper;
import com.studentbag.backend.chatbot.mapper.ScheduleEntryAiMapper;
import com.studentbag.backend.chatbot.mapper.StudentAiProfileMapper;
import com.studentbag.backend.chatbot.mapper.TaskAiMapper;
import com.studentbag.backend.chatbot.service.StudentAiContextService;
import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.events.repository.EventRegistrationRepository;
import com.studentbag.backend.grades.repository.GradeCalculationRepository;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.notes.repository.NoteAttachmentRepository;
import com.studentbag.backend.notes.repository.NoteRepository;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.PersonalResourceItem;
import com.studentbag.backend.resources.repository.AdminResourceRepository;
import com.studentbag.backend.resources.repository.PersonalResourceItemRepository;
import com.studentbag.backend.schedule.repository.ScheduleEntryRepository;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.tasks.entity.TaskAttachment;
import com.studentbag.backend.tasks.repository.TaskAttachmentRepository;
import com.studentbag.backend.tasks.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAiContextServiceImpl implements StudentAiContextService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int RESOURCE_LIMIT = 12;
    private static final int GENERAL_ADMIN_RESOURCE_SCAN_LIMIT = 30;

    private static final int FILE_CONTENT_LIMIT = 6;
    private static final int PERSONAL_RESOURCE_FILE_LIMIT = 2;
    private static final int ADMIN_RESOURCE_FILE_LIMIT = 2;
    private static final int NOTE_ATTACHMENT_FILE_LIMIT = 2;
    private static final int TASK_ATTACHMENT_FILE_LIMIT = 2;
    private static final int FILE_CONTENT_PREVIEW_CHARS = 5_000;

    private final StudentRepository studentRepository;
    private final StudentDashboardAnalyticsService dashboardAnalyticsService;

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final PersonalResourceItemRepository resourceItemRepository;
    private final AdminResourceRepository adminResourceRepository;
    private final NoteAttachmentRepository noteAttachmentRepository;
    private final TaskAttachmentRepository taskAttachmentRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final GradeCalculationRepository gradeCalculationRepository;

    private final AiFileContentReaderService aiFileContentReaderService;

    private final StudentAiProfileMapper studentAiProfileMapper;
    private final DashboardAiMapper dashboardAiMapper;
    private final ScheduleEntryAiMapper scheduleEntryAiMapper;
    private final TaskAiMapper taskAiMapper;
    private final NoteAiMapper noteAiMapper;
    private final ResourceAiMapper resourceAiMapper;
    private final EventAiMapper eventAiMapper;
    private final GradeAiMapper gradeAiMapper;

    @Override
    public StudentAiContextDto buildGeneralContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .dashboard(loadDashboard(studentId))
                .todaySchedule(loadTodaySchedule(studentId))
                .activeTasks(loadActiveTasks(studentId))
                .overdueTasks(loadOverdueTasks(studentId))
                .dueTodayTasks(loadDueTodayTasks(studentId))
                .importantNotes(loadImportantNotes(studentId))
                .resources(loadSmartResources(studentId, null))
                .upcomingEvents(loadUpcomingEvents(studentId))
                .grades(loadLatestGrades(studentId))
                .build();
    }

    @Override
    public StudentAiContextDto buildTodayStudyContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .dashboard(loadDashboard(studentId))
                .todaySchedule(loadTodaySchedule(studentId))
                .upcomingSchedule(loadUpcomingSchedule(studentId))
                .dueTodayTasks(loadDueTodayTasks(studentId))
                .overdueTasks(loadOverdueTasks(studentId))
                .importantNotes(loadImportantNotes(studentId))
                .resources(loadSmartResources(studentId, null))
                .upcomingEvents(loadUpcomingEvents(studentId))
                .grades(loadLatestGrades(studentId))
                .build();
    }

    @Override
    public StudentAiContextDto buildTaskContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .activeTasks(loadActiveTasks(studentId))
                .overdueTasks(loadOverdueTasks(studentId))
                .dueTodayTasks(loadDueTodayTasks(studentId))
                .build();
    }

    @Override
    public StudentAiContextDto buildScheduleContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .todaySchedule(loadTodaySchedule(studentId))
                .upcomingSchedule(loadUpcomingSchedule(studentId))
                .build();
    }

    @Override
    public StudentAiContextDto buildNotesContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .importantNotes(loadImportantNotes(studentId))
                .resources(loadSmartResources(studentId, null))
                .build();
    }

    @Override
    public StudentAiContextDto buildResourcesContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .resources(loadSmartResources(studentId, null))
                .build();
    }

    @Override
    public StudentAiContextDto buildEventsContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .upcomingEvents(loadUpcomingEvents(studentId))
                .build();
    }

    @Override
    public StudentAiContextDto buildGradesContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .grades(loadLatestGrades(studentId))
                .build();
    }

    @Override
    public StudentAiContextDto buildQuizContextFromNote(Long studentId, Long noteId) {
        Student student = findStudent(studentId);

        var note = noteRepository.findByIdAndStudentIdAndIsDeletedFalse(noteId, studentId)
                .orElseThrow(() -> new AiContextBuildException("Note not found or not owned by student."));

        String query = "quiz " + safeText(readField(note, "getTitle"));

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .importantNotes(List.of(noteAiMapper.toContext(note)))
                .resources(loadSmartResources(studentId, query))
                .fileContents(loadRelevantFileContents(studentId, query))
                .build();
    }

    @Override
    public StudentAiContextDto buildContextForQuestion(Long studentId, String question) {
        if (question == null || question.isBlank()) {
            return buildGeneralContext(studentId);
        }

        String q = normalize(question);

        if (containsAny(q, "quiz", "quizzes", "test", "exam", "questions",
                "كويز", "اختبار", "امتحان", "اسئله", "اسئلة", "سؤال")) {
            return buildQuizContextFromQuestion(studentId, question);
        }

        if (containsAny(q, "study today", "study plan", "plan", "revision",
                "شو ادرس", "خطة", "خطه", "ادرس", "اذاكر", "راجع")) {
            return buildTodayStudyContext(studentId);
        }

        if (containsAny(q, "task", "deadline", "assignment", "due",
                "تاسك", "مهمه", "مهمة", "واجب", "موعد", "تسليم")) {

            if (shouldReadFileContent(question)) {
                return buildAttachmentAwareTaskContext(studentId, question);
            }

            return buildTaskContext(studentId);
        }

        if (containsAny(q, "schedule", "timetable", "calendar", "lecture", "class", "room",
                "جدول", "محاضره", "محاضرة", "قاعة", "قاعه", "غرفة", "كلاس")) {
            return buildScheduleContext(studentId);
        }

        if (containsAny(q, "note", "notes", "explain", "summary", "summarize",
                "نوت", "ملاحظه", "ملاحظة", "شرح", "اشرح", "لخص", "تلخيص")) {

            if (shouldReadFileContent(question)) {
                return buildAttachmentAwareNotesContext(studentId, question);
            }

            return buildNotesContext(studentId);
        }

        if (containsAny(q, "resource", "resources", "file", "folder", "pdf", "material",
                "ملف", "مصدر", "رابط", "مرفق", "مادة", "مواد", "مرجع")) {

            if (shouldReadFileContent(question)) {
                return buildAttachmentAwareResourcesContext(studentId, question);
            }

            return buildResourcesContext(studentId);
        }

        if (containsAny(q, "event", "opportunity", "workshop", "training",
                "ايفنت", "فعاليه", "فعالية", "حدث", "فرصه", "فرصة")) {
            return buildEventsContext(studentId);
        }

        if (containsAny(q, "grade", "gpa", "mark", "average",
                "علامه", "علامة", "معدل", "حساب", "نسبة")) {
            return buildGradesContext(studentId);
        }

        if (shouldReadFileContent(question)) {
            return buildAttachmentAwareGeneralContext(studentId, question);
        }

        return buildGeneralContext(studentId);
    }

    private StudentAiContextDto buildQuizContextFromQuestion(Long studentId, String question) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .todaySchedule(loadTodaySchedule(studentId))
                .upcomingSchedule(loadUpcomingSchedule(studentId))
                .importantNotes(loadRelevantNotesForQuestion(studentId, question))
                .resources(loadSmartResources(studentId, question))
                .fileContents(loadRelevantFileContents(studentId, question))
                .build();
    }

    private StudentAiContextDto buildAttachmentAwareGeneralContext(Long studentId, String question) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .dashboard(loadDashboard(studentId))
                .todaySchedule(loadTodaySchedule(studentId))
                .upcomingSchedule(loadUpcomingSchedule(studentId))
                .activeTasks(loadActiveTasks(studentId))
                .overdueTasks(loadOverdueTasks(studentId))
                .dueTodayTasks(loadDueTodayTasks(studentId))
                .importantNotes(loadRelevantNotesForQuestion(studentId, question))
                .resources(loadSmartResources(studentId, question))
                .fileContents(loadRelevantFileContents(studentId, question))
                .upcomingEvents(loadUpcomingEvents(studentId))
                .grades(loadLatestGrades(studentId))
                .build();
    }

    private StudentAiContextDto buildAttachmentAwareNotesContext(Long studentId, String question) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .importantNotes(loadRelevantNotesForQuestion(studentId, question))
                .resources(loadSmartResources(studentId, question))
                .fileContents(loadRelevantFileContents(studentId, question))
                .build();
    }

    private StudentAiContextDto buildAttachmentAwareResourcesContext(Long studentId, String question) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .resources(loadSmartResources(studentId, question))
                .fileContents(loadRelevantFileContents(studentId, question))
                .build();
    }

    private StudentAiContextDto buildAttachmentAwareTaskContext(Long studentId, String question) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .activeTasks(loadActiveTasks(studentId))
                .overdueTasks(loadOverdueTasks(studentId))
                .dueTodayTasks(loadDueTodayTasks(studentId))
                .resources(loadSmartResources(studentId, question))
                .fileContents(loadRelevantFileContents(studentId, question))
                .build();
    }

    private Student findStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new AiStudentNotFoundException(String.valueOf(studentId)));
    }

    private DashboardAiContext loadDashboard(Long studentId) {
        try {
            String email = studentRepository.findById(studentId)
                    .map(student -> student.getUser().getEmail())
                    .orElseThrow(() -> new AiStudentNotFoundException(String.valueOf(studentId)));

            var dashboard = dashboardAnalyticsService.getMyDashboardAnalyticsByEmail(email);
            return dashboardAiMapper.toContext(dashboard);
        } catch (Exception ex) {
            return null;
        }
    }

    private List<ScheduleEntryAiContext> loadTodaySchedule(Long studentId) {
        LocalDate today = LocalDate.now();

        return scheduleEntryRepository
                .findActiveScheduleEntriesBetween(
                        studentId,
                        today.atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                )
                .stream()
                .map(scheduleEntryAiMapper::toContext)
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private List<ScheduleEntryAiContext> loadUpcomingSchedule(Long studentId) {
        LocalDateTime now = LocalDateTime.now();

        return scheduleEntryRepository
                .findActiveUpcomingEntries(studentId, now, now.plusDays(7))
                .stream()
                .map(scheduleEntryAiMapper::toContext)
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private List<TaskAiContext> loadActiveTasks(Long studentId) {
        return taskRepository
                .findTop10ByStudentIdAndStatusNotAndArchivedFalseAndIsDeletedFalseOrderByDueDateTimeAsc(
                        studentId,
                        TaskStatus.COMPLETED
                )
                .stream()
                .map(taskAiMapper::toContext)
                .toList();
    }

    private List<TaskAiContext> loadOverdueTasks(Long studentId) {
        return taskRepository
                .findOverdueTasksForAi(studentId, LocalDateTime.now())
                .stream()
                .map(taskAiMapper::toContext)
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private List<TaskAiContext> loadDueTodayTasks(Long studentId) {
        LocalDate today = LocalDate.now();

        return taskRepository
                .findTasksDueBetweenForAi(
                        studentId,
                        today.atStartOfDay(),
                        today.atTime(LocalTime.MAX)
                )
                .stream()
                .map(taskAiMapper::toContext)
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private List<NoteAiContext> loadImportantNotes(Long studentId) {
        return noteRepository
                .findTop10ByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByIsPinnedDescIsImportantDescUpdatedAtDesc(studentId)
                .stream()
                .map(noteAiMapper::toContext)
                .toList();
    }

    private List<NoteAiContext> loadRelevantNotesForQuestion(Long studentId, String question) {
        List<NoteAiContext> notes = loadImportantNotes(studentId);

        if (question == null || question.isBlank()) {
            return notes;
        }

        String q = normalize(question);

        List<NoteAiContext> matched = notes.stream()
                .filter(note -> matchesQuestion(q, note.getTitle(), note.getCourseName()))
                .limit(DEFAULT_LIMIT)
                .toList();

        return matched.isEmpty() ? notes : matched;
    }

    private List<ResourceAiContext> loadSmartResources(Long studentId, String question) {
        List<ResourceAiContext> result = new ArrayList<>();

        List<PersonalResourceItem> personalItems = resourceItemRepository
                .findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(studentId);

        List<PersonalResourceItem> matchedPersonalItems = filterPersonalResources(personalItems, question);

        for (PersonalResourceItem item : matchedPersonalItems) {
            result.add(resourceAiMapper.toContext(item));

            if (result.size() >= RESOURCE_LIMIT) {
                return distinctResources(result).stream()
                        .limit(RESOURCE_LIMIT)
                        .toList();
            }
        }

        List<AdminResource> adminResources = loadRelevantAdminResources(question, personalItems);

        for (AdminResource adminResource : adminResources) {
            result.add(toAdminResourceContext(adminResource));

            if (result.size() >= RESOURCE_LIMIT) {
                return distinctResources(result).stream()
                        .limit(RESOURCE_LIMIT)
                        .toList();
            }
        }

        return distinctResources(result).stream()
                .limit(RESOURCE_LIMIT)
                .toList();
    }

    private List<PersonalResourceItem> filterPersonalResources(
            List<PersonalResourceItem> items,
            String question
    ) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        if (question == null || question.isBlank()) {
            return items.stream()
                    .limit(DEFAULT_LIMIT)
                    .toList();
        }

        String q = normalize(question);

        List<PersonalResourceItem> matched = items.stream()
                .filter(item -> matchesQuestion(
                        q,
                        item.getTitle(),
                        item.getDescription(),
                        item.getFileName(),
                        item.getCategory() != null ? item.getCategory().name() : null,
                        item.getResourceType() != null ? item.getResourceType().name() : null,
                        readCourseField(item.getCourse(), "getCode"),
                        readCourseField(item.getCourse(), "getCourseCode"),
                        readCourseField(item.getCourse(), "getName"),
                        readCourseField(item.getCourse(), "getCourseName"),
                        readCourseField(item.getCourse(), "getNameAr"),
                        readCourseField(item.getCourse(), "getArabicName")
                ))
                .limit(DEFAULT_LIMIT)
                .toList();

        if (!matched.isEmpty()) {
            return matched;
        }

        return items.stream()
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private List<AdminResource> loadRelevantAdminResources(
            String question,
            List<PersonalResourceItem> personalItems
    ) {
        List<AdminResource> approvedVisibleResources = adminResourceRepository
                .findTop30ByApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                        ResourceApprovalStatus.APPROVED
                );

        if (approvedVisibleResources.isEmpty()) {
            return List.of();
        }

        String q = normalize(question);

        List<Long> knownCourseIds = extractCourseIdsFromPersonalItems(personalItems);

        List<AdminResource> matchedByQuestion = approvedVisibleResources.stream()
                .filter(resource -> matchesAdminResource(q, resource))
                .limit(DEFAULT_LIMIT)
                .toList();

        if (!matchedByQuestion.isEmpty()) {
            return matchedByQuestion;
        }

        List<AdminResource> matchedByStudentCourses = approvedVisibleResources.stream()
                .filter(resource -> resource.getCourse() != null)
                .filter(resource -> knownCourseIds.contains(resource.getCourse().getId()))
                .limit(DEFAULT_LIMIT)
                .toList();

        if (!matchedByStudentCourses.isEmpty()) {
            return matchedByStudentCourses;
        }

        return approvedVisibleResources.stream()
                .limit(Math.min(DEFAULT_LIMIT, GENERAL_ADMIN_RESOURCE_SCAN_LIMIT))
                .toList();
    }

    private boolean matchesAdminResource(String normalizedQuestion, AdminResource resource) {
        if (resource == null) {
            return false;
        }

        return matchesQuestion(
                normalizedQuestion,
                resource.getTitle(),
                resource.getDescription(),
                resource.getFileName(),
                resource.getCategory() != null ? resource.getCategory().name() : null,
                resource.getResourceType() != null ? resource.getResourceType().name() : null,
                resource.getLearningObject() != null ? resource.getLearningObject().getTitle() : null,
                resource.getLearningObject() != null ? resource.getLearningObject().getDescription() : null,
                readCourseField(resource.getCourse(), "getCode"),
                readCourseField(resource.getCourse(), "getCourseCode"),
                readCourseField(resource.getCourse(), "getName"),
                readCourseField(resource.getCourse(), "getCourseName"),
                readCourseField(resource.getCourse(), "getNameAr"),
                readCourseField(resource.getCourse(), "getArabicName")
        );
    }

    private List<AiFileContentContext> loadRelevantFileContents(
            Long studentId,
            String question
    ) {
        if (!shouldReadFileContent(question)) {
            return List.of();
        }

        List<AiFileContentContext> result = new ArrayList<>();

        result.addAll(loadPersonalResourceFileContents(studentId, question));

        if (result.size() < FILE_CONTENT_LIMIT) {
            result.addAll(loadAdminResourceFileContents(
                    studentId,
                    question,
                    FILE_CONTENT_LIMIT - result.size()
            ));
        }

        if (result.size() < FILE_CONTENT_LIMIT) {
            result.addAll(loadNoteAttachmentContents(
                    studentId,
                    question,
                    FILE_CONTENT_LIMIT - result.size()
            ));
        }

        if (result.size() < FILE_CONTENT_LIMIT) {
            result.addAll(loadTaskAttachmentContents(
                    studentId,
                    question,
                    FILE_CONTENT_LIMIT - result.size()
            ));
        }

        return result.stream()
                .filter(file -> file.getContentPreview() != null && !file.getContentPreview().isBlank())
                .limit(FILE_CONTENT_LIMIT)
                .toList();
    }

    private List<AiFileContentContext> loadPersonalResourceFileContents(
            Long studentId,
            String question
    ) {
        List<PersonalResourceItem> personalItems = resourceItemRepository
                .findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(studentId);

        if (personalItems == null || personalItems.isEmpty()) {
            return List.of();
        }

        String q = normalize(question);

        List<PersonalResourceItem> matchedItems = personalItems.stream()
                .filter(item -> hasReadableFile(item.getFileUrl()))
                .filter(item -> matchesQuestion(
                        q,
                        item.getTitle(),
                        item.getDescription(),
                        item.getFileName(),
                        item.getCategory() != null ? item.getCategory().name() : null,
                        item.getResourceType() != null ? item.getResourceType().name() : null,
                        readCourseField(item.getCourse(), "getCode"),
                        readCourseField(item.getCourse(), "getCourseCode"),
                        readCourseField(item.getCourse(), "getName"),
                        readCourseField(item.getCourse(), "getCourseName"),
                        readCourseField(item.getCourse(), "getNameAr"),
                        readCourseField(item.getCourse(), "getArabicName")
                ))
                .limit(PERSONAL_RESOURCE_FILE_LIMIT)
                .toList();

        if (matchedItems.isEmpty()) {
            matchedItems = personalItems.stream()
                    .filter(item -> hasReadableFile(item.getFileUrl()))
                    .limit(PERSONAL_RESOURCE_FILE_LIMIT)
                    .toList();
        }

        return matchedItems.stream()
                .map(item -> buildFileContentContext(
                        "RESOURCE",
                        item.getId(),
                        item.getTitle(),
                        item.getFileName(),
                        item.getMimeType(),
                        item.getFileSizeBytes(),
                        item.getFileUrl()
                ))
                .filter(file -> file.getContentPreview() != null && !file.getContentPreview().isBlank())
                .toList();
    }

    private List<AiFileContentContext> loadAdminResourceFileContents(
            Long studentId,
            String question,
            int limit
    ) {
        if (limit <= 0) {
            return List.of();
        }

        List<PersonalResourceItem> personalItems = resourceItemRepository
                .findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(studentId);

        List<AdminResource> adminResources = loadRelevantAdminResources(question, personalItems);

        if (adminResources == null || adminResources.isEmpty()) {
            return List.of();
        }

        return adminResources.stream()
                .filter(resource -> hasReadableFile(resource.getFileUrl()))
                .limit(Math.min(limit, ADMIN_RESOURCE_FILE_LIMIT))
                .map(resource -> buildFileContentContext(
                        "RESOURCE_HUB",
                        resource.getId(),
                        resource.getTitle(),
                        resource.getFileName(),
                        resource.getMimeType(),
                        resource.getFileSizeBytes(),
                        resource.getFileUrl()
                ))
                .filter(file -> file.getContentPreview() != null && !file.getContentPreview().isBlank())
                .toList();
    }

    private List<AiFileContentContext> loadNoteAttachmentContents(
            Long studentId,
            String question,
            int limit
    ) {
        if (limit <= 0) {
            return List.of();
        }

        List<NoteAttachment> attachments = noteAttachmentRepository.findByStudentIdForAi(studentId);

        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        String q = normalize(question);

        List<NoteAttachment> matchedAttachments = attachments.stream()
                .filter(attachment -> hasReadableFile(attachment.getUrlValue()))
                .filter(attachment -> matchesQuestion(
                        q,
                        attachment.getFileName(),
                        attachment.getTypeValue(),
                        readField(attachment.getNote(), "getTitle"),
                        readField(attachment.getNote(), "getContentHtml"),
                        readField(readObjectField(attachment.getNote(), "getCourse"), "getCode"),
                        readField(readObjectField(attachment.getNote(), "getCourse"), "getCourseCode"),
                        readField(readObjectField(attachment.getNote(), "getCourse"), "getName"),
                        readField(readObjectField(attachment.getNote(), "getCourse"), "getCourseName"),
                        readField(readObjectField(attachment.getNote(), "getCourse"), "getNameAr"),
                        readField(readObjectField(attachment.getNote(), "getCourse"), "getArabicName")
                ))
                .limit(Math.min(limit, NOTE_ATTACHMENT_FILE_LIMIT))
                .toList();

        if (matchedAttachments.isEmpty()) {
            matchedAttachments = attachments.stream()
                    .filter(attachment -> hasReadableFile(attachment.getUrlValue()))
                    .limit(Math.min(limit, NOTE_ATTACHMENT_FILE_LIMIT))
                    .toList();
        }

        return matchedAttachments.stream()
                .map(attachment -> buildFileContentContext(
                        "NOTE_ATTACHMENT",
                        attachment.getId(),
                        buildAttachmentTitle(
                                readField(attachment.getNote(), "getTitle"),
                                attachment.getFileName()
                        ),
                        attachment.getFileName(),
                        attachment.getTypeValue(),
                        attachment.getFileSizeBytes(),
                        attachment.getUrlValue()
                ))
                .filter(file -> file.getContentPreview() != null && !file.getContentPreview().isBlank())
                .toList();
    }

    private List<AiFileContentContext> loadTaskAttachmentContents(
            Long studentId,
            String question,
            int limit
    ) {
        if (limit <= 0) {
            return List.of();
        }

        List<TaskAttachment> attachments = taskAttachmentRepository.findByStudentIdForAi(studentId);

        if (attachments == null || attachments.isEmpty()) {
            return List.of();
        }

        String q = normalize(question);

        List<TaskAttachment> matchedAttachments = attachments.stream()
                .filter(attachment -> hasReadableFile(attachment.getUrl()))
                .filter(attachment -> matchesQuestion(
                        q,
                        attachment.getFileName(),
                        attachment.getMimeType(),
                        attachment.getExtension(),
                        attachment.getKind() != null ? attachment.getKind().name() : null,
                        readField(attachment.getTask(), "getTitle"),
                        readField(attachment.getTask(), "getDescription"),
                        readField(readObjectField(attachment.getTask(), "getCourse"), "getCode"),
                        readField(readObjectField(attachment.getTask(), "getCourse"), "getCourseCode"),
                        readField(readObjectField(attachment.getTask(), "getCourse"), "getName"),
                        readField(readObjectField(attachment.getTask(), "getCourse"), "getCourseName"),
                        readField(readObjectField(attachment.getTask(), "getCourse"), "getNameAr"),
                        readField(readObjectField(attachment.getTask(), "getCourse"), "getArabicName")
                ))
                .limit(Math.min(limit, TASK_ATTACHMENT_FILE_LIMIT))
                .toList();

        if (matchedAttachments.isEmpty()) {
            matchedAttachments = attachments.stream()
                    .filter(attachment -> hasReadableFile(attachment.getUrl()))
                    .limit(Math.min(limit, TASK_ATTACHMENT_FILE_LIMIT))
                    .toList();
        }

        return matchedAttachments.stream()
                .map(attachment -> buildFileContentContext(
                        "TASK_ATTACHMENT",
                        attachment.getId(),
                        buildAttachmentTitle(
                                readField(attachment.getTask(), "getTitle"),
                                attachment.getFileName()
                        ),
                        attachment.getFileName(),
                        attachment.getMimeType(),
                        attachment.getFileSizeBytes(),
                        attachment.getUrl()
                ))
                .filter(file -> file.getContentPreview() != null && !file.getContentPreview().isBlank())
                .toList();
    }

    private AiFileContentContext buildFileContentContext(
            String ownerType,
            Long ownerId,
            String title,
            String fileName,
            String mimeType,
            Long fileSizeBytes,
            String fileUrl
    ) {
        String contentPreview = aiFileContentReaderService.readFileContentPreview(
                fileUrl,
                fileName,
                mimeType,
                fileSizeBytes,
                FILE_CONTENT_PREVIEW_CHARS
        );

        return AiFileContentContext.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .title(title)
                .fileName(fileName)
                .mimeType(mimeType)
                .fileSizeBytes(fileSizeBytes)
                .contentPreview(contentPreview)
                .build();
    }

    private boolean shouldReadFileContent(String question) {
        if (question == null || question.isBlank()) {
            return false;
        }

        String q = normalize(question);

        return containsAny(
                q,
                "quiz",
                "quizzes",
                "test",
                "exam",
                "questions",
                "summary",
                "summarize",
                "explain",
                "read",
                "file",
                "pdf",
                "document",
                "attachment",
                "material",
                "content",
                "كويز",
                "اختبار",
                "امتحان",
                "اسئله",
                "اسئلة",
                "لخص",
                "تلخيص",
                "اشرح",
                "شرح",
                "اقرا",
                "اقرأ",
                "ملف",
                "مرفق",
                "اتاشمنت",
                "بي دي اف",
                "محتوى",
                "محتوي"
        );
    }

    private boolean hasReadableFile(String fileUrl) {
        return fileUrl != null && !fileUrl.isBlank();
    }

    private String buildAttachmentTitle(String ownerTitle, String fileName) {
        String cleanOwnerTitle = safeText(ownerTitle);
        String cleanFileName = safeText(fileName);

        if (!cleanOwnerTitle.isBlank() && !cleanFileName.isBlank()) {
            return cleanOwnerTitle + " | " + cleanFileName;
        }

        if (!cleanOwnerTitle.isBlank()) {
            return cleanOwnerTitle;
        }

        return cleanFileName;
    }

    private List<Long> extractCourseIdsFromPersonalItems(List<PersonalResourceItem> items) {
        if (items == null || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .filter(item -> item.getCourse() != null)
                .map(item -> item.getCourse().getId())
                .distinct()
                .toList();
    }

    private ResourceAiContext toAdminResourceContext(AdminResource resource) {
        return ResourceAiContext.builder()
                .title(buildAdminResourceTitle(resource))
                .category(resource.getCategory() != null ? resource.getCategory().name() : null)
                .build();
    }

    private String buildAdminResourceTitle(AdminResource resource) {
        String title = safeText(resource.getTitle());

        String courseCode = readCourseField(resource.getCourse(), "getCode");
        if (courseCode == null || courseCode.isBlank()) {
            courseCode = readCourseField(resource.getCourse(), "getCourseCode");
        }

        if (courseCode != null && !courseCode.isBlank()) {
            return title + " | Resource Hub | " + courseCode;
        }

        return title + " | Resource Hub";
    }

    private List<ResourceAiContext> distinctResources(List<ResourceAiContext> resources) {
        Map<String, ResourceAiContext> map = new LinkedHashMap<>();

        for (ResourceAiContext resource : resources) {
            if (resource == null || resource.getTitle() == null) {
                continue;
            }

            map.putIfAbsent(normalize(resource.getTitle()), resource);
        }

        return new ArrayList<>(map.values());
    }

    private List<EventAiContext> loadUpcomingEvents(Long studentId) {
        return eventRegistrationRepository
                .findUpcomingRegisteredEventsForAi(studentId, LocalDateTime.now())
                .stream()
                .map(registration -> eventAiMapper.toContext(registration.getEvent()))
                .limit(DEFAULT_LIMIT)
                .toList();
    }

    private GradeAiContext loadLatestGrades(Long studentId) {
        return gradeCalculationRepository
                .findTopByStudentIdOrderByUpdatedAtDesc(studentId)
                .map(gradeAiMapper::toContext)
                .orElse(null);
    }

    private boolean containsAny(String text, String... keywords) {
        String normalizedText = normalize(text);

        for (String keyword : keywords) {
            if (normalizedText.contains(normalize(keyword))) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesQuestion(String normalizedQuestion, String... values) {
        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return false;
        }

        for (String value : values) {
            String normalizedValue = normalize(value);

            if (normalizedValue.isBlank()) {
                continue;
            }

            if (normalizedQuestion.contains(normalizedValue) ||
                    normalizedValue.contains(normalizedQuestion) ||
                    hasSharedUsefulToken(normalizedQuestion, normalizedValue)) {
                return true;
            }
        }

        return false;
    }

    private boolean hasSharedUsefulToken(String left, String right) {
        String[] leftTokens = left.split("\\s+");
        String[] rightTokens = right.split("\\s+");

        for (String leftToken : leftTokens) {
            if (leftToken.length() < 3 || isIgnoredToken(leftToken)) {
                continue;
            }

            for (String rightToken : rightTokens) {
                if (rightToken.length() < 3 || isIgnoredToken(rightToken)) {
                    continue;
                }

                if (leftToken.equals(rightToken)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isIgnoredToken(String token) {
        return switch (token) {
            case "quiz", "quizzes", "test", "exam", "questions",
                 "summary", "summarize", "explain", "read",
                 "كويز", "اختبار", "امتحان", "اسئله", "اسئلة",
                 "اعمل", "بدي", "اريد", "ابني", "ولد", "لخص",
                 "تلخيص", "اشرح", "اقرا", "اقرأ",
                 "generate", "make", "create", "from", "for", "the",
                 "من", "عن", "على", "في" -> true;
            default -> false;
        };
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('أ', 'ا')
                .replace('إ', 'ا')
                .replace('آ', 'ا')
                .replace('ى', 'ي')
                .replace('ة', 'ه')
                .replace('ؤ', 'و')
                .replace('ئ', 'ي')
                .replaceAll("[\\u064B-\\u065F\\u0670]", "")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    private String readCourseField(Object course, String getterName) {
        return readField(course, getterName);
    }

    private String readField(Object source, String getterName) {
        Object value = readObjectField(source, getterName);
        return value == null ? null : String.valueOf(value);
    }

    private Object readObjectField(Object source, String getterName) {
        if (source == null || getterName == null || getterName.isBlank()) {
            return null;
        }

        try {
            Method method = source.getClass().getMethod(getterName);
            return method.invoke(source);
        } catch (Exception ignored) {
            return null;
        }
    }
}