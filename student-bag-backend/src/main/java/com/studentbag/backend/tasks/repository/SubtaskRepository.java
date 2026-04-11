package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.tasks.entity.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubtaskRepository extends JpaRepository<Subtask, Long> {

    List<Subtask> findByTaskIdOrderByOrderIndexAsc(Long taskId);
}