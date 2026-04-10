package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.schedule.service.ConflictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Service
public class ConflictServiceImpl implements ConflictService {

    @Override
    public boolean isOverlap(ClassSession s1, ClassSession s2) {
        if (s1 == null || s2 == null) {
            return false;
        }

        if (s1.getDayOfWeek() == null || s2.getDayOfWeek() == null) {
            log.warn("Cannot compare overlap because one session has null dayOfWeek. s1={}, s2={}", s1, s2);
            return false;
        }

        if (s1.getStartTime() == null || s1.getEndTime() == null ||
                s2.getStartTime() == null || s2.getEndTime() == null) {
            log.warn("Cannot compare overlap because one session has null time. s1={}, s2={}", s1, s2);
            return false;
        }

        if (!s1.getDayOfWeek().equals(s2.getDayOfWeek())) {
            return false;
        }

        return s1.getStartTime().isBefore(s2.getEndTime()) &&
                s2.getStartTime().isBefore(s1.getEndTime());
    }

    @Override
    public boolean hasConflict(CourseSection newSection, List<CourseSection> currentPicks) {
        if (newSection == null || CollectionUtils.isEmpty(currentPicks)) {
            return false;
        }

        if (CollectionUtils.isEmpty(newSection.getClassSessions())) {
            log.warn("New section {} has no class sessions defined.", newSection.getId());
            return false;
        }

        for (CourseSection existingSection : currentPicks) {
            if (existingSection == null) {
                continue;
            }

            if (existingSection.getId() != null && existingSection.getId().equals(newSection.getId())) {
                continue;
            }

            if (CollectionUtils.isEmpty(existingSection.getClassSessions())) {
                log.warn("Existing section {} has no class sessions defined.", existingSection.getId());
                continue;
            }

            for (ClassSession sessionA : newSection.getClassSessions()) {
                for (ClassSession sessionB : existingSection.getClassSessions()) {
                    log.debug(
                            "Comparing section {} [{} {}-{}] with section {} [{} {}-{}]",
                            newSection.getId(),
                            sessionA != null ? sessionA.getDayOfWeek() : null,
                            sessionA != null ? sessionA.getStartTime() : null,
                            sessionA != null ? sessionA.getEndTime() : null,
                            existingSection.getId(),
                            sessionB != null ? sessionB.getDayOfWeek() : null,
                            sessionB != null ? sessionB.getStartTime() : null,
                            sessionB != null ? sessionB.getEndTime() : null
                    );

                    if (isOverlap(sessionA, sessionB)) {
                        log.info(
                                "Schedule conflict found: section {} overlaps with section {} on {}",
                                newSection.getId(),
                                existingSection.getId(),
                                sessionA.getDayOfWeek()
                        );
                        return true;
                    }
                }
            }
        }

        return false;
    }
}