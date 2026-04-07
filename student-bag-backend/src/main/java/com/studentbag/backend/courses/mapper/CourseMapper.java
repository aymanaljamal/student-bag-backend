package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.CourseRequestDTO;
import com.studentbag.backend.courses.dto.response.CourseResponseDTO;
import com.studentbag.backend.courses.dto.response.CourseSectionSimpleDTO;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Mapper for Course entity
 */
@Component
public class CourseMapper {

    /**
     * Map request DTO to entity (create/update)
     */
    public void toEntity(CourseRequestDTO request,
                         Course course,
                         Institution institution,
                         Department department) {

        course.setExternalId(request.getExternalId());
        course.setCode(request.getCode());
        course.setNameArabic(request.getNameArabic());
        course.setNameEnglish(request.getNameEnglish());
        course.setDescription(request.getDescription());
        course.setCreditHours(request.getCreditHours());
        course.setLevel(request.getLevel());
        course.setProgramNameArabic(request.getProgramNameArabic());
        course.setProgramNameEnglish(request.getProgramNameEnglish());
        course.setInstitution(institution);
        course.setDepartment(department);
        course.setIsActive(request.getIsActive());
    }

    /**
     * Basic response (بدون sections)
     */
    public CourseResponseDTO toResponse(Course course) {
        return toResponse(course, false);
    }

    /**
     * Advanced response (مع تحكم بالـ sections)
     */
    public CourseResponseDTO toResponse(Course course, boolean includeSections) {

        return CourseResponseDTO.builder()
                .id(course.getId())
                .externalId(course.getExternalId())
                .code(course.getCode())
                .nameArabic(course.getNameArabic())
                .nameEnglish(course.getNameEnglish())
                .description(course.getDescription())
                .creditHours(course.getCreditHours())
                .level(course.getLevel())
                .programNameArabic(course.getProgramNameArabic())
                .programNameEnglish(course.getProgramNameEnglish())
                .institutionId(course.getInstitution().getId())
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .isActive(course.getIsActive())
                .sections(includeSections ? mapSections(course) : null)
                .build();
    }

    /**
     * Map sections safely
     */
    private List<CourseSectionSimpleDTO> mapSections(Course course) {

        if (course.getSections() == null) return List.of();

        return course.getSections().stream()
                .map(section -> CourseSectionSimpleDTO.builder()
                        .id(section.getId())
                        .sectionNumber(section.getSectionNumber())
                        .sectionType(section.getSectionType().name())
                        .build()
                )
                .toList();
    }
}