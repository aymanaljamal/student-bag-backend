package com.studentbag.backend.events.dto.response;

import com.studentbag.backend.domain.enums.courses.RegistrationStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class EventRegistrationInfoDTO {

    private Long registrationId;

    private Long studentId;

    private UUID userId;

    private String studentName;

    private String studentEmail;

    private RegistrationStatus status;
}