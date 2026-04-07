package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "departments",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"external_id", "faculty_id"},
                        name = "uq_department_external_faculty"
                ),
                @UniqueConstraint(
                        columnNames = {"name_arabic", "faculty_id"},
                        name = "uq_department_name_arabic_faculty"
                )
        }
)
@Getter
@Setter
public class Department extends BaseEntity {

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "name_arabic", nullable = false)
    private String nameArabic;

    @Column(name = "name_english")
    private String nameEnglish;

    /**
     * لو البرنامج عندك فقط نص وما بدك Entity مستقل
     * ممكن نخزنه هون إذا كان القسم نفسه مرتبط ببرنامج ظاهر من ريتاج
     */
    @Column(name = "program_name_arabic")
    private String programNameArabic;

    @Column(name = "program_name_english")
    private String programNameEnglish;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Course> courses = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;
}