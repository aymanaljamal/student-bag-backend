package com.studentbag.backend.events.repository;

import com.studentbag.backend.domain.enums.courses.RegistrationStatus;
import com.studentbag.backend.events.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    Optional<EventRegistration> findByEventIdAndStudentId(Long eventId, Long studentId);

    List<EventRegistration> findAllByStudentId(Long studentId);

    List<EventRegistration> findAllByEventId(Long eventId);

    List<EventRegistration> findAllByEventIdAndStatus(Long eventId, RegistrationStatus status);

    long countByEventIdAndStatusIn(Long eventId, List<RegistrationStatus> activeStatuses);
    @Query("""
    SELECT er
    FROM EventRegistration er
    JOIN FETCH er.event e
    LEFT JOIN FETCH e.opportunity o
    WHERE er.student.id = :studentId
      AND er.status <> com.studentbag.backend.domain.enums.courses.RegistrationStatus.CANCELLED
      AND e.startDateTime >= :now
    ORDER BY e.startDateTime ASC
""")
    List<EventRegistration> findUpcomingRegisteredEventsForAi(
            Long studentId,
            LocalDateTime now
    );
    void deleteByEventId(Long eventId);
}