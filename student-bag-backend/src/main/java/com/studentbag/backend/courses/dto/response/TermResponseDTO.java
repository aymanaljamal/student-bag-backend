package com.studentbag.backend.courses.dto.response;
import com.studentbag.backend.domain.enums.Season;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class TermResponseDTO {
    private Long id;
    private String termCode;
    private String name;
    private String academicYear;
    private Season season;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isCurrent;
}