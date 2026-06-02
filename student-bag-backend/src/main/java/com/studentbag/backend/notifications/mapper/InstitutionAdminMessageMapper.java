package com.studentbag.backend.notifications.mapper;

import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageResponse;
import com.studentbag.backend.notifications.entity.InstitutionAdminMessage;
import org.springframework.stereotype.Component;

@Component
public class InstitutionAdminMessageMapper {

    public InstitutionAdminMessageResponse toResponse(InstitutionAdminMessage message) {
        return InstitutionAdminMessageResponse.builder()
                .id(message.getId())

                .senderUserId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .senderEmail(message.getSender().getEmail())

                .institutionId(message.getInstitution().getId())
                .institutionName(message.getInstitution().getName())

                .subject(message.getSubject())
                .body(message.getBody())
                .status(message.getStatus())

                .notificationId(
                        message.getNotification() != null
                                ? message.getNotification().getId()
                                : null
                )

                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .resolvedAt(message.getResolvedAt())
                .build();
    }
}