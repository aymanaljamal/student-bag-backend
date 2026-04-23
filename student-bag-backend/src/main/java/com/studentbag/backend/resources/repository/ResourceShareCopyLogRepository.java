package com.studentbag.backend.resources.repository;

import com.studentbag.backend.resources.entity.ResourceShareCopyLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceShareCopyLogRepository extends JpaRepository<ResourceShareCopyLog, Long> {

    List<ResourceShareCopyLog> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<ResourceShareCopyLog> findBySourceAdminResourceIdOrderByCreatedAtDesc(Long sourceAdminResourceId);

    List<ResourceShareCopyLog> findByCreatedPersonalItemIdOrderByCreatedAtDesc(Long createdPersonalItemId);
}