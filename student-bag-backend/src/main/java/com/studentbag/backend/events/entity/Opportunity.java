package com.studentbag.backend.events.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "opportunities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Opportunity extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Event event;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "role_title")
    private String roleTitle;

    private String field;

    @Builder.Default
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid = false;

    @Column(name = "work_mode")
    private String workMode;

    @Column(name = "application_deadline")
    private LocalDate applicationDeadline;

    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Column(name = "application_url")
    private String applicationUrl;

    public boolean isApplicationOpen() {
        return applicationDeadline == null ||
                !LocalDate.now().isAfter(applicationDeadline);
    }
}