package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.domain.enums.TaskStatus;
import com.studentbag.backend.tasks.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStudentIdAndIsDeletedFalse(Long studentId);

    List<Task> findByStudentIdAndStatusAndIsDeletedFalse(Long studentId, TaskStatus status);

    List<Task> findByStudentIdAndDueDateTimeBetweenAndIsDeletedFalse(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Task> findByStudentIdAndCourseIdAndIsDeletedFalse(Long studentId, Long courseId);

    Optional<Task> findByIdAndStudentIdAndIsDeletedFalse(Long id, Long studentId);
}