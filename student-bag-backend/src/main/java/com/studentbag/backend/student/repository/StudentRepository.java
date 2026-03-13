package com.studentbag.backend.student.repository;

import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByUser_Id(UUID userId);
}