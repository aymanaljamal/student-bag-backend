package com.studentbag.backend.notes.repository;

import com.studentbag.backend.notes.entity.NoteAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteAttachmentRepository extends JpaRepository<NoteAttachment, Long> {

    List<NoteAttachment> findByNoteId(Long noteId);
}