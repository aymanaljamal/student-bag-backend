package com.studentbag.backend.schedule.mapper;

import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.schedule.dto.CourseSectionDTO;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
@Component
@RequiredArgsConstructor
public class TimetableMapper {

    private final ScheduleMapper scheduleMapper;

    public ScheduleOptionResponseDTO toOptionDTO(List<CourseSection> combination, Double score, List<String> insights) {
        if (combination == null) return null;

        List<CourseSectionDTO> sectionDTOs = combination.stream()
                .map(scheduleMapper::toSectionDTO)
                .collect(Collectors.toList());

        return ScheduleOptionResponseDTO.builder()
                .sections(sectionDTOs)
                .score(score)
                .insights(insights)
                .totalCreditHours(combination.stream()
                        .filter(s -> s.getCourse() != null)
                        .mapToInt(s -> s.getCourse().getCreditHours())
                        .sum())
                .build();
    }
}