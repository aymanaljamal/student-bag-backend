package com.studentbag.backend.notifications.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteNotificationsResponse {
    private int deletedCount;
    private String message;
}