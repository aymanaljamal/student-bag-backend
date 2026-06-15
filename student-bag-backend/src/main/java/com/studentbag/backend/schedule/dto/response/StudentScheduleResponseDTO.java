package com.studentbag.backend.schedule.dto.response;

import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.schedule.dto.ScheduleEntryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentScheduleResponseDTO {
    private Long id;
    private String name;
    private Long termId;
    private String termName;
    private ScheduleStatus status;
    private List<ScheduleEntryDTO> entries;
}