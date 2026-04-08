package com.studentbag.backend.schedule.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_schedule_preferences")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSchedulePreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, unique = true)
    private Student student;

    @Builder.Default
    @Column(name = "avoid_early_morning")
    private Boolean avoidEarlyMorning = false;

    @Column(name = "earliest_start_time")
    private LocalTime earliestStartTime;

    @Builder.Default
    @Column(name = "prefer_day_off")
    private Boolean preferDayOff = false;

    @Builder.Default
    @Column(name = "max_consecutive_hours_per_day")
    private Integer maxConsecutiveHoursPerDay = 4;

    @Builder.Default
    @Column(name = "max_gaps_per_day")
    private Integer maxGapsPerDay = 2;

    @Builder.Default
    @Column(name = "max_gap_minutes")
    private Integer maxGapMinutes = 90;

    @ElementCollection
    @CollectionTable(name = "preference_days_off",
            joinColumns = @JoinColumn(name = "preference_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    @Builder.Default
    private List<DayOfWeek> preferredDaysOff = new ArrayList<>();

    public boolean isTimePreferred(LocalTime time) {
        if (Boolean.FALSE.equals(avoidEarlyMorning) || earliestStartTime == null) return true;
        return !time.isBefore(earliestStartTime);
    }

    public boolean validateSchedule(List<ScheduleEntry> entries) {
        return entries.stream()
                .filter(e -> e.getStartDateTime() != null)
                .allMatch(e -> isTimePreferred(e.getStartDateTime().toLocalTime()));
    }
}