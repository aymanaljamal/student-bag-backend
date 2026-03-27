package com.studentbag.backend.courses.dto.response;

import com.studentbag.backend.domain.enums.Season;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class TermResponse {

    private Long id;
    private String name;
    private String academicYear;
    private Season season;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long institutionId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}