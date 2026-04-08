package com.studentbag.backend.schedule.dto.response;

import com.studentbag.backend.schedule.dto.CourseSectionDTO;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleOptionResponseDTO {

    /**
     * The list of sections included in this specific timetable combination.
     */
    private List<CourseSectionDTO> sections;

    /**
     * The calculated quality score (e.g., 0-100) based on student preferences.
     */
    private Double score;

    /**
     * A human-readable tag like "Best Match", "Balanced", or "Early Bird".
     */
    private String rankLabel;

    /**
     * Descriptive insights such as ["Free Fridays!", "No morning classes"].
     * This directly supports the requirements in FR-4.5.
     */
    private List<String> insights;

    /**
     * Cumulative credit hours for this specific combination.
     */
    private Integer totalCreditHours;
}