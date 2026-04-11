package com.studentbag.backend.tasks.dto.response;


import com.studentbag.backend.domain.enums.ReminderChannel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskReminderResponse {

    private Long id;
    private LocalDateTime remindAt;
    private Integer minutesBefore;
    private ReminderChannel channel;
    private Boolean enabled;
    private Boolean sent;
    private LocalDateTime sentAt;
}