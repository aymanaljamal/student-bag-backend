package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.notifications.entity.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Long> {

    Optional<UserDeviceToken> findByFcmToken(String fcmToken);

    @Query("""
        select udt.fcmToken
        from UserDeviceToken udt
        where udt.user.id = :userId
          and udt.active = true
    """)
    List<String> findActiveTokensByUserId(UUID userId);
}