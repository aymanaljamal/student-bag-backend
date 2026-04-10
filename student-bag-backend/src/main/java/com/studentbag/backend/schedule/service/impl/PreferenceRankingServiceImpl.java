package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import com.studentbag.backend.schedule.entity.StudentSchedulePreference;
import com.studentbag.backend.schedule.mapper.TimetableMapper;
import com.studentbag.backend.schedule.repository.StudentSchedulePreferenceRepository;
import com.studentbag.backend.schedule.service.PreferenceRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceRankingServiceImpl implements PreferenceRankingService {

    private final StudentSchedulePreferenceRepository prefRepository;
    private final TimetableMapper timetableMapper;

    @Override
    public List<ScheduleOptionResponseDTO> rankAndScore(
            List<List<CourseSection>> options,
            Long studentId,
            Map<Long, Integer> courseRatings
    ) {
        StudentSchedulePreference prefs = prefRepository.findByStudentId(studentId)
                .orElse(new StudentSchedulePreference());

        List<ScheduleOptionResponseDTO> rankedOptions = new ArrayList<>();

        for (List<CourseSection> combination : options) {
            List<String> insights = new ArrayList<>();
            double score = calculateTotalScore(combination, prefs, insights, courseRatings);
            rankedOptions.add(timetableMapper.toOptionDTO(combination, score, insights));
        }

        rankedOptions.sort(Comparator.comparing(ScheduleOptionResponseDTO::getScore).reversed());
        assignRankLabels(rankedOptions);

        return rankedOptions;
    }

    private double calculateTotalScore(
            List<CourseSection> sections,
            StudentSchedulePreference prefs,
            List<String> insights,
            Map<Long, Integer> courseRatings
    ) {
        double score = 100.0;
        Map<DayOfWeek, Integer> dailyDifficultyMap = new HashMap<>();
        boolean earlyMorningFound = false;
        Set<DayOfWeek> activeDays = new HashSet<>();

        for (CourseSection section : sections) {
            Long courseId = section.getCourse() != null ? section.getCourse().getId() : null;

            int difficulty = 3;
            if (courseId != null && courseRatings != null) {
                difficulty = courseRatings.getOrDefault(courseId, 3);
            }

            for (ClassSession session : section.getClassSessions()) {
                activeDays.add(session.getDayOfWeek());

                if (Boolean.TRUE.equals(prefs.getAvoidEarlyMorning()) && prefs.getEarliestStartTime() != null) {
                    if (session.getStartTime().isBefore(prefs.getEarliestStartTime())) {
                        score -= 5.0;
                        earlyMorningFound = true;
                    }
                }

                dailyDifficultyMap.merge(session.getDayOfWeek(), difficulty, Integer::sum);
            }
        }

        for (Map.Entry<DayOfWeek, Integer> entry : dailyDifficultyMap.entrySet()) {
            if (entry.getValue() >= 9) {
                score -= 10.0;
                insights.add("Heavy workload on " + entry.getKey());
            }
        }

        if (!earlyMorningFound && Boolean.TRUE.equals(prefs.getAvoidEarlyMorning())) {
            insights.add("No early morning classes! Perfect for you.");
        }

        if (activeDays.size() < 5) {
            insights.add("You have " + (5 - activeDays.size()) + " day(s) off!");
        }

        return Math.max(0, score);
    }

    private void assignRankLabels(List<ScheduleOptionResponseDTO> rankedOptions) {
        if (rankedOptions.isEmpty()) return;

        for (int i = 0; i < rankedOptions.size(); i++) {
            ScheduleOptionResponseDTO option = rankedOptions.get(i);
            if (i == 0 && option.getScore() >= 90) {
                option.setRankLabel("Best Match ✨");
            } else if (option.getScore() >= 80) {
                option.setRankLabel("Highly Recommended");
            } else if (option.getScore() < 60) {
                option.setRankLabel("Heavy Schedule");
            } else {
                option.setRankLabel("Balanced");
            }
        }
    }
}