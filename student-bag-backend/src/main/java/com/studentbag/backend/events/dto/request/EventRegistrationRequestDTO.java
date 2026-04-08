package com.studentbag.backend.events.dto.request;

import lombok.Data;

@Data
public class EventRegistrationRequestDTO {
    private Long eventId;
    private Long studentId;

}