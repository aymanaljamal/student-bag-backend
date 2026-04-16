package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.domain.enums.notifications.UserNotificationStatus;
import com.studentbag.backend.notifications.entity.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {

    @Query("""
        select un
        from UserNotification un
        join fetch un.notification n
        left join fetch n.attachments
        where un.user.id = :userId
          and n.active = true
          and (n.expiresAt is null or n.expiresAt > :now)
        order by n.createdAt desc
    """)
    List<UserNotification> findActiveByUserId(@Param("userId") UUID userId,
                                              @Param("now") LocalDateTime now,
                                              Pageable pageable);

    @Query("""
        select count(un)
        from UserNotification un
        join un.notification n
        where un.user.id = :userId
          and un.readFlag = false
          and n.active = true
          and (n.expiresAt is null or n.expiresAt > :now)
    """)
    long countUnreadByUserId(@Param("userId") UUID userId,
                             @Param("now") LocalDateTime now);

    Optional<UserNotification> findByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("""
        update UserNotification un
        set un.readFlag = true,
            un.status = :status,
            un.readAt = :readAt
        where un.user.id = :userId
          and un.readFlag = false
    """)
    int markAllAsRead(@Param("userId") UUID userId,
                      @Param("status") UserNotificationStatus status,
                      @Param("readAt") LocalDateTime readAt);
}