package com.studentbag.backend.notifications.entity;

import com.studentbag.backend.domain.enums.notifications.UserNotificationStatus;
import com.studentbag.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_notifications",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"notification_id", "user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserNotificationStatus status;

    @Column(nullable = false)
    private boolean readFlag = false;

    private LocalDateTime deliveredAt;

    private LocalDateTime readAt;
}