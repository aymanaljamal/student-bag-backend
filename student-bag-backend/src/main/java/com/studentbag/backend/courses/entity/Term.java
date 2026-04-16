package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.courses.Season;
import com.studentbag.backend.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(
        name = "terms",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"term_code", "institution_id"},
                        name = "uq_term_code_institution"
                )
        }
)
@Getter
@Setter
public class Term extends BaseEntity {

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "term_code", nullable = false, length = 100)
    private String termCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String academicYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Season season;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    private Boolean isCurrent = false;

    public boolean isActive(LocalDate date) {
        return (date.isEqual(startDate) || date.isAfter(startDate))
                && (date.isEqual(endDate) || date.isBefore(endDate));
    }

    public int getDurationWeeks() {
        return Math.max(0, (int) ((endDate.toEpochDay() - startDate.toEpochDay()) / 7));
    }
}