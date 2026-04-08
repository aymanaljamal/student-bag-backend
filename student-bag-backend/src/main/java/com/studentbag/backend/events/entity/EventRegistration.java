package com.studentbag.backend.events.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.RegistrationStatus;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_registrations",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"event_id", "student_id"},
                name = "uq_registration_event_student"
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    protected void onCreate() {
        if (this.registeredAt == null) {
            this.registeredAt = LocalDateTime.now();
        }
    }

    public void register() {
        this.status = RegistrationStatus.REGISTERED;
        this.registeredAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void checkIn() {
        this.status = RegistrationStatus.CHECKED_IN;
        this.checkedInAt = LocalDateTime.now();
    }
}