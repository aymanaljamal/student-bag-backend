package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.CourseSectionRequest;
import com.studentbag.backend.courses.dto.response.CourseSectionResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class CourseSectionMapper {

    public CourseSection toEntity(
            CourseSectionRequest request,
            Course course,
            Term term,
            Instructor instructor
    ) {
        CourseSection section = new CourseSection();
        section.setCourse(course);
        section.setTerm(term);
        section.setSectionNumber(request.getSectionNumber());
        section.setInstructor(instructor);
        section.setCapacity(request.getCapacity());
        section.setEnrolled(request.getEnrolled() != null ? request.getEnrolled() : 0);
        section.setIsOfficial(request.getIsOfficial() != null ? request.getIsOfficial() : true);
        return section;
    }

    public void updateEntity(
            CourseSection section,
            CourseSectionRequest request,
            Course course,
            Term term,
            Instructor instructor
    ) {
        section.setCourse(course);
        section.setTerm(term);
        section.setSectionNumber(request.getSectionNumber());
        section.setInstructor(instructor);
        section.setCapacity(request.getCapacity());
        section.setEnrolled(request.getEnrolled() != null ? request.getEnrolled() : section.getEnrolled());
        section.setIsOfficial(request.getIsOfficial() != null ? request.getIsOfficial() : section.getIsOfficial());
    }

    public CourseSectionResponse toResponse(CourseSection section) {
        return CourseSectionResponse.builder()
                .id(section.getId())
                .courseId(section.getCourse() != null ? section.getCourse().getId() : null)
                .termId(section.getTerm() != null ? section.getTerm().getId() : null)
                .sectionNumber(section.getSectionNumber())
                .instructorId(section.getInstructor() != null ? section.getInstructor().getId() : null)
                .capacity(section.getCapacity())
                .enrolled(section.getEnrolled())
                .isOfficial(section.getIsOfficial())
                .createdAt(section.getCreatedAt())
                .updatedAt(section.getUpdatedAt())
                .build();
    }
}