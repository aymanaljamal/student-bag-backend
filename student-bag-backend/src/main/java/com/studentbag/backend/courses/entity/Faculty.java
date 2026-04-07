package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "faculties",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"external_id", "institution_id"},
                        name = "uq_faculty_external_institution"
                ),
                @UniqueConstraint(
                        columnNames = {"name_arabic", "institution_id"},
                        name = "uq_faculty_name_arabic_institution"
                )
        }
)
@Getter
@Setter
public class Faculty extends BaseEntity {

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "name_arabic", nullable = false)
    private String nameArabic;

    @Column(name = "name_english")
    private String nameEnglish;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @OneToMany(mappedBy = "faculty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Department> departments = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;
}