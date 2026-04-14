package com.studentbag.backend.notifications.repository;

import com.studentbag.backend.notifications.entity.StudentNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentNotificationPreferenceRepository
        extends JpaRepository<StudentNotificationPreference, Long> {

    Optional<StudentNotificationPreference> findByStudentId(Long studentId);
}