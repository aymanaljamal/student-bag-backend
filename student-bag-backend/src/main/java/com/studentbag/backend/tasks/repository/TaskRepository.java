package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.domain.enums.tasks.TaskRecurrenceType;
import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.tasks.entity.Task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStudentIdAndIsDeletedFalse(Long studentId);

    List<Task> findByStudentIdAndStatusAndIsDeletedFalse(Long studentId, TaskStatus status);
    @Query("""
    select t
    from Task t
    join fetch t.student s
    join fetch s.user u
    left join fetch t.course c
    where t.isDeleted = false
      and t.archived = false
      and t.status <> :completedStatus
      and t.notificationsEnabled = true
      and t.recurrenceType <> :noneRecurrenceType
      and t.nextOccurrenceAt is not null
      and t.nextOccurrenceAt >= :from
      and t.nextOccurrenceAt < :to
""")
    List<Task> findRecurringTasksForWindow(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("completedStatus") TaskStatus completedStatus,
            @Param("noneRecurrenceType") TaskRecurrenceType noneRecurrenceType
    );

    List<Task> findByStudentIdAndDueDateTimeBetweenAndIsDeletedFalse(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Task> findByStudentIdAndCourseIdAndIsDeletedFalse(Long studentId, Long courseId);

    Optional<Task> findByIdAndStudentIdAndIsDeletedFalse(Long id, Long studentId);
}