package com.studentbag.backend.notifications.service;

import com.studentbag.backend.notifications.dto.request.SaveDeviceTokenRequest;

import java.util.UUID;

public interface DeviceTokenService {
    void saveToken(UUID userId, SaveDeviceTokenRequest request);
}