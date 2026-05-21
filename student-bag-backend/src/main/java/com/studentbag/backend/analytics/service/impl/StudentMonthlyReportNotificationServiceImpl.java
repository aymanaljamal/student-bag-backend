package com.studentbag.backend.analytics.service.impl;

import com.studentbag.backend.analytics.repository.AnalyticsQueryRepository;
import com.studentbag.backend.analytics.service.StudentMonthlyReportNotificationService;
import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.entity.StudentNotificationPreference;
import com.studentbag.backend.notifications.repository.StudentNotificationPreferenceRepository;
import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentMonthlyReportNotificationServiceImpl
        implements StudentMonthlyReportNotificationService {

    private final StudentRepository studentRepository;
    private final StudentNotificationPreferenceRepository preferenceRepository;
    private final NotificationService notificationService;
    private final AnalyticsQueryRepository analyticsQueryRepository;

    @Override
    @Transactional
    public void dispatchMonthlyReports() {
        List<Student> students = studentRepository.findAll();

        for (Student student : students) {
            if (student.getUser() == null) {
                continue;
            }

            StudentNotificationPreference preference = getPreferenceOrDefault(student);

            if (!Boolean.TRUE.equals(preference.getMonthlyStatsNotificationsEnabled())) {
                continue;
            }

            Long studentId = student.getId();

            String title = buildTitle();
            String body = buildBody(studentId);

            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle(title);
            request.setBody(body);
            request.setType(NotificationType.MONTHLY_STATS);
            request.setPriority(NotificationPriority.NORMAL);
            request.setChannel(NotificationChannel.BOTH);
            request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);

            // عدل هذا الراوت حسب الراوت الموجود عندك بالفرونت
            request.setTargetValue("studentbag://reports/monthly");

            request.setBroadcastToAll(false);
            request.setRecipientUserIds(List.of(student.getUser().getId()));

            notificationService.createAndSend(request);
        }
    }

    private StudentNotificationPreference getPreferenceOrDefault(Student student) {
        return preferenceRepository.findByStudentId(student.getId())
                .orElse(
                        StudentNotificationPreference.builder()
                                .student(student)
                                .eventNotificationsEnabled(true)
                                .taskNotificationsEnabled(true)
                                .recurringTaskNotificationsEnabled(true)
                                .taskReminderOneDayBeforeEnabled(true)
                                .monthlyStatsNotificationsEnabled(false)
                                .build()
                );
    }

    private String buildTitle() {
        return "Your monthly report is ready";
    }

    private String buildBody(Long studentId) {
        Long completedTasks = analyticsQueryRepository.countStudentCompletedTasks(studentId);
        Long activeTasks = analyticsQueryRepository.countStudentActiveTasks(studentId);
        Long notes = analyticsQueryRepository.countStudentNotes(studentId);
        Long registeredEvents = analyticsQueryRepository.countStudentRegisteredEvents(studentId);

        return "This month: "
                + completedTasks + " completed tasks, "
                + activeTasks + " active tasks, "
                + notes + " notes, and "
                + registeredEvents + " registered events. Tap to view your report.";
    }
}