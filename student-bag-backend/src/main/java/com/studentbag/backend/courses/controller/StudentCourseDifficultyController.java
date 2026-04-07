package com.studentbag.backend.courses.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.dto.request.StudentCourseDifficultyRequest;
import com.studentbag.backend.courses.dto.response.StudentCourseDifficultyResponse;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.StudentCourseDifficulty;
import com.studentbag.backend.courses.mapper.StudentCourseDifficultyMapper;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.courses.service.StudentCourseDifficultyService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-difficulties")
@RequiredArgsConstructor
public class StudentCourseDifficultyController {

}