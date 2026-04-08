package com.studentbag.backend.schedule.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.ScheduleSourceType;
import com.studentbag.backend.events.entity.Event;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedule_entries")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "schedule_id", nullable = false)
    private StudentSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private ScheduleSourceType sourceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_section_id")
    private CourseSection courseSection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private String title;

    private String description;
    private String location;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Builder.Default
    @Column(name = "is_all_day", nullable = false)
    private Boolean isAllDay = false;

    @Column(name = "color_hex")
    private String colorHex;

    @Builder.Default
    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked = false;

    public boolean conflictsWith(ScheduleEntry other) {
        if (Boolean.TRUE.equals(this.isAllDay) || Boolean.TRUE.equals(other.isAllDay)) return false;
        return this.startDateTime.isBefore(other.endDateTime)
                && other.startDateTime.isBefore(this.endDateTime);
    }

    public int getDurationMinutes() {
        return (int) Duration.between(startDateTime, endDateTime).toMinutes();
    }
}