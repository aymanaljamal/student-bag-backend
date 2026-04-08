package com.studentbag.backend.schedule.dto.response;

import com.studentbag.backend.domain.enums.ScheduleStatus;
import com.studentbag.backend.schedule.dto.ScheduleEntryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder // <--- This generates the builder() method
@NoArgsConstructor // Required for JSON frameworks like Jackson
@AllArgsConstructor // Required for @Builder to work
public class StudentScheduleResponseDTO {
    private Long id;
    private Long termId;
    private String termName;
    private ScheduleStatus status;
    private List<ScheduleEntryDTO> entries;
}