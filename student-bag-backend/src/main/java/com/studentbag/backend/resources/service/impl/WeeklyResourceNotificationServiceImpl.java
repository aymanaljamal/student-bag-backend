package com.studentbag.backend.resources.service.impl;

import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.domain.enums.resources.ResourceApprovalStatus;
import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.entity.StudentNotificationPreference;
import com.studentbag.backend.notifications.repository.NotificationRepository;
import com.studentbag.backend.notifications.repository.StudentNotificationPreferenceRepository;
import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.repository.AdminResourceRepository;
import com.studentbag.backend.resources.service.WeeklyResourceNotificationService;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import com.studentbag.backend.schedule.repository.StudentScheduleRepository;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeeklyResourceNotificationServiceImpl implements WeeklyResourceNotificationService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Hebron");

    /*
     * لازم يكون نفس route الموجود في Flutter.
     */
    private static final String PUBLIC_LIBRARY_ROUTE = "/student/resources/public-library";

    private final StudentRepository studentRepository;
    private final StudentScheduleRepository studentScheduleRepository;
    private final AdminResourceRepository adminResourceRepository;
    private final StudentNotificationPreferenceRepository preferenceRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public void dispatchWeeklyResourceNotifications() {
        LocalDate today = LocalDate.now(APP_ZONE);
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDateTime from = weekStart.atStartOfDay();
        LocalDateTime to = from.plusWeeks(1);

        List<Student> students = studentRepository.findAll();

        int sentCount = 0;
        int skippedCount = 0;

        for (Student student : students) {
            if (!canReceiveWeeklyResourceNotification(student)) {
                skippedCount++;
                continue;
            }

            User user = student.getUser();

            if (alreadySentThisWeek(user.getId(), from, to)) {
                skippedCount++;
                continue;
            }

            List<AdminResource> resources = findRelevantApprovedResources(student);

            if (resources.isEmpty()) {
                skippedCount++;
                continue;
            }

            sendWeeklyResourceNotification(student, resources);
            sentCount++;
        }

        log.info(
                "Weekly resource notifications dispatched. sent={}, skipped={}",
                sentCount,
                skippedCount
        );
    }

    private boolean canReceiveWeeklyResourceNotification(Student student) {
        if (student == null || student.getUser() == null || student.getUser().getId() == null) {
            return false;
        }

        StudentNotificationPreference preference = getPreferenceOrDefault(student);

        return Boolean.TRUE.equals(preference.getWeeklyResourceNotificationsEnabled());
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
                                .weeklyResourceNotificationsEnabled(true)
                                .build()
                );
    }

    private boolean alreadySentThisWeek(
            UUID userId,
            LocalDateTime from,
            LocalDateTime to
    ) {
        return notificationRepository.existsForRecipientAndTypeAndTargetValueInWindow(
                userId,
                NotificationType.SYSTEM,
                PUBLIC_LIBRARY_ROUTE,
                from,
                to
        );
    }

    private List<AdminResource> findRelevantApprovedResources(Student student) {
        List<AdminResource> resources = new ArrayList<>();

        List<Long> activeCourseIds = findActiveCourseIds(student.getId());

        if (activeCourseIds.isEmpty()) {
            resources.addAll(
                    adminResourceRepository.findTop30ByApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                            ResourceApprovalStatus.APPROVED
                    )
            );
        } else {
            for (Long courseId : activeCourseIds) {
                resources.addAll(
                        adminResourceRepository.findByCourseIdAndApprovalStatusAndIsVisibleTrueAndIsDeletedFalseOrderByCreatedAtDesc(
                                courseId,
                                ResourceApprovalStatus.APPROVED
                        )
                );
            }
        }

        return distinctAndSortResources(resources);
    }

    private List<Long> findActiveCourseIds(Long studentId) {
        StudentSchedule activeSchedule = studentScheduleRepository
                .findFirstByStudent_IdAndStatusOrderByActivatedAtDescCreatedAtDesc(
                        studentId,
                        ScheduleStatus.ACTIVE
                )
                .orElse(null);

        if (activeSchedule == null || activeSchedule.getEntries() == null) {
            return List.of();
        }

        return activeSchedule.getEntries()
                .stream()
                .filter(entry -> entry.getCourseSection() != null)
                .filter(entry -> entry.getCourseSection().getCourse() != null)
                .map(entry -> entry.getCourseSection().getCourse().getId())
                .filter(id -> id != null)
                .distinct()
                .toList();
    }

    private List<AdminResource> distinctAndSortResources(List<AdminResource> resources) {
        if (resources == null || resources.isEmpty()) {
            return List.of();
        }

        Map<Long, AdminResource> uniqueResources = new LinkedHashMap<>();

        resources.stream()
                .filter(resource -> resource != null && resource.getId() != null)
                .sorted(
                        Comparator.comparing(
                                AdminResource::getCreatedAt,
                                Comparator.nullsLast(LocalDateTime::compareTo)
                        ).reversed()
                )
                .forEach(resource -> uniqueResources.putIfAbsent(resource.getId(), resource));

        return uniqueResources.values()
                .stream()
                .limit(10)
                .toList();
    }

    private void sendWeeklyResourceNotification(
            Student student,
            List<AdminResource> resources
    ) {
        CreateNotificationRequest request = new CreateNotificationRequest();

        request.setTitle("Weekly resources update");
        request.setBody(buildNotificationBody(resources));
        request.setType(NotificationType.SYSTEM);
        request.setPriority(NotificationPriority.NORMAL);
        request.setChannel(NotificationChannel.BOTH);
        request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
        request.setTargetValue(PUBLIC_LIBRARY_ROUTE);
        request.setBroadcastToAll(false);
        request.setRecipientUserIds(List.of(student.getUser().getId()));

        notificationService.createAndSend(request);
    }

    private String buildNotificationBody(List<AdminResource> resources) {
        StringBuilder body = new StringBuilder();

        body.append("New learning resources are available in the public library.");

        body.append("\n\nAvailable resources: ")
                .append(resources.size());

        body.append("\n\nRecent resources:");

        int visibleCount = Math.min(resources.size(), 5);

        for (int i = 0; i < visibleCount; i++) {
            body.append("\n- ")
                    .append(resourceLine(resources.get(i)));
        }

        if (resources.size() > visibleCount) {
            body.append("\n- +")
                    .append(resources.size() - visibleCount)
                    .append(" more resource(s)");
        }

        body.append("\n\nTap to open the public library.");

        return body.toString();
    }

    private String resourceLine(AdminResource resource) {
        String title = resource.getTitle() == null || resource.getTitle().isBlank()
                ? "Resource"
                : resource.getTitle().trim();

        String category = resource.getCategory() != null
                ? resource.getCategory().name()
                : null;

        String type = resource.getResourceType() != null
                ? resource.getResourceType().name()
                : null;

        if (category == null && type == null) {
            return title;
        }

        if (category == null) {
            return title + " • " + type;
        }

        if (type == null) {
            return title + " • " + category;
        }

        return title + " • " + category + " • " + type;
    }
}