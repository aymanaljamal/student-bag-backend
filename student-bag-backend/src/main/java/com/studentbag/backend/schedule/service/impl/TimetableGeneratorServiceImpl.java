package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import com.studentbag.backend.schedule.service.ConflictService;
import com.studentbag.backend.schedule.service.TimetableGeneratorService;
import com.studentbag.backend.schedule.service.PreferenceRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableGeneratorServiceImpl implements TimetableGeneratorService {

    private final CourseSectionRepository sectionRepository;
    private final ConflictService conflictService;
    private final PreferenceRankingService rankingService;

    @Override
    public List<ScheduleOptionResponseDTO> generateValidOptions(Long termId, List<Long> courseIds,
                                                                List<Long> lockedSectionIds, Long studentId) {
        log.info("Generating timetable options for Student: {}, Term: {}, Courses: {}", studentId, termId, courseIds);

        // 1. Fetch all available sections for the selected courses
        List<CourseSection> allSections = sectionRepository.findByCourseIdInAndTermId(courseIds, termId);

        // 2. Group sections by Course ID
        Map<Long, List<CourseSection>> sectionsByCourse = allSections.stream()
                .collect(Collectors.groupingBy(s -> s.getCourse().getId()));

        // Validate that all requested courses have at least one section
        if (sectionsByCourse.size() < courseIds.size()) {
            log.warn("Some requested courses have no available sections in term {}", termId);
        }

        List<List<CourseSection>> validCombinations = new ArrayList<>();

        // 3. Execute Backtracking Algorithm
        generateCombinations(
                new ArrayList<>(sectionsByCourse.keySet()),
                0,
                new ArrayList<>(),
                sectionsByCourse,
                lockedSectionIds != null ? lockedSectionIds : Collections.emptyList(),
                validCombinations
        );

        log.info("Found {} valid non-conflicting combinations", validCombinations.size());

        // 4. Rank and Score based on FR-4.5 & FR-4.6
        return rankingService.rankAndScore(validCombinations, studentId);
    }

    private void generateCombinations(List<Long> courseIds, int index, List<CourseSection> currentPath,
                                      Map<Long, List<CourseSection>> sectionsByCourse,
                                      List<Long> lockedIds, List<List<CourseSection>> results) {
        // Base Case: If we have picked a section for every course
        if (index == courseIds.size()) {
            results.add(new ArrayList<>(currentPath));
            return;
        }

        Long currentCourseId = courseIds.get(index);
        List<CourseSection> sections = sectionsByCourse.get(currentCourseId);

        for (CourseSection section : sections) {
            // FR-4.7: Locked Sections Logic
            if (isCourseRestrictedByLock(sections, lockedIds) && !lockedIds.contains(section.getId())) {
                continue; // Skip sections that are not the locked one for this course
            }

            // FR-4.3: Time Conflict Check
            if (!conflictService.hasConflict(section, currentPath)) {
                currentPath.add(section);
                generateCombinations(courseIds, index + 1, currentPath, sectionsByCourse, lockedIds, results);
                currentPath.remove(currentPath.size() - 1); // Backtrack
            }
        }
    }

    private boolean isCourseRestrictedByLock(List<CourseSection> sections, List<Long> lockedIds) {
        return sections.stream().anyMatch(s -> lockedIds.contains(s.getId()));
    }
}