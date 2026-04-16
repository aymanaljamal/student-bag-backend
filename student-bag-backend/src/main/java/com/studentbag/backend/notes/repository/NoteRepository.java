package com.studentbag.backend.notes.repository;

import com.studentbag.backend.domain.enums.notes.NotePriority;
import com.studentbag.backend.domain.enums.notes.NoteType;
import com.studentbag.backend.notes.entity.Note;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByStudentIdAndIsDeletedFalse(Long studentId);

    List<Note> findByStudentIdAndIsDeletedFalseAndIsArchivedFalse(Long studentId);

    List<Note> findByStudentIdAndCourseIdAndIsDeletedFalse(Long studentId, Long courseId);

    List<Note> findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalse(Long studentId, Long courseId);

    List<Note> findByStudentIdAndIsImportantTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByStudentIdAndIsPinnedTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByStudentIdAndNoteTypeAndIsDeletedFalse(Long studentId, NoteType noteType);

    List<Note> findByStudentIdAndPriorityAndIsDeletedFalse(Long studentId, NotePriority priority);

    List<Note> findByStudentIdAndColorIgnoreCaseAndIsDeletedFalse(Long studentId, String color);

    List<Note> findByStudentIdAndIsArchivedTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByCourseIdAndIsDeletedFalse(Long courseId);
    List<Note> findByStudentIdAndIsDeletedTrue(Long studentId);
    List<Note> findByStudentIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(Long studentId, String title);

    List<Note> findByStudentIdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Long studentId);

    List<Note> findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(Long studentId);

    List<Note> findByStudentIdAndIsDeletedFalseOrderByPriorityDescCreatedAtDesc(Long studentId);
}