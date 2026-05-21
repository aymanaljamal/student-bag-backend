package com.studentbag.backend.notifications.service.impl;

import com.studentbag.backend.domain.enums.*;
import com.studentbag.backend.domain.enums.notifications.*;
import com.studentbag.backend.domain.enums.tasks.TaskRecurrenceType;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.events.repository.EventRepository;
import com.studentbag.backend.notifications.dto.request.CreateAdminNotificationRequest;
import com.studentbag.backend.notifications.dto.request.CreateNotificationRequest;
import com.studentbag.backend.notifications.dto.request.NotificationAttachmentRequest;
import com.studentbag.backend.notifications.dto.response.DeleteNotificationsResponse;
import com.studentbag.backend.notifications.dto.response.NotificationResponse;
import com.studentbag.backend.notifications.entity.Notification;
import com.studentbag.backend.notifications.entity.NotificationAttachment;
import com.studentbag.backend.notifications.entity.StudentNotificationPreference;
import com.studentbag.backend.notifications.entity.UserNotification;
import com.studentbag.backend.notifications.exception.InvalidNotificationRequestException;
import com.studentbag.backend.notifications.exception.NotificationNotFoundException;
import com.studentbag.backend.notifications.mapper.NotificationMapper;
import com.studentbag.backend.notifications.repository.NotificationRepository;
import com.studentbag.backend.notifications.repository.StudentNotificationPreferenceRepository;
import com.studentbag.backend.notifications.repository.UserDeviceTokenRepository;
import com.studentbag.backend.notifications.repository.UserNotificationRepository;
import com.studentbag.backend.notifications.service.FirebasePushNotificationService;
import com.studentbag.backend.notifications.service.NotificationService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.tasks.entity.Task;
import com.studentbag.backend.tasks.repository.TaskRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TaskRepository taskRepository;
    private final EventRepository eventRepository;
    private final StudentNotificationPreferenceRepository preferenceRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final NotificationMapper notificationMapper;
    private final FirebasePushNotificationService firebasePushNotificationService;

    @Override
    @Transactional
    public NotificationResponse createAndSend(CreateNotificationRequest request) {
        validateRequest(request);

        Notification notification = Notification.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .type(request.getType())
                .priority(request.getPriority())
                .channel(request.getChannel())
                .targetType(request.getTargetType())
                .targetValue(request.getTargetValue())
                .imageUrl(request.getImageUrl())
                .iconUrl(request.getIconUrl())
                .broadcastToAll(request.isBroadcastToAll())
                .scheduledAt(request.getScheduledAt())
                .expiresAt(request.getExpiresAt())
                .active(true)
                .build();

        if (request.getAttachments() != null) {
            for (NotificationAttachmentRequest attachmentRequest : request.getAttachments()) {
                NotificationAttachment attachment = NotificationAttachment.builder()
                        .notification(notification)
                        .type(attachmentRequest.getType())
                        .url(attachmentRequest.getUrl())
                        .fileName(attachmentRequest.getFileName())
                        .build();

                notification.getAttachments().add(attachment);
            }
        }

        notificationRepository.save(notification);

        List<User> recipients = resolveRecipients(request);

        if (recipients.isEmpty()) {
            throw new InvalidNotificationRequestException("No recipients found for this notification");
        }

        List<UserNotification> createdUserNotifications = new ArrayList<>();

        for (User user : recipients) {
            UserNotification userNotification = UserNotification.builder()
                    .notification(notification)
                    .user(user)
                    .status(UserNotificationStatus.SENT)
                    .readFlag(false)
                    .build();

            createdUserNotifications.add(userNotification);
        }

        userNotificationRepository.saveAll(createdUserNotifications);

        for (User user : recipients) {
            List<String> tokens = userDeviceTokenRepository.findActiveTokensByUserId(user.getId());

            firebasePushNotificationService.sendToTokens(
                    tokens,
                    notification.getTitle(),
                    notification.getBody(),
                    notification.getTargetValue(),
                    notification.getType().name()
            );
        }

        return notificationMapper.toResponse(createdUserNotifications.get(0));
    }
    @Override
    @Transactional
    public NotificationResponse createAndSendAdminNotification(
            UUID senderUserId,
            CreateAdminNotificationRequest request
    ) {
        User sender = userRepository.findById(senderUserId)
                .orElseThrow(() -> new IllegalArgumentException("Sender user not found"));

        if (sender.getRole() != UserRole.ADMINISTRATOR
                && sender.getRole() != UserRole.INSTRUCTOR) {
            throw new IllegalArgumentException(
                    "Only administrator or instructor can send this notification"
            );
        }

        CreateNotificationRequest internalRequest = new CreateNotificationRequest();
        internalRequest.setTitle(request.getTitle());
        internalRequest.setBody(request.getBody());
        internalRequest.setType(request.getType());
        internalRequest.setPriority(request.getPriority());
        internalRequest.setChannel(request.getChannel());
        internalRequest.setTargetType(request.getTargetType());
        internalRequest.setTargetValue(request.getTargetValue());
        internalRequest.setImageUrl(request.getImageUrl());
        internalRequest.setIconUrl(request.getIconUrl());
        internalRequest.setBroadcastToAll(false);
        internalRequest.setRecipientUserIds(resolveAdminRecipients(request));

        return createAndSend(internalRequest);
    }

    @Override
    public List<NotificationResponse> getMyNotifications(UUID userId, int page, int size) {
        return userNotificationRepository.findActiveByUserId(
                        userId,
                        LocalDateTime.now(),
                        PageRequest.of(page, size)
                )
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return userNotificationRepository.countUnreadByUserId(userId, LocalDateTime.now());
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID userId, UUID userNotificationId) {
        UserNotification userNotification = userNotificationRepository
                .findByIdAndUserId(userNotificationId, userId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        if (!userNotification.isReadFlag()) {
            userNotification.setReadFlag(true);
            userNotification.setStatus(UserNotificationStatus.READ);
            userNotification.setReadAt(LocalDateTime.now());
            userNotificationRepository.save(userNotification);
        }

        return notificationMapper.toResponse(userNotification);
    }

    @Override
    @Transactional
    public int markAllAsRead(UUID userId) {
        return userNotificationRepository.markAllAsRead(
                userId,
                UserNotificationStatus.READ,
                LocalDateTime.now()
        );
    }

    @Override
    @Transactional
    public void dispatchTaskReminderNotifications() {
        // Disabled intentionally.
        // One-day-before task reminders are handled locally in Flutter.
    }

    @Override
    @Transactional
    public void dispatchRecurringTaskNotifications() {
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);

        List<Task> tasks = taskRepository.findRecurringTasksForWindow(
                tomorrowStart,
                tomorrowEnd,
                TaskStatus.COMPLETED,
                TaskRecurrenceType.NONE
        );

        for (Task task : tasks) {
            if (task.getStudent() == null || task.getStudent().getUser() == null) {
                continue;
            }

            StudentNotificationPreference preference = getPreferenceOrDefault(task.getStudent());

            if (!Boolean.TRUE.equals(preference.getRecurringTaskNotificationsEnabled())) {
                continue;
            }

            CreateNotificationRequest request = new CreateNotificationRequest();
            request.setTitle(buildRecurringTaskTitle(task));
            request.setBody(buildRecurringTaskBody(task));
            request.setType(NotificationType.RECURRING_TASK);
            request.setPriority(NotificationPriority.HIGH);
            request.setChannel(NotificationChannel.BOTH);
            request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
            request.setTargetValue("/student/tasks/" + task.getId());
            request.setBroadcastToAll(false);
            request.setRecipientUserIds(List.of(task.getStudent().getUser().getId()));

            createAndSend(request);

            task.setRecurrenceLastGeneratedAt(LocalDateTime.now());
            task.setNextOccurrenceAt(calculateNextOccurrence(task));
            taskRepository.save(task);
        }
    }

    @Override
    @Transactional
    public void dispatchEventNotifications() {
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1);

        List<Event> events = eventRepository.findEventsStartingBetween(tomorrowStart, tomorrowEnd);
        List<Student> students = studentRepository.findAll();

        for (Event event : events) {
            for (Student student : students) {
                if (student.getUser() == null) {
                    continue;
                }

                StudentNotificationPreference preference = getPreferenceOrDefault(student);

                if (!Boolean.TRUE.equals(preference.getEventNotificationsEnabled())) {
                    continue;
                }

                CreateNotificationRequest request = new CreateNotificationRequest();
                request.setTitle("Event reminder");
                request.setBody(buildEventReminderBody(event));
                request.setType(NotificationType.EVENT);
                request.setPriority(NotificationPriority.NORMAL);
                request.setChannel(NotificationChannel.BOTH);
                request.setTargetType(NotificationTargetType.INTERNAL_ROUTE);
                request.setTargetValue("/student/events/" + event.getId());
                request.setBroadcastToAll(false);
                request.setRecipientUserIds(List.of(student.getUser().getId()));

                createAndSend(request);
            }
        }
    }

    private void validateRequest(CreateNotificationRequest request) {
        if (!request.isBroadcastToAll()
                && (request.getRecipientUserIds() == null || request.getRecipientUserIds().isEmpty())) {
            throw new InvalidNotificationRequestException(
                    "recipientUserIds must not be empty when broadcastToAll is false"
            );
        }

        if (request.getTargetType() == NotificationTargetType.NONE && request.getTargetValue() != null) {
            throw new InvalidNotificationRequestException(
                    "targetValue must be null when targetType is NONE"
            );
        }

        if (request.getTargetType() != NotificationTargetType.NONE
                && (request.getTargetValue() == null || request.getTargetValue().isBlank())) {
            throw new InvalidNotificationRequestException(
                    "targetValue is required when targetType is not NONE"
            );
        }
    }

    private List<User> resolveRecipients(CreateNotificationRequest request) {
        if (request.isBroadcastToAll()) {
            return userRepository.findAll().stream()
                    .filter(user -> shouldReceiveNotification(user, request.getType()))
                    .toList();
        }

        return userRepository.findAllById(request.getRecipientUserIds()).stream()
                .filter(user -> shouldReceiveNotification(user, request.getType()))
                .toList();
    }

    private List<UUID> resolveAdminRecipients(CreateAdminNotificationRequest request) {
        return switch (request.getAudienceType()) {
            case ALL_USERS -> userRepository.findAll().stream()
                    .filter(user -> shouldReceiveNotification(user, request.getType()))
                    .map(User::getId)
                    .toList();

            case ALL_STUDENTS -> studentRepository.findAll().stream()
                    .filter(student -> student.getUser() != null)
                    .filter(student -> shouldReceiveStudentNotification(student, request.getType()))
                    .map(Student::getUser)
                    .map(User::getId)
                    .toList();

            case ALL_INSTRUCTORS -> userRepository.findAllByRole(UserRole.INSTRUCTOR).stream()
                    .map(User::getId)
                    .toList();

            case SPECIFIC_USERS -> {
                if (request.getUserIds() == null || request.getUserIds().isEmpty()) {
                    throw new IllegalArgumentException("userIds are required for SPECIFIC_USERS");
                }

                yield userRepository.findAllById(request.getUserIds()).stream()
                        .filter(user -> shouldReceiveNotification(user, request.getType()))
                        .map(User::getId)
                        .toList();
            }

            case SPECIFIC_STUDENTS -> {
                if (request.getStudentIds() == null || request.getStudentIds().isEmpty()) {
                    throw new IllegalArgumentException("studentIds are required for SPECIFIC_STUDENTS");
                }

                yield studentRepository.findAllById(request.getStudentIds()).stream()
                        .filter(student -> student.getUser() != null)
                        .filter(student -> shouldReceiveStudentNotification(student, request.getType()))
                        .map(Student::getUser)
                        .map(User::getId)
                        .toList();
            }
        };
    }

    private boolean shouldReceiveNotification(User user, NotificationType type) {
        if (user == null) {
            return false;
        }

        if (user.getRole() != UserRole.STUDENT) {
            return true;
        }

        return studentRepository.findByUserId(user.getId())
                .map(student -> shouldReceiveStudentNotification(student, type))
                .orElse(true);
    }

    private boolean shouldReceiveStudentNotification(Student student, NotificationType type) {
        if (student == null) {
            return false;
        }

        StudentNotificationPreference preference = getPreferenceOrDefault(student);

        return switch (type) {
            case EVENT -> Boolean.TRUE.equals(preference.getEventNotificationsEnabled());

            case TASK -> Boolean.TRUE.equals(preference.getTaskNotificationsEnabled());

            case RECURRING_TASK -> Boolean.TRUE.equals(preference.getRecurringTaskNotificationsEnabled());

            case MONTHLY_STATS -> Boolean.TRUE.equals(preference.getMonthlyStatsNotificationsEnabled());

            case GENERAL, SYSTEM -> true;
        };
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

    private String buildEventReminderBody(Event event) {
        return "The event \"" + event.getTitle() + "\" starts tomorrow. Tap to open it.";
    }

    private String buildRecurringTaskTitle(Task task) {
        return switch (task.getRecurrenceType()) {
            case DAILY -> "Daily recurring task reminder";
            case WEEKLY -> "Weekly recurring task reminder";
            case MONTHLY -> "Monthly recurring task reminder";
            default -> "Recurring task reminder";
        };
    }

    private String buildRecurringTaskBody(Task task) {
        return "Your recurring task \"" + task.getTitle() + "\" is scheduled for tomorrow. Tap to open it.";
    }

    private LocalDateTime calculateNextOccurrence(Task task) {
        if (task.getNextOccurrenceAt() == null || task.getRecurrenceType() == null) {
            return null;
        }

        int interval = task.getRecurrenceInterval() == null || task.getRecurrenceInterval() < 1
                ? 1
                : task.getRecurrenceInterval();

        return switch (task.getRecurrenceType()) {
            case DAILY -> task.getNextOccurrenceAt().plusDays(interval);
            case WEEKLY -> task.getNextOccurrenceAt().plusWeeks(interval);
            case MONTHLY -> task.getNextOccurrenceAt().plusMonths(interval);
            default -> null;
        };
    }

    @Override
    @Transactional
    public DeleteNotificationsResponse deleteMyNotification(UUID userId, UUID userNotificationId) {
        int deleted = userNotificationRepository.deleteOneForUser(userId, userNotificationId);

        if (deleted == 0) {
            throw new NotificationNotFoundException("Notification not found");
        }

        return DeleteNotificationsResponse.builder()
                .deletedCount(deleted)
                .message("Notification deleted successfully")
                .build();
    }

    @Override
    @Transactional
    public DeleteNotificationsResponse deleteAllMyNotifications(UUID userId) {
        int deleted = userNotificationRepository.deleteAllForUser(userId);

        return DeleteNotificationsResponse.builder()
                .deletedCount(deleted)
                .message("All notifications deleted successfully")
                .build();
    }
}