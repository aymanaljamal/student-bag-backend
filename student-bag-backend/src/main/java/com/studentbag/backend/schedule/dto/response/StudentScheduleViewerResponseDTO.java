package com.studentbag.backend.schedule.dto.response;

import com.studentbag.backend.domain.enums.ScheduleStatus;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentScheduleViewerResponseDTO {

    private Long id;
    private Long termId;
    private String termName;
    private ScheduleStatus status;

    @Builder.Default
    private List<ScheduleViewerEntryResponseDTO> entries = new ArrayList<>();
}