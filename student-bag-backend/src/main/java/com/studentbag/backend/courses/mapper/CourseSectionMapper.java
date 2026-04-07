package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.CourseSectionRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseSectionResponseDTO;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for CourseSection
 */
@Component
public class CourseSectionMapper {

    public void toEntity(CourseSectionRequestDTO request,
                         CourseSection section,
                         Course course,
                         Term term,
                         Instructor instructor,
                         CourseSection parentLecture) {

        section.setExternalId(request.getExternalId());
        section.setCourse(course);
        section.setTerm(term);
        section.setSectionNumber(request.getSectionNumber());
        section.setSectionType(request.getSectionType());
        section.setInstructor(instructor);
        section.setParentLectureSection(parentLecture);
        section.setCapacity(request.getCapacity());
        section.setEnrolled(request.getEnrolled());
        section.setIsOfficial(request.getIsOfficial());
    }

    public CourseSectionResponseDTO toResponse(CourseSection section) {
        return CourseSectionResponseDTO.builder()
                .id(section.getId())
                .externalId(section.getExternalId())
                .courseId(section.getCourse().getId())
                .termId(section.getTerm().getId())
                .sectionNumber(section.getSectionNumber())
                .sectionType(section.getSectionType())
                .instructorId(section.getInstructor() != null ? section.getInstructor().getId() : null)
                .parentLectureSectionId(
                        section.getParentLectureSection() != null
                                ? section.getParentLectureSection().getId()
                                : null
                )
                .capacity(section.getCapacity())
                .enrolled(section.getEnrolled())
                .availableSeats(section.getAvailableSeats())
                .isOfficial(section.getIsOfficial())
                .build();
    }
}