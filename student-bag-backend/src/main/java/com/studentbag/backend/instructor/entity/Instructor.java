package com.studentbag.backend.instructor.entity;

import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "instructors",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"external_id", "institution_id"},
                        name = "uq_instructor_external_institution"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instructor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(nullable = false)
    private String fullNameArabic;

    private String fullNameEnglish;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountConfirmed = false;
}