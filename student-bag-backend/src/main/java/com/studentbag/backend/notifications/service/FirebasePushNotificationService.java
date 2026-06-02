package com.studentbag.backend.notifications.service;

import java.util.List;
import java.util.Map;

public interface FirebasePushNotificationService {

    void sendToTokens(
            List<String> tokens,
            String title,
            String body,
            String route,
            String type
    );

    void sendToTokens(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data
    );
}