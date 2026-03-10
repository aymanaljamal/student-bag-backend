package com.studentbag.backend.instructor.repository;


import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstructorRepository extends JpaRepository<Instructor, Long> {
}