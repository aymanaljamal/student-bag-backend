package com.studentbag.backend.notifications.service.impl;

import com.studentbag.backend.notifications.dto.request.UpdateStudentNotificationPreferenceRequest;
import com.studentbag.backend.notifications.dto.response.StudentNotificationPreferenceResponse;
import com.studentbag.backend.notifications.entity.StudentNotificationPreference;
import com.studentbag.backend.notifications.repository.StudentNotificationPreferenceRepository;
import com.studentbag.backend.notifications.service.StudentNotificationPreferenceService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StudentNotificationPreferenceServiceImpl
        implements StudentNotificationPreferenceService {

    private final StudentNotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Override
    public StudentNotificationPreferenceResponse getMyPreferences(UUID userId) {
        StudentNotificationPreference preference = getOrCreatePreference(userId);
        return mapToResponse(preference);
    }

    @Override
    public StudentNotificationPreferenceResponse updateMyPreferences(
            UUID userId,
            UpdateStudentNotificationPreferenceRequest request
    ) {
        StudentNotificationPreference preference = getOrCreatePreference(userId);

        preference.setEventNotificationsEnabled(
                safeBool(request.getEventNotificationsEnabled(), true)
        );
        preference.setTaskNotificationsEnabled(
                safeBool(request.getTaskNotificationsEnabled(), true)
        );
        preference.setRecurringTaskNotificationsEnabled(
                safeBool(request.getRecurringTaskNotificationsEnabled(), true)
        );
        preference.setTaskReminderOneDayBeforeEnabled(
                safeBool(request.getTaskReminderOneDayBeforeEnabled(), true)
        );
        preference.setMonthlyStatsNotificationsEnabled(
                safeBool(request.getMonthlyStatsNotificationsEnabled(), false)
        );
        preference.setWeeklyResourceNotificationsEnabled(
                safeBool(request.getWeeklyResourceNotificationsEnabled(), true)
        );

        StudentNotificationPreference saved = preferenceRepository.save(preference);
        return mapToResponse(saved);
    }

    private StudentNotificationPreference getOrCreatePreference(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Student not found"));

        return preferenceRepository.findByStudentId(student.getId())
                .orElseGet(() -> {
                    StudentNotificationPreference newPreference =
                            StudentNotificationPreference.builder()
                                    .student(student)
                                    .eventNotificationsEnabled(true)
                                    .taskNotificationsEnabled(true)
                                    .recurringTaskNotificationsEnabled(true)
                                    .taskReminderOneDayBeforeEnabled(true)
                                    .monthlyStatsNotificationsEnabled(false)
                                    .weeklyResourceNotificationsEnabled(true)
                                    .build();

                    return preferenceRepository.save(newPreference);
                });
    }

    private StudentNotificationPreferenceResponse mapToResponse(
            StudentNotificationPreference preference
    ) {
        return StudentNotificationPreferenceResponse.builder()
                .eventNotificationsEnabled(
                        safeBool(preference.getEventNotificationsEnabled(), true)
                )
                .taskNotificationsEnabled(
                        safeBool(preference.getTaskNotificationsEnabled(), true)
                )
                .recurringTaskNotificationsEnabled(
                        safeBool(preference.getRecurringTaskNotificationsEnabled(), true)
                )
                .taskReminderOneDayBeforeEnabled(
                        safeBool(preference.getTaskReminderOneDayBeforeEnabled(), true)
                )
                .monthlyStatsNotificationsEnabled(
                        safeBool(preference.getMonthlyStatsNotificationsEnabled(), false)
                )
                .weeklyResourceNotificationsEnabled(
                        safeBool(preference.getWeeklyResourceNotificationsEnabled(), true)
                )
                .build();
    }

    private Boolean safeBool(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }
}