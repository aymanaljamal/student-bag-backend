package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}