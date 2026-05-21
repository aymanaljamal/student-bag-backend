package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.analytics.service.StudentDashboardAnalyticsService;
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
import com.studentbag.backend.chatbot.mapper.DashboardAiMapper;
import com.studentbag.backend.chatbot.mapper.EventAiMapper;
import com.studentbag.backend.chatbot.mapper.GradeAiMapper;
import com.studentbag.backend.chatbot.mapper.NoteAiMapper;
import com.studentbag.backend.chatbot.mapper.ResourceAiMapper;
import com.studentbag.backend.chatbot.mapper.ScheduleEntryAiMapper;
import com.studentbag.backend.chatbot.mapper.StudentAiProfileMapper;
import com.studentbag.backend.chatbot.mapper.TaskAiMapper;
import com.studentbag.backend.chatbot.service.StudentAiContextService;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.events.repository.EventRegistrationRepository;
import com.studentbag.backend.grades.repository.GradeCalculationRepository;
import com.studentbag.backend.notes.repository.NoteRepository;
import com.studentbag.backend.resources.repository.PersonalResourceItemRepository;
import com.studentbag.backend.schedule.repository.ScheduleEntryRepository;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.tasks.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentAiContextServiceImpl implements StudentAiContextService {

    private static final int DEFAULT_LIMIT = 10;

    private final StudentRepository studentRepository;
    private final StudentDashboardAnalyticsService dashboardAnalyticsService;

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final TaskRepository taskRepository;
    private final NoteRepository noteRepository;
    private final PersonalResourceItemRepository resourceItemRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final GradeCalculationRepository gradeCalculationRepository;

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
                .resources(loadRecentResources(studentId))
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
                .build();
    }

    @Override
    public StudentAiContextDto buildResourcesContext(Long studentId) {
        Student student = findStudent(studentId);

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .resources(loadRecentResources(studentId))
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

        return StudentAiContextDto.builder()
                .student(studentAiProfileMapper.toContext(student))
                .importantNotes(List.of(noteAiMapper.toContext(note)))
                .build();
    }

    @Override
    public StudentAiContextDto buildContextForQuestion(Long studentId, String question) {
        if (question == null || question.isBlank()) {
            return buildGeneralContext(studentId);
        }

        String q = question.toLowerCase();

        if (containsAny(q, "quiz", "كويز", "اختبار", "اسئلة", "أسئلة")) {
            return buildNotesContext(studentId);
        }

        if (containsAny(q, "شو ادرس", "شو أدرس", "study today", "خطة", "ادرس", "أدرس")) {
            return buildTodayStudyContext(studentId);
        }

        if (containsAny(q, "task", "deadline", "تاسك", "مهمة", "واجب", "موعد")) {
            return buildTaskContext(studentId);
        }

        if (containsAny(q, "schedule", "جدول", "محاضرة", "قاعة", "غرفة", "كلاس")) {
            return buildScheduleContext(studentId);
        }

        if (containsAny(q, "note", "نوت", "ملاحظة", "شرح", "لخص", "تلخيص")) {
            return buildNotesContext(studentId);
        }

        if (containsAny(q, "resource", "file", "folder", "ملف", "مصدر", "رابط", "مرفق")) {
            return buildResourcesContext(studentId);
        }

        if (containsAny(q, "event", "ايفنت", "فعالية", "حدث", "opportunity", "فرصة")) {
            return buildEventsContext(studentId);
        }

        if (containsAny(q, "grade", "gpa", "علامة", "معدل", "حساب")) {
            return buildGradesContext(studentId);
        }

        return buildGeneralContext(studentId);
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

    private List<ResourceAiContext> loadRecentResources(Long studentId) {
        return resourceItemRepository
                .findTop10ByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByUpdatedAtDesc(studentId)
                .stream()
                .map(resourceAiMapper::toContext)
                .toList();
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
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}