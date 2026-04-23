package com.studentbag.backend.resources.repository;

import com.studentbag.backend.resources.entity.ResourceApprovalAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceApprovalActionRepository extends JpaRepository<ResourceApprovalAction, Long> {

    List<ResourceApprovalAction> findByAdminResourceIdOrderByCreatedAtDesc(Long adminResourceId);

    List<ResourceApprovalAction> findByActorIdOrderByCreatedAtDesc(Long actorId);
}