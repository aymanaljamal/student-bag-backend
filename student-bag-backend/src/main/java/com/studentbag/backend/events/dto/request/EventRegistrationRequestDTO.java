package com.studentbag.backend.events.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequestDTO {

    /**
     * optional note for future use
     */
    private String note;
}