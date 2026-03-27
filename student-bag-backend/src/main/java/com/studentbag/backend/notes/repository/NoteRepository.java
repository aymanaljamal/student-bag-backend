package com.studentbag.backend.notes.repository;

import com.studentbag.backend.notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByStudentIdAndIsDeletedFalse(Long studentId);

    List<Note> findByStudentIdAndCourseIdAndIsDeletedFalse(Long studentId, Long courseId);

    List<Note> findByStudentIdAndIsImportantTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByCourseIdAndIsDeletedFalse(Long courseId);

    List<Note> findByStudentIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(Long studentId, String title);
}