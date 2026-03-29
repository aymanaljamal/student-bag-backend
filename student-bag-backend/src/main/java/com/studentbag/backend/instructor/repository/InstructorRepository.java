package com.studentbag.backend.instructor.repository;


import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface InstructorRepository extends JpaRepository<Instructor, Long> {

    Optional<Instructor> findByUser_Id(UUID userId);
}

