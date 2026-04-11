package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.tasks.entity.TaskLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskLabelRepository extends JpaRepository<TaskLabel, Long> {

    List<TaskLabel> findByStudentId(Long studentId);

    Optional<TaskLabel> findByIdAndStudentId(Long id, Long studentId);

    boolean existsByStudentIdAndNameIgnoreCase(Long studentId, String name);
}