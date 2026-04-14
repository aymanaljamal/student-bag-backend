package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.tasks.entity.TaskReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskReminderRepository extends JpaRepository<TaskReminder, Long> {

    List<TaskReminder> findByEnabledTrueAndSentFalseAndRemindAtLessThanEqual(LocalDateTime now);

    List<TaskReminder> findByTaskIdOrderByRemindAtAsc(Long taskId);

    @Query("""
        select tr
        from TaskReminder tr
        join fetch tr.task t
        join fetch t.student s
        join fetch s.user u
        where tr.enabled = true
          and tr.sent = false
          and tr.remindAt is not null
          and tr.remindAt <= :now
          and t.isDeleted = false
          and t.archived = false
          and t.status <> :completedStatus
          and t.notificationsEnabled = true
    """)
    List<TaskReminder> findDueReminders(
            @Param("now") LocalDateTime now,
            @Param("completedStatus") TaskStatus completedStatus
    );
}