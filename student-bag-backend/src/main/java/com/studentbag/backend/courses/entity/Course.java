package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.AcademicLevel;
import com.studentbag.backend.institution.entity.Institution;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "courses",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"code", "institution_id"},
                        name = "uq_course_code_institution"
                )
        }
)
@Getter
@Setter
public class Course extends BaseEntity {

    /**
     * المعرف الخارجي من نظام ريتاج (زدنا الطول لـ 100 لتجنب خطأ Value too long)
     */
    @Column(name = "external_id", length = 100)
    private String externalId;

    /**
     * رمز المساق (مثل CHEM132)
     */
    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "name_arabic", nullable = false, length = 255)
    private String nameArabic;

    @Column(name = "name_english", length = 255)
    private String nameEnglish;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer creditHours;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AcademicLevel level;

    /**
     * اسم البرنامج (مثل بكالوريوس الكيمياء)
     */
    @Column(name = "program_name_arabic", length = 255)
    private String programNameArabic;

    @Column(name = "program_name_english", length = 255)
    private String programNameEnglish;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    /**
     * علاقة المساق بالشعب الدراسية
     */
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CourseSection> sections = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isActive = true;

    // مساعد لإضافة شعبة للمساق بسهولة
    public void addSection(CourseSection section) {
        sections.add(section);
        section.setCourse(this);
    }
}