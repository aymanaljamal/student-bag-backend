package com.studentbag.backend.notifications.service.impl;

import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.administrator.repository.AdministratorRepository;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.domain.enums.notifications.AdminMessageStatus;
import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import com.studentbag.backend.domain.enums.notifications.UserNotificationStatus;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.notifications.dto.request.SendInstitutionAdminMessageRequest;
import com.studentbag.backend.notifications.dto.request.UpdateInstitutionAdminMessageRequest;
import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageResponse;
import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageStatsResponse;
import com.studentbag.backend.notifications.entity.InstitutionAdminMessage;
import com.studentbag.backend.notifications.entity.Notification;
import com.studentbag.backend.notifications.entity.UserNotification;
import com.studentbag.backend.notifications.exception.NotificationNotFoundException;
import com.studentbag.backend.notifications.repository.InstitutionAdminMessageRepository;
import com.studentbag.backend.notifications.repository.NotificationRepository;
import com.studentbag.backend.notifications.repository.UserDeviceTokenRepository;
import com.studentbag.backend.notifications.repository.UserNotificationRepository;
import com.studentbag.backend.notifications.service.FirebasePushNotificationService;
import com.studentbag.backend.notifications.service.InstitutionAdminMessageService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InstitutionAdminMessageServiceImpl implements InstitutionAdminMessageService {

    private static final String STUDENT_MESSAGE_TYPE = "STUDENT_MESSAGE";
    private final StudentRepository studentRepository;
    private final AdministratorRepository administratorRepository;
    private final InstitutionAdminMessageRepository messageRepository;
    private final NotificationRepository notificationRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final UserDeviceTokenRepository userDeviceTokenRepository;
    private final FirebasePushNotificationService firebasePushNotificationService;

    @Override
    public InstitutionAdminMessageResponse sendToInstitutionAdmins(
            User sender,
            SendInstitutionAdminMessageRequest request
    ) {
        validateUser(sender);
        validateRequest(request);

        Institution institution = resolveSenderInstitution(sender);

        List<User> admins = administratorRepository.findActiveAdminUsersByInstitutionId(
                institution.getId()
        );

        if (admins.isEmpty()) {
            throw new IllegalStateException(
                    "No active administrators found for institution: " + getInstitutionName(institution)
            );
        }

        InstitutionAdminMessage message = InstitutionAdminMessage.builder()
                .sender(sender)
                .institution(institution)
                .subject(request.getSubject().trim())
                .body(request.getBody().trim())
                .status(AdminMessageStatus.OPEN)
                .build();

        message = messageRepository.save(message);

        Notification notification = buildAdminNotification(sender, message, admins);
        notification = notificationRepository.save(notification);

        message.setNotification(notification);
        message = messageRepository.save(message);

        sendFirebasePushToAdmins(sender, message, notification, admins);

        return toResponse(message, sender);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstitutionAdminMessageResponse> getMySentMessages(
            User currentUser,
            int page,
            int size
    ) {
        validateUser(currentUser);

        return messageRepository.findMySentMessages(
                        currentUser.getId(),
                        PageRequest.of(safePage(page), safeSize(size))
                )
                .stream()
                .map(message -> toResponse(message, currentUser))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstitutionAdminMessageResponse> getAdminInboxMessages(
            User adminUser,
            int page,
            int size
    ) {
        validateAdmin(adminUser);

        Institution institution = resolveAdminInstitution(adminUser);

        return messageRepository.findInstitutionInbox(
                        institution.getId(),
                        PageRequest.of(safePage(page), safeSize(size))
                )
                .stream()
                .map(message -> toResponse(message, adminUser))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InstitutionAdminMessageResponse getMessageDetails(
            User currentUser,
            Long messageId
    ) {
        validateUser(currentUser);

        InstitutionAdminMessage message = findMessageDetails(messageId);

        ensureCanAccessMessage(currentUser, message);

        return toResponse(message, currentUser);
    }

    @Override
    public InstitutionAdminMessageResponse updateMyMessage(
            User currentUser,
            Long messageId,
            UpdateInstitutionAdminMessageRequest request
    ) {
        validateUser(currentUser);
        validateUpdateRequest(request);

        InstitutionAdminMessage message = findMessageDetails(messageId);

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can update only your own messages.");
        }

        if (message.getStatus() != AdminMessageStatus.OPEN) {
            throw new IllegalStateException("Only open messages can be updated.");
        }

        message.setSubject(request.getSubject().trim());
        message.setBody(request.getBody().trim());

        if (message.getNotification() != null) {
            Notification notification = message.getNotification();
            notification.setTitle("Updated message from " + safeText(currentUser.getFullName()));
            notification.setBody(message.getSubject());
            notificationRepository.save(notification);
        }

        message = messageRepository.save(message);

        return toResponse(message, currentUser);
    }

    @Override
    public void deleteMyMessage(
            User currentUser,
            Long messageId
    ) {
        validateUser(currentUser);

        InstitutionAdminMessage message = findMessageDetails(messageId);

        if (!message.getSender().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("You can delete only your own messages.");
        }

        if (message.getNotification() != null) {
            UUID notificationId = message.getNotification().getId();

            userNotificationRepository.deleteByNotificationId(notificationId);
            notificationRepository.delete(message.getNotification());
        }

        messageRepository.delete(message);
    }

    @Override
    public InstitutionAdminMessageResponse markMessageAsReadByAdmin(
            User adminUser,
            Long messageId
    ) {
        validateAdmin(adminUser);

        InstitutionAdminMessage message = findMessageDetails(messageId);
        Institution adminInstitution = resolveAdminInstitution(adminUser);

        if (!message.getInstitution().getId().equals(adminInstitution.getId())) {
            throw new IllegalStateException("You cannot read messages outside your institution.");
        }

        if (message.getNotification() == null) {
            return toResponse(message, adminUser);
        }

        UserNotification userNotification = userNotificationRepository
                .findByNotificationIdAndUserId(
                        message.getNotification().getId(),
                        adminUser.getId()
                )
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification recipient record not found"
                ));

        if (!userNotification.isReadFlag()) {
            userNotification.setReadFlag(true);
            userNotification.setStatus(UserNotificationStatus.READ);
            userNotification.setReadAt(LocalDateTime.now());
            userNotificationRepository.save(userNotification);
        }

        return toResponse(message, adminUser);
    }

    @Override
    @Transactional(readOnly = true)
    public InstitutionAdminMessageStatsResponse getAdminInstitutionMessageStats(User adminUser) {
        validateAdmin(adminUser);

        Institution institution = resolveAdminInstitution(adminUser);

        long totalMessages = messageRepository.countByInstitutionId(institution.getId());

        long openMessages = messageRepository.countByInstitutionIdAndStatus(
                institution.getId(),
                AdminMessageStatus.OPEN
        );

        long resolvedMessages = messageRepository.countByInstitutionIdAndStatus(
                institution.getId(),
                AdminMessageStatus.RESOLVED
        );

        long archivedMessages = messageRepository.countByInstitutionIdAndStatus(
                institution.getId(),
                AdminMessageStatus.ARCHIVED
        );

        List<InstitutionAdminMessage> messages = messageRepository.findInstitutionInbox(
                institution.getId(),
                PageRequest.of(0, 500)
        );

        long totalAdminRecipients = 0;
        long readByAdmins = 0;
        long unreadByAdmins = 0;

        for (InstitutionAdminMessage message : messages) {
            if (message.getNotification() == null) {
                continue;
            }

            UUID notificationId = message.getNotification().getId();

            totalAdminRecipients += userNotificationRepository.countByNotificationId(notificationId);
            readByAdmins += userNotificationRepository.countReadByNotificationId(notificationId);
            unreadByAdmins += userNotificationRepository.countUnreadByNotificationId(notificationId);
        }

        return InstitutionAdminMessageStatsResponse.builder()
                .totalMessages(totalMessages)
                .openMessages(openMessages)
                .resolvedMessages(resolvedMessages)
                .archivedMessages(archivedMessages)
                .totalAdminRecipients(totalAdminRecipients)
                .readByAdmins(readByAdmins)
                .unreadByAdmins(unreadByAdmins)
                .build();
    }

    private Notification buildAdminNotification(
            User sender,
            InstitutionAdminMessage message,
            List<User> admins
    ) {
        String route = buildAdminMessageRoute(message.getId());

        Notification notification = Notification.builder()
                .title("New message from " + safeText(sender.getFullName()))
                .body(message.getSubject())
                .type(NotificationType.STUDENT_MESSAGE)
                .priority(NotificationPriority.HIGH)
                .channel(NotificationChannel.BOTH)
                .targetType(NotificationTargetType.INTERNAL_ROUTE)
                .targetValue(route)
                .broadcastToAll(false)
                .active(true)
                .build();

        for (User admin : admins) {
            UserNotification recipient = UserNotification.builder()
                    .user(admin)
                    .status(UserNotificationStatus.SENT)
                    .readFlag(false)
                    .build();

            notification.addRecipient(recipient);
        }

        return notification;
    }

    private void sendFirebasePushToAdmins(
            User sender,
            InstitutionAdminMessage message,
            Notification notification,
            List<User> admins
    ) {
        String route = buildAdminMessageRoute(message.getId());

        String title = "New message from " + safeText(sender.getFullName());
        String body = message.getSubject();

        for (User admin : admins) {
            List<String> tokens = userDeviceTokenRepository.findActiveTokensByUserId(admin.getId());

            firebasePushNotificationService.sendToTokens(
                    tokens,
                    title,
                    body,
                    Map.of(
                            "notificationId", notification.getId().toString(),
                            "messageId", message.getId().toString(),
                            "route", route,
                            "type", STUDENT_MESSAGE_TYPE,
                            "targetType", NotificationTargetType.INTERNAL_ROUTE.name(),
                            "targetValue", route,
                            "senderUserId", sender.getId().toString(),
                            "institutionId", message.getInstitution().getId().toString()
                    )
            );
        }
    }

    private Institution resolveSenderInstitution(User sender) {
        validateUser(sender);

        Student student = studentRepository.findByUserId(sender.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Only users linked to a student institution can send institution admin messages currently."
                ));

        if (student.getInstitution() == null) {
            throw new IllegalStateException("The sender is not linked to any institution.");
        }

        return student.getInstitution();
    }

    private Institution resolveAdminInstitution(User adminUser) {
        validateAdmin(adminUser);

        Administrator administrator = administratorRepository.findByUserId(adminUser.getId())
                .orElseThrow(() -> new IllegalStateException("Administrator profile was not found."));

        if (administrator.getInstitution() == null) {
            throw new IllegalStateException("Administrator is not linked to any institution.");
        }

        return administrator.getInstitution();
    }

    private InstitutionAdminMessage findMessageDetails(Long messageId) {
        if (messageId == null) {
            throw new IllegalArgumentException("Message id is required.");
        }

        return messageRepository.findDetailsById(messageId)
                .orElseThrow(() -> new NotificationNotFoundException("Admin message not found"));
    }

    private void ensureCanAccessMessage(User currentUser, InstitutionAdminMessage message) {
        if (message.getSender().getId().equals(currentUser.getId())) {
            return;
        }

        if (currentUser.getRole() == UserRole.ADMINISTRATOR) {
            Institution adminInstitution = resolveAdminInstitution(currentUser);

            if (message.getInstitution().getId().equals(adminInstitution.getId())) {
                return;
            }
        }

        throw new IllegalStateException("You cannot access this admin message.");
    }

    private InstitutionAdminMessageResponse toResponse(
            InstitutionAdminMessage message,
            User currentUser
    ) {
        UUID notificationId = message.getNotification() != null
                ? message.getNotification().getId()
                : null;

        long totalRecipients = 0;
        long readCount = 0;
        long unreadCount = 0;

        Boolean readByCurrentUser = null;
        LocalDateTime currentUserReadAt = null;

        if (notificationId != null) {
            totalRecipients = userNotificationRepository.countByNotificationId(notificationId);
            readCount = userNotificationRepository.countReadByNotificationId(notificationId);
            unreadCount = userNotificationRepository.countUnreadByNotificationId(notificationId);

            if (currentUser != null && currentUser.getId() != null) {
                var currentRecipient = userNotificationRepository
                        .findByNotificationIdAndUserId(notificationId, currentUser.getId());

                if (currentRecipient.isPresent()) {
                    readByCurrentUser = currentRecipient.get().isReadFlag();
                    currentUserReadAt = currentRecipient.get().getReadAt();
                }
            }
        }

        return InstitutionAdminMessageResponse.builder()
                .id(message.getId())

                .senderUserId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderEmail(message.getSender().getEmail())

                .institutionId(message.getInstitution().getId())
                .institutionName(getInstitutionName(message.getInstitution()))

                .subject(message.getSubject())
                .body(message.getBody())

                .status(message.getStatus())

                .notificationId(notificationId)

                .totalAdminRecipients(totalRecipients)
                .readByAdmins(readCount)
                .unreadByAdmins(unreadCount)

                .readByCurrentUser(readByCurrentUser)
                .currentUserReadAt(currentUserReadAt)

                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .resolvedAt(message.getResolvedAt())

                .build();
    }

    private void validateRequest(SendInstitutionAdminMessageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        String subject = request.getSubject() != null ? request.getSubject().trim() : "";
        String body = request.getBody() != null ? request.getBody().trim() : "";

        if (subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (body.isBlank()) {
            throw new IllegalArgumentException("Message body is required.");
        }

        if (subject.length() > 200) {
            throw new IllegalArgumentException("Subject must not exceed 200 characters.");
        }

        if (body.length() > 5000) {
            throw new IllegalArgumentException("Message body must not exceed 5000 characters.");
        }
    }

    private void validateUpdateRequest(UpdateInstitutionAdminMessageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required.");
        }

        String subject = request.getSubject() != null ? request.getSubject().trim() : "";
        String body = request.getBody() != null ? request.getBody().trim() : "";

        if (subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required.");
        }

        if (body.isBlank()) {
            throw new IllegalArgumentException("Message body is required.");
        }

        if (subject.length() > 200) {
            throw new IllegalArgumentException("Subject must not exceed 200 characters.");
        }

        if (body.length() > 5000) {
            throw new IllegalArgumentException("Message body must not exceed 5000 characters.");
        }
    }

    private void validateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalStateException("Authenticated user is required.");
        }
    }

    private void validateAdmin(User user) {
        validateUser(user);

        if (user.getRole() != UserRole.ADMINISTRATOR) {
            throw new IllegalStateException("Only administrators can access this endpoint.");
        }
    }

    private int safePage(int page) {
        return Math.max(page, 0);
    }

    private int safeSize(int size) {
        if (size < 1) {
            return 20;
        }

        return Math.min(size, 100);
    }

    private String buildAdminMessageRoute(Long messageId) {
        return "studentbag://admin/messages/" + messageId;
    }

    private String safeText(String value) {
        return value != null && !value.isBlank() ? value.trim() : "User";
    }

    private String getInstitutionName(Institution institution) {
        if (institution == null || institution.getName() == null || institution.getName().isBlank()) {
            return "Unknown institution";
        }

        return institution.getName();
    }
}