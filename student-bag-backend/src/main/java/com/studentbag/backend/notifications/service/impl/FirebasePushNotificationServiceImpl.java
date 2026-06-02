package com.studentbag.backend.notifications.service.impl;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.studentbag.backend.notifications.service.FirebasePushNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FirebasePushNotificationServiceImpl implements FirebasePushNotificationService {

    @Override
    public void sendToTokens(
            List<String> tokens,
            String title,
            String body,
            String route,
            String type
    ) {
        sendToTokens(
                tokens,
                title,
                body,
                Map.of(
                        "route", route != null ? route : "",
                        "type", type != null ? type : ""
                )
        );
    }

    @Override
    public void sendToTokens(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data
    ) {
        if (tokens == null || tokens.isEmpty()) {
            log.info("No active Firebase tokens found. Push notification skipped.");
            return;
        }

        Map<String, String> safeData = sanitizeData(data);

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(
                        Notification.builder()
                                .setTitle(title != null ? title : "")
                                .setBody(body != null ? body : "")
                                .build()
                )
                .putAllData(safeData)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = FirebaseMessaging.getInstance()
                    .sendEachForMulticast(message);

            log.info(
                    "Firebase push sent. tokens={}, success={}, failure={}",
                    tokens.size(),
                    response.getSuccessCount(),
                    response.getFailureCount()
            );

            if (response.getFailureCount() > 0) {
                log.warn("Some Firebase push notifications failed.");
            }

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send Firebase push notification.", e);
        }
    }

    private Map<String, String> sanitizeData(Map<String, String> data) {
        Map<String, String> safeData = new HashMap<>();

        if (data == null || data.isEmpty()) {
            return safeData;
        }

        data.forEach((key, value) -> {
            if (key != null && !key.isBlank()) {
                safeData.put(key, value != null ? value : "");
            }
        });

        return safeData;
    }
}