package com.studentbag.backend.courses.dto.request;

import com.studentbag.backend.domain.enums.Season;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TermRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String academicYear;

    @NotNull
    private Season season;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    private Long institutionId;
}