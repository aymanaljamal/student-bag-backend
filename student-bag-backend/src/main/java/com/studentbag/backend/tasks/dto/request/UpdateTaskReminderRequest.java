package com.studentbag.backend.tasks.dto.request;

import com.studentbag.backend.domain.enums.ReminderChannel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTaskReminderRequest {

    private Long id;

    private LocalDateTime remindAt;

    private Integer minutesBefore;

    private ReminderChannel channel;

    private Boolean enabled;

    private Boolean sent;
}