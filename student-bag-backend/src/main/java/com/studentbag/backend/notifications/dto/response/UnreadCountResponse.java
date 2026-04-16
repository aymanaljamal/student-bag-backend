package com.studentbag.backend.notifications.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UnreadCountResponse {
    private long unreadCount;
}