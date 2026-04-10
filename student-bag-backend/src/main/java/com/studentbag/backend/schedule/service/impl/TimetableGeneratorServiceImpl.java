package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import com.studentbag.backend.schedule.service.ConflictService;
import com.studentbag.backend.schedule.service.PreferenceRankingService;
import com.studentbag.backend.schedule.service.TimetableGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableGeneratorServiceImpl implements TimetableGeneratorService {

    private final CourseSectionRepository sectionRepository;
    private final ConflictService conflictService;
    private final PreferenceRankingService rankingService;

    @Override
    public List<ScheduleOptionResponseDTO> generateValidOptions(
            Long termId,
            List<Long> courseIds,
            List<Long> lockedSectionIds,
            Map<Long, Integer> courseRatings,
            Long studentId
    ) {
        log.info(
                "Generating timetable options for student={}, term={}, courseIds={}, lockedSectionIds={}, courseRatings={}",
                studentId, termId, courseIds, lockedSectionIds, courseRatings
        );

        if (termId == null || CollectionUtils.isEmpty(courseIds)) {
            log.warn("Cannot generate timetable options because termId or courseIds are missing.");
            return Collections.emptyList();
        }

        List<Long> normalizedLockedIds = lockedSectionIds != null
                ? lockedSectionIds
                : Collections.emptyList();

        Map<Long, Integer> normalizedRatings = courseRatings != null
                ? courseRatings
                : Collections.emptyMap();

        // مهم: يفضّل أن يكون هذا الميثود في الريبو عامل fetch للـ classSessions
        List<CourseSection> allSections =
                sectionRepository.findByCourseIdInAndTermId(courseIds, termId);

        log.info("Loaded {} sections from repository for term={}", allSections.size(), termId);
        for (CourseSection section : allSections) {
            int sessionsCount = section.getClassSessions() == null ? 0 : section.getClassSessions().size();
            log.info("Section {} for course {} has {} sessions",
                    section.getId(),
                    section.getCourse() != null ? section.getCourse().getId() : null,
                    sessionsCount
            );
        }

        Map<Long, List<CourseSection>> sectionsByCourse = allSections.stream()
                .filter(section -> section.getCourse() != null && section.getCourse().getId() != null)
                .collect(Collectors.groupingBy(
                        section -> section.getCourse().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        if (sectionsByCourse.size() < courseIds.size()) {
            List<Long> foundCourseIds = new ArrayList<>(sectionsByCourse.keySet());
            List<Long> missingCourseIds = courseIds.stream()
                    .filter(id -> !foundCourseIds.contains(id))
                    .toList();

            log.warn("Some requested courses have no available sections in term={}. Missing courseIds={}",
                    termId, missingCourseIds);
        }

        List<Long> orderedCourseIds = new ArrayList<>(courseIds);
        orderedCourseIds.sort(Comparator.comparingInt(courseId -> {
            List<CourseSection> sections = sectionsByCourse.getOrDefault(courseId, Collections.emptyList());
            return sections.size();
        }));

        List<List<CourseSection>> validCombinations = new ArrayList<>();

        generateCombinations(
                orderedCourseIds,
                0,
                new ArrayList<>(),
                sectionsByCourse,
                normalizedLockedIds,
                validCombinations
        );

        log.info("Found {} valid non-conflicting combinations", validCombinations.size());

        return rankingService.rankAndScore(
                validCombinations,
                studentId,
                normalizedRatings
        );
    }

    private void generateCombinations(
            List<Long> courseIds,
            int index,
            List<CourseSection> currentPath,
            Map<Long, List<CourseSection>> sectionsByCourse,
            List<Long> lockedIds,
            List<List<CourseSection>> results
    ) {
        if (index == courseIds.size()) {
            results.add(new ArrayList<>(currentPath));
            return;
        }

        Long currentCourseId = courseIds.get(index);
        List<CourseSection> sections =
                sectionsByCourse.getOrDefault(currentCourseId, Collections.emptyList());

        if (sections.isEmpty()) {
            log.debug("No sections found for course {}", currentCourseId);
            return;
        }

        boolean courseRestrictedByLock = isCourseRestrictedByLock(sections, lockedIds);

        for (CourseSection section : sections) {
            if (courseRestrictedByLock && !lockedIds.contains(section.getId())) {
                log.debug("Skipping section {} because course {} is restricted by lock",
                        section.getId(), currentCourseId);
                continue;
            }

            if (conflictService.hasConflict(section, currentPath)) {
                log.debug("Skipping section {} because it conflicts with current path {}",
                        section.getId(),
                        currentPath.stream().map(CourseSection::getId).toList()
                );
                continue;
            }

            currentPath.add(section);

            generateCombinations(
                    courseIds,
                    index + 1,
                    currentPath,
                    sectionsByCourse,
                    lockedIds,
                    results
            );

            currentPath.remove(currentPath.size() - 1);
        }
    }

    private boolean isCourseRestrictedByLock(List<CourseSection> sections, List<Long> lockedIds) {
        if (CollectionUtils.isEmpty(sections) || CollectionUtils.isEmpty(lockedIds)) {
            return false;
        }

        return sections.stream().anyMatch(section -> lockedIds.contains(section.getId()));
    }
}