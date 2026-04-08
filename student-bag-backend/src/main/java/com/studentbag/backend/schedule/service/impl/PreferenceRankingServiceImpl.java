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

/**
 * Service to rank and score generated schedule combinations based on student preferences.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenceRankingServiceImpl implements PreferenceRankingService {

    private final StudentSchedulePreferenceRepository prefRepository;
    private final TimetableMapper timetableMapper;

    @Override
    public List<ScheduleOptionResponseDTO> rankAndScore(List<List<CourseSection>> options, Long studentId) {
        // 1. Fetch student preferences or use defaults
        StudentSchedulePreference prefs = prefRepository.findByStudentId(studentId)
                .orElse(new StudentSchedulePreference());

        List<ScheduleOptionResponseDTO> rankedOptions = new ArrayList<>();

        // 2. Score each combination
        for (List<CourseSection> combination : options) {
            List<String> insights = new ArrayList<>();
            double score = calculateTotalScore(combination, prefs, insights);

            // Mapping using TimetableMapper to include insights and logic
            rankedOptions.add(timetableMapper.toOptionDTO(combination, score, insights));
        }

        // 3. Sort from highest score to lowest
        rankedOptions.sort(Comparator.comparing(ScheduleOptionResponseDTO::getScore).reversed());

        // 4. Assign dynamic labels to the top results
        assignRankLabels(rankedOptions);

        return rankedOptions;
    }

    private double calculateTotalScore(List<CourseSection> sections, StudentSchedulePreference prefs, List<String> insights) {
        double score = 100.0;
        Map<DayOfWeek, Integer> dailyDifficultyMap = new HashMap<>();
        boolean earlyMorningFound = false;
        Set<DayOfWeek> activeDays = new HashSet<>();

        for (CourseSection section : sections) {
            // Assume default difficulty is 3 if not specified
            int difficulty = 3;

            for (ClassSession session : section.getClassSessions()) {
                activeDays.add(session.getDayOfWeek());

                // FR-4.5: Penalty for early morning sessions if student prefers to avoid them
                if (prefs.getAvoidEarlyMorning() && prefs.getEarliestStartTime() != null) {
                    if (session.getStartTime().isBefore(prefs.getEarliestStartTime())) {
                        score -= 5.0; // Deduct 5 points per early session
                        earlyMorningFound = true;
                    }
                }

                // Track workload per day for FR-4.6
                dailyDifficultyMap.merge(session.getDayOfWeek(), difficulty, Integer::sum);
            }
        }

        // FR-4.6: Difficulty Balancing Logic
        for (Map.Entry<DayOfWeek, Integer> entry : dailyDifficultyMap.entrySet()) {
            if (entry.getValue() >= 9) { // High difficulty threshold
                score -= 10.0;
                insights.add("Heavy workload on " + entry.getKey());
            }
        }

        // Positive insights for the UI
        if (!earlyMorningFound && prefs.getAvoidEarlyMorning()) {
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