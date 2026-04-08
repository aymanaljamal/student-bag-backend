package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.schedule.service.ConflictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * Implementation of the ConflictService.
 * Handles time-overlap calculations for academic schedules.
 */
@Slf4j
@Service
public class ConflictServiceImpl implements ConflictService {

    /**
     * FR-4.3: Checks if two class sessions overlap in time.
     * Uses the interval overlap formula: (StartA < EndB) AND (StartB < EndA)
     */
    @Override
    public boolean isOverlap(ClassSession s1, ClassSession s2) {
        // 1. Null Safety check
        if (s1 == null || s2 == null) return false;

        // 2. Different days cannot have time conflicts
        if (s1.getDayOfWeek() != s2.getDayOfWeek()) return false;

        // 3. Mathematical overlap logic
        return s1.getStartTime().isBefore(s2.getEndTime()) &&
                s2.getStartTime().isBefore(s1.getEndTime());
    }

    /**
     * Checks if a new section conflicts with a list of already selected sections.
     */
    @Override
    public boolean hasConflict(CourseSection newSection, List<CourseSection> currentPicks) {
        // 1. Early exit if data is missing
        if (newSection == null || CollectionUtils.isEmpty(currentPicks)) {
            return false;
        }

        // 2. Ensure the new section actually has sessions to check
        if (CollectionUtils.isEmpty(newSection.getClassSessions())) {
            log.warn("Section {} has no sessions defined.", newSection.getId());
            return false;
        }

        // 3. Iterate and compare sessions
        for (CourseSection existingSection : currentPicks) {
            // Optimization: Skip if comparing the same section
            if (existingSection.getId() != null && existingSection.getId().equals(newSection.getId())) {
                continue;
            }

            for (ClassSession sessionA : newSection.getClassSessions()) {
                for (ClassSession sessionB : existingSection.getClassSessions()) {
                    if (isOverlap(sessionA, sessionB)) {
                        log.info("Schedule Conflict: Section {} overlaps with {} on {}",
                                newSection.getId(), existingSection.getId(), sessionA.getDayOfWeek());
                        return true;
                    }
                }
            }
        }
        return false;
    }
}