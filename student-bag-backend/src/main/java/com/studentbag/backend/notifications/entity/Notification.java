package com.studentbag.backend.notifications.entity;

import com.studentbag.backend.domain.enums.notifications.NotificationChannel;
import com.studentbag.backend.domain.enums.notifications.NotificationPriority;
import com.studentbag.backend.domain.enums.notifications.NotificationTargetType;
import com.studentbag.backend.domain.enums.notifications.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationTargetType targetType;

    @Column(length = 1000)
    private String targetValue; // external url or app route

    @Column(length = 1000)
    private String imageUrl;

    @Column(length = 1000)
    private String iconUrl;

    @Column(nullable = false)
    private boolean broadcastToAll;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime scheduledAt;

    private LocalDateTime expiresAt;

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserNotification> recipients = new ArrayList<>();

    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NotificationAttachment> attachments = new ArrayList<>();
}