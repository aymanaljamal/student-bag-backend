package com.studentbag.backend.schedule.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class UpdateScheduleRequest {
    private List<UpdateScheduleEntryRequest> entries;
}