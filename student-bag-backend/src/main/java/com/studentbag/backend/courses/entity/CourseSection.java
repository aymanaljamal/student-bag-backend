package com.studentbag.backend.courses.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.SectionType;
import com.studentbag.backend.instructor.entity.Instructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "course_sections",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"course_id", "term_id", "section_number", "section_type"},
                        name = "uq_section_course_term_number_type"
                )
        }
)
@Getter
@Setter
public class CourseSection extends BaseEntity {

    /**
     * تم رفع الطول لـ 500 لأن دمج (كود الكورس + الفصل + الرقم + النوع) قد يتجاوز الـ 50 والـ 100
     */
    @Column(name = "external_id", length = 500)
    private String externalId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "term_id", nullable = false)
    private Term term;

    /**
     * تم رفعه لـ 255 حرف تحسباً لأي نصوص إضافية في ملف ريتاج
     */
    @Column(name = "section_number", nullable = false, length = 255)
    private String sectionNumber;

    /**
     * تم رفعه لـ 255 حرف لضمان عدم حدوث خطأ character varying
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false, length = 255)
    private SectionType sectionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_lecture_section_id")
    private CourseSection parentLectureSection;

    @OneToMany(mappedBy = "parentLectureSection")
    private List<CourseSection> linkedLabs = new ArrayList<>();

    @Column(nullable = false)
    private Integer capacity = 0;

    @Column(nullable = false)
    private Integer enrolled = 0;

    @Column(nullable = false)
    private Boolean isOfficial = true;

    @OneToMany(mappedBy = "courseSection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassSession> classSessions = new ArrayList<>();

    public Integer getAvailableSeats() {
        if (capacity == null || enrolled == null) return 0;
        return Math.max(capacity - enrolled, 0);
    }

    public boolean isLecture() {
        return sectionType == SectionType.LECTURE;
    }

    public boolean isLab() {
        return sectionType == SectionType.LAB;
    }
}