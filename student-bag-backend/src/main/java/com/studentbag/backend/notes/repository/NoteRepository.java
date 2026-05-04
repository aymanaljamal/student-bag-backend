package com.studentbag.backend.notes.repository;

import com.studentbag.backend.domain.enums.notes.NotePriority;
import com.studentbag.backend.domain.enums.notes.NoteType;
import com.studentbag.backend.notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByStudent_IdAndIsDeletedFalse(Long studentId);

    List<Note> findByStudent_IdAndIsDeletedFalseAndIsArchivedFalse(Long studentId);

    List<Note> findByStudent_IdAndCourse_IdAndIsDeletedFalse(Long studentId, Long courseId);

    List<Note> findByStudent_IdAndCourse_IdAndIsDeletedFalseAndIsArchivedFalse(Long studentId, Long courseId);

    List<Note> findByStudent_IdAndIsImportantTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByStudent_IdAndIsPinnedTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByStudent_IdAndNoteTypeAndIsDeletedFalse(Long studentId, NoteType noteType);

    List<Note> findByStudent_IdAndPriorityAndIsDeletedFalse(Long studentId, NotePriority priority);

    List<Note> findByStudent_IdAndColorIgnoreCaseAndIsDeletedFalse(Long studentId, String color);

    List<Note> findByStudent_IdAndIsArchivedTrueAndIsDeletedFalse(Long studentId);

    List<Note> findByCourse_IdAndIsDeletedFalse(Long courseId);

    List<Note> findByStudent_IdAndIsDeletedTrue(Long studentId);

    List<Note> findByStudent_IdAndTitleContainingIgnoreCaseAndIsDeletedFalse(
            Long studentId,
            String title
    );

    List<Note> findByStudent_IdAndIsDeletedFalseOrderByIsPinnedDescCreatedAtDesc(Long studentId);

    List<Note> findByStudent_IdAndIsDeletedFalseAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(
            Long studentId
    );

    List<Note> findByStudent_IdAndIsDeletedFalseOrderByPriorityDescCreatedAtDesc(Long studentId);
}