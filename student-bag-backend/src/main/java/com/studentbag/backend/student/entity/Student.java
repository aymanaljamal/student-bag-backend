package com.studentbag.backend.student.entity;

import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.users.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private String academicLevel;
    private String schoolGrade;
    private String universityMajor;

    @ManyToOne
    @JoinColumn(name = "institution_id")
    private Institution institution;

    private boolean gpaVisibleToParents = true;
}