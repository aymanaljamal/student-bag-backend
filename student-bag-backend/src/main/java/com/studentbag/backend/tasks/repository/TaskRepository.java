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
        SELECT t
        FROM Task t
        JOIN FETCH t.student s
        JOIN FETCH s.user u
        LEFT JOIN FETCH t.course c
        WHERE t.isDeleted = false
          AND t.archived = false
          AND t.status <> :completedStatus
          AND t.notificationsEnabled = true
          AND t.recurrenceType <> :noneRecurrenceType
          AND t.nextOccurrenceAt IS NOT NULL
          AND t.nextOccurrenceAt >= :from
          AND t.nextOccurrenceAt < :to
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

    List<Task> findTop10ByStudentIdAndStatusNotAndArchivedFalseAndIsDeletedFalseOrderByDueDateTimeAsc(
            Long studentId,
            TaskStatus status
    );

    @Query("""
        SELECT DISTINCT t
        FROM Task t
        LEFT JOIN FETCH t.course c
        LEFT JOIN FETCH t.labels l
        LEFT JOIN FETCH t.subtasks st
        WHERE t.student.id = :studentId
          AND t.status <> com.studentbag.backend.domain.enums.tasks.TaskStatus.COMPLETED
          AND t.dueDateTime < :now
          AND t.archived = false
          AND t.isDeleted = false
        ORDER BY t.dueDateTime ASC
    """)
    List<Task> findOverdueTasksForAi(
            @Param("studentId") Long studentId,
            @Param("now") LocalDateTime now
    );

    @Query("""
        SELECT DISTINCT t
        FROM Task t
        LEFT JOIN FETCH t.course c
        LEFT JOIN FETCH t.labels l
        LEFT JOIN FETCH t.subtasks st
        WHERE t.student.id = :studentId
          AND t.status <> com.studentbag.backend.domain.enums.tasks.TaskStatus.COMPLETED
          AND t.dueDateTime BETWEEN :start AND :end
          AND t.archived = false
          AND t.isDeleted = false
        ORDER BY t.dueDateTime ASC
    """)
    List<Task> findTasksDueBetweenForAi(
            @Param("studentId") Long studentId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}