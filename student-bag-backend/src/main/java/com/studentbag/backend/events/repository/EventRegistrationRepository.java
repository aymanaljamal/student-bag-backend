package com.studentbag.backend.events.repository;

import com.studentbag.backend.domain.enums.RegistrationStatus;
import com.studentbag.backend.events.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // FR-9.4: Check if a student is already registered
    Optional<EventRegistration> findByEventIdAndStudentId(Long eventId, Long studentId);

    // FR-9.8: Track status (Saved, Applied, etc.) for a specific student
    List<EventRegistration> findAllByStudentId(Long studentId);

    // FR-9.4: Count active registrations to enforce capacity limits
    long countByEventIdAndStatusIn(Long eventId, List<RegistrationStatus> activeStatuses);

    // Find registrations for students who need Smart Schedule notifications (FR-9.5)
    List<EventRegistration> findAllByEventIdAndStatus(Long eventId, RegistrationStatus status);
}