package com.studentbag.backend.schedule.dto.response;

import com.studentbag.backend.schedule.dto.ConflictDTO;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UpdateScheduleResponseDTO {
    private  StudentScheduleViewerResponseDTO schedule;
    private List<ConflictDTO> conflicts;   // قائمة conflicts لو في
    private boolean hasConflicts;
}