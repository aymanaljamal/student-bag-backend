package com.studentbag.backend.resources.service;

import com.studentbag.backend.resources.entity.AdminResource;

import java.util.UUID;

public interface ResourceNotificationService {

    void notifyInstructorResourceApproved(AdminResource resource, UUID adminUserId);

    void notifyInstructorResourceRejected(AdminResource resource, UUID adminUserId, String reason);

    void notifyInstructorResourceRemoved(AdminResource resource, UUID adminUserId, String reason);
}