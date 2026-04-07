package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "class_sessions")
@Getter
@Setter
public class ClassSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_section_id", nullable = false)
    private CourseSection courseSection;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    private String room;

    private String building;

    private String campus;

    @Column(nullable = false)
    private Boolean isOnline = false;

    public int getDurationMinutes() {
        return (endTime.getHour() * 60 + endTime.getMinute())
                - (startTime.getHour() * 60 + startTime.getMinute());
    }
}