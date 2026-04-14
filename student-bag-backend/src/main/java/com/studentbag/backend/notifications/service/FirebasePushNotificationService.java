package com.studentbag.backend.notifications.service;

import java.util.List;

public interface FirebasePushNotificationService {
    void sendToTokens(List<String> tokens, String title, String body, String route, String type);
}