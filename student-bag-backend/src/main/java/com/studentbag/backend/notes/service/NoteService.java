package com.studentbag.backend.notes.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.domain.enums.NotePriority;
import com.studentbag.backend.domain.enums.NoteType;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.notes.repository.NoteAttachmentRepository;
import com.studentbag.backend.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteAttachmentRepository noteAttachmentRepository;

    // -------------------------------------------------------------------------
    // Core CRUD
    // -------------------------------------------------------------------------

    public Note create(Note note) {
        return noteRepository.save(note);
    }

    public Note update(Long noteId, Note updatedNote) {
        Note existingNote = getById(noteId);
        applyUpdates(existingNote, updatedNote);
        return noteRepository.save(existingNote);
    }

    @Transactional(readOnly = true)
    public Note getById(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + noteId));
    }

    @Transactional(readOnly = true)
    public List<Note> getAll() {
        return noteRepository.findAll();
    }

    // -------------------------------------------------------------------------
    // Student Note Views
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Note> getStudentNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<Note> getArchivedNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsArchivedTrueAndIsDeletedFalse(studentId);
    }

    @Transactional(readOnly = true)
    public List<Note> getDeletedNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsDeletedTrue(studentId);
    }

    @Transactional(readOnly = true)
    public List<Note> getStudentNotesByCourse(Long studentId, Long courseId) {
        return noteRepository.findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalse(studentId, courseId);
    }

    @Transactional(readOnly = true)
    public List<Note> getImportantNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsImportantTrueAndIsDeletedFalse(studentId);
    }

    @Transactional(readOnly = true)
    public List<Note> getPinnedNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsPinnedTrueAndIsDeletedFalse(studentId);
    }

    // -------------------------------------------------------------------------
    // Filters
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Note> getNotesByType(Long studentId, NoteType noteType) {
        return noteRepository.findByStudentIdAndNoteTypeAndIsDeletedFalse(studentId, noteType);
    }

    @Transactional(readOnly = true)
    public List<Note> getNotesByPriority(Long studentId, NotePriority priority) {
        return noteRepository.findByStudentIdAndPriorityAndIsDeletedFalse(studentId, priority);
    }

    @Transactional(readOnly = true)
    public List<Note> getNotesByColor(Long studentId, String color) {
        return noteRepository.findByStudentIdAndColorIgnoreCaseAndIsDeletedFalse(studentId, color);
    }

    @Transactional(readOnly = true)
    public List<Note> searchByTitle(Long studentId, String title) {
        return noteRepository.findByStudentIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(studentId, title);
    }

    // -------------------------------------------------------------------------
    // Note State Actions
    // -------------------------------------------------------------------------

    public Note markImportant(Long noteId) {
        return updateNoteState(noteId, Note::markImportant);
    }

    public Note pin(Long noteId) {
        return updateNoteState(noteId, Note::pin);
    }

    public Note unpin(Long noteId) {
        return updateNoteState(noteId, Note::unpin);
    }

    public Note archive(Long noteId) {
        return updateNoteState(noteId, Note::archive);
    }

    public Note softDelete(Long noteId) {
        return updateNoteState(noteId, Note::delete);
    }

    public void deletePermanently(Long noteId) {
        Note note = getById(noteId);
        noteRepository.delete(note);
    }

    // -------------------------------------------------------------------------
    // Attachments
    // -------------------------------------------------------------------------

    public NoteAttachment addAttachment(NoteAttachment attachment) {
        return noteAttachmentRepository.save(attachment);
    }

    @Transactional(readOnly = true)
    public List<NoteAttachment> getAttachments(Long noteId) {
        return noteAttachmentRepository.findByNoteId(noteId);
    }

    @Transactional(readOnly = true)
    public NoteAttachment getAttachmentById(Long attachmentId) {
        return noteAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note attachment not found with id: " + attachmentId));
    }

    public void deleteAttachment(Long attachmentId) {
        NoteAttachment attachment = getAttachmentById(attachmentId);
        noteAttachmentRepository.delete(attachment);
    }

    // -------------------------------------------------------------------------
    // Internal Helpers
    // -------------------------------------------------------------------------

    private void applyUpdates(Note existingNote, Note updatedNote) {
        existingNote.setStudent(updatedNote.getStudent());
        existingNote.setCourse(updatedNote.getCourse());
        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setContentHtml(updatedNote.getContentHtml());
        existingNote.setContentJson(updatedNote.getContentJson());
        existingNote.setIsImportant(updatedNote.getIsImportant());
        existingNote.setIsPinned(updatedNote.getIsPinned());
        existingNote.setIsArchived(updatedNote.getIsArchived());
        existingNote.setIsDeleted(updatedNote.getIsDeleted());
        existingNote.setPriority(updatedNote.getPriority());
        existingNote.setNoteType(updatedNote.getNoteType());
        existingNote.setColor(updatedNote.getColor());
        existingNote.setTags(updatedNote.getTags());
    }

    private Note updateNoteState(Long noteId, Consumer<Note> action) {
        Note note = getById(noteId);
        action.accept(note);
        return noteRepository.save(note);
    }
}