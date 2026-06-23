package com.studentbag.backend.events.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.EventStatus;
import com.studentbag.backend.domain.enums.courses.RegistrationStatus;
import com.studentbag.backend.domain.enums.schedule.EventType;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private String title;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EventStatus status = EventStatus.ACTIVE;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    private String location;
    private String department;
    private String host;

    @Builder.Default
    @Column(name = "requires_registration", nullable = false)
    private Boolean requiresRegistration = false;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Builder.Default
    @Column(name = "is_opportunity", nullable = false)
    private Boolean isOpportunity = false;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private Opportunity opportunity;

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventRegistration> registrations = new ArrayList<>();

    public void setOpportunity(Opportunity opportunity) {
        if (opportunity == null) {
            if (this.opportunity != null) {
                this.opportunity.setEvent(null);
            }
        } else {
            opportunity.setEvent(this);
        }

        this.opportunity = opportunity;
    }

    public void addRegistration(EventRegistration registration) {
        if (registration == null) {
            return;
        }

        if (registrations == null) {
            registrations = new ArrayList<>();
        }

        registrations.add(registration);
        registration.setEvent(this);
    }

    public boolean hasCapacity() {
        if (maxParticipants == null) {
            return true;
        }

        if (registrations == null || registrations.isEmpty()) {
            return maxParticipants > 0;
        }

        long currentCount = registrations.stream()
                .filter(registration ->
                        registration.getStatus() == RegistrationStatus.REGISTERED
                                || registration.getStatus() == RegistrationStatus.CHECKED_IN
                )
                .count();

        return currentCount < maxParticipants;
    }

    public boolean isUpcoming() {
        return startDateTime != null && LocalDateTime.now().isBefore(startDateTime);
    }

    public boolean isSoftDeleted() {
        return status == EventStatus.DELETED;
    }

    public boolean isCancelled() {
        return status == EventStatus.CANCELLED;
    }

    public boolean isFinished() {
        return status == EventStatus.FINISHED;
    }

    public boolean isActive() {
        return status == null || status == EventStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Event other)) {
            return false;
        }

        return getId() != null && getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
