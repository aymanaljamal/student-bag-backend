package com.studentbag.backend.notifications.service.impl;

import com.google.firebase.messaging.*;
import com.studentbag.backend.notifications.service.FirebasePushNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebasePushNotificationServiceImpl implements FirebasePushNotificationService {

    @Override
    public void sendToTokens(List<String> tokens, String title, String body, String route, String type) {
        if (tokens == null || tokens.isEmpty()) {
            return;
        }

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(Map.of(
                        "route", route != null ? route : "",
                        "type", type != null ? type : ""
                ))
                .addAllTokens(tokens)
                .build();

        try {
            FirebaseMessaging.getInstance().sendEachForMulticast(message);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Failed to send Firebase push notification", e);
        }
    }
}