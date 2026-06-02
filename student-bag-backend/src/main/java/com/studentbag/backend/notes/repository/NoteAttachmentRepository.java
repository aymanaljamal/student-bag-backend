package com.studentbag.backend.notes.repository;

import com.studentbag.backend.notes.entity.NoteAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoteAttachmentRepository extends JpaRepository<NoteAttachment, Long> {

    List<NoteAttachment> findByNoteId(Long noteId);

    @Query("""
            select a
            from NoteAttachment a
            join fetch a.note n
            where n.student.id = :studentId
              and n.isDeleted = false
              and n.isArchived = false
            order by a.createdAt desc
            """)
    List<NoteAttachment> findByStudentIdForAi(@Param("studentId") Long studentId);
}