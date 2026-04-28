package com.studentbag.backend.analytics.dto.dashboard;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSeriesPointDto {

    private LocalDate date;
    private Long value;
}