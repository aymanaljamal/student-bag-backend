package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.tasks.entity.TaskReminder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskReminderRepository extends JpaRepository<TaskReminder, Long> {

    List<TaskReminder> findByTaskIdOrderByRemindAtAsc(Long taskId);

    List<TaskReminder> findByEnabledTrueAndSentFalseAndRemindAtLessThanEqual(LocalDateTime now);
}