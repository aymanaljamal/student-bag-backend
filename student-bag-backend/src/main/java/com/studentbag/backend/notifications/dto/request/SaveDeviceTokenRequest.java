package com.studentbag.backend.notifications.dto.request;

import lombok.Data;

@Data
public class SaveDeviceTokenRequest {
    private String fcmToken;
    private String deviceType;
}