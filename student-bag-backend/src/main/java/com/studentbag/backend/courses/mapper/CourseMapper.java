package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.CourseRequest;
import com.studentbag.backend.courses.dto.response.CourseResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public Course toEntity(CourseRequest request, Institution institution) {
        Course course = new Course();
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCreditHours(request.getCreditHours());
        course.setLevel(request.getLevel());
        course.setInstitution(institution);
        course.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        return course;
    }

    public void updateEntity(Course course, CourseRequest request, Institution institution) {
        course.setCode(request.getCode());
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCreditHours(request.getCreditHours());
        course.setLevel(request.getLevel());
        course.setInstitution(institution);
        course.setIsActive(request.getIsActive() != null ? request.getIsActive() : course.getIsActive());
    }

    public CourseResponse toResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .code(course.getCode())
                .name(course.getName())
                .description(course.getDescription())
                .creditHours(course.getCreditHours())
                .level(course.getLevel())
                .institutionId(course.getInstitution() != null ? course.getInstitution().getId() : null)
                .isActive(course.getIsActive())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}