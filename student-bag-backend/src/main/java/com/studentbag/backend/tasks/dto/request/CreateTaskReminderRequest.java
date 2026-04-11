package com.studentbag.backend.tasks.dto.request;


import com.studentbag.backend.domain.enums.ReminderChannel;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTaskReminderRequest {

    private LocalDateTime remindAt;

    private Integer minutesBefore;

    @Builder.Default
    private ReminderChannel channel = ReminderChannel.IN_APP;

    @Builder.Default
    private Boolean enabled = true;
}