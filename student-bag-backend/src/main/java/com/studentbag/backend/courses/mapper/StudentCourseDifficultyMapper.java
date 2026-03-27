package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.StudentCourseDifficultyRequest;
import com.studentbag.backend.courses.dto.response.StudentCourseDifficultyResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.StudentCourseDifficulty;
import com.studentbag.backend.student.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentCourseDifficultyMapper {

    public StudentCourseDifficulty toEntity(
            StudentCourseDifficultyRequest request,
            Student student,
            Course course
    ) {
        StudentCourseDifficulty entity = new StudentCourseDifficulty();
        entity.setStudent(student);
        entity.setCourse(course);
        entity.setDifficulty(request.getDifficulty());
        entity.setNote(request.getNote());
        return entity;
    }

    public void updateEntity(
            StudentCourseDifficulty entity,
            StudentCourseDifficultyRequest request,
            Student student,
            Course course
    ) {
        entity.setStudent(student);
        entity.setCourse(course);
        entity.setDifficulty(request.getDifficulty());
        entity.setNote(request.getNote());
    }

    public StudentCourseDifficultyResponse toResponse(StudentCourseDifficulty entity) {
        return StudentCourseDifficultyResponse.builder()
                .id(entity.getId())
                .studentId(entity.getStudent() != null ? entity.getStudent().getId() : null)
                .courseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .difficulty(entity.getDifficulty())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}