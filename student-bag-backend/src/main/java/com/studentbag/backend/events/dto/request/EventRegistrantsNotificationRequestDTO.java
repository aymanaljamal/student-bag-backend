package com.studentbag.backend.events.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventRegistrantsNotificationRequestDTO {

    @NotBlank(message = "Notification message must not be blank")
    @Size(min = 3, max = 500, message = "Notification message must be between 3 and 500 characters")
    private String message;
}