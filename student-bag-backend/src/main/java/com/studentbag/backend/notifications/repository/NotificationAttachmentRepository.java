package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.notifications.entity.NotificationAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationAttachmentRepository extends JpaRepository<NotificationAttachment, UUID> {
}