package com.studentbag.backend.tasks.repository;

import com.studentbag.backend.tasks.entity.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    List<TaskAttachment> findByTaskId(Long taskId);

    @Query("""
            select a
            from TaskAttachment a
            join fetch a.task t
            where t.student.id = :studentId
              and t.isDeleted = false
              and t.archived = false
            order by a.createdAt desc
            """)
    List<TaskAttachment> findByStudentIdForAi(@Param("studentId") Long studentId);
}