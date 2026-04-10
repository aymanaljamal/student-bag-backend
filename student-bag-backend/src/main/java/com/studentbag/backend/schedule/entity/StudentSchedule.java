package com.studentbag.backend.schedule.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.ScheduleStatus;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleEntry> entries = new ArrayList<>();

    public void addEntry(ScheduleEntry entry) {
        entries.add(entry);
        entry.setSchedule(this);
    }

    public List<ScheduleEntry> getActiveEntries() {
        return entries.stream()
                .filter(e -> e.getIsAllDay() != null && !e.getIsAllDay())
                .toList();
    }
}