package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.domain.enums.notifications.AdminMessageStatus;
import com.studentbag.backend.notifications.entity.InstitutionAdminMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstitutionAdminMessageRepository extends JpaRepository<InstitutionAdminMessage, Long> {

    @Query("""
        select distinct m
        from InstitutionAdminMessage m
        join fetch m.sender s
        join fetch m.institution i
        left join fetch m.notification n
        left join fetch n.recipients r
        left join fetch r.user ru
        where s.id = :senderUserId
        order by m.createdAt desc
    """)
    List<InstitutionAdminMessage> findMySentMessages(
            @Param("senderUserId") UUID senderUserId,
            Pageable pageable
    );

    @Query("""
        select distinct m
        from InstitutionAdminMessage m
        join fetch m.sender s
        join fetch m.institution i
        left join fetch m.notification n
        left join fetch n.recipients r
        left join fetch r.user ru
        where i.id = :institutionId
        order by m.createdAt desc
    """)
    List<InstitutionAdminMessage> findInstitutionInbox(
            @Param("institutionId") Long institutionId,
            Pageable pageable
    );

    @Query("""
        select distinct m
        from InstitutionAdminMessage m
        join fetch m.sender s
        join fetch m.institution i
        left join fetch m.notification n
        left join fetch n.recipients r
        left join fetch r.user ru
        where m.id = :messageId
    """)
    Optional<InstitutionAdminMessage> findDetailsById(@Param("messageId") Long messageId);

    long countBySenderId(UUID senderUserId);

    long countByInstitutionId(Long institutionId);

    long countByInstitutionIdAndStatus(Long institutionId, AdminMessageStatus status);
}