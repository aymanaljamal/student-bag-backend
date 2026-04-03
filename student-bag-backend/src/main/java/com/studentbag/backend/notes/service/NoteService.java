package com.studentbag.backend.notes.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.notes.repository.NoteAttachmentRepository;
import com.studentbag.backend.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteAttachmentRepository noteAttachmentRepository;

    public Note create(Note note) {
        return noteRepository.save(note);
    }

    public Note update(Long id, Note updatedNote) {
        Note existing = getById(id);

        existing.setStudent(updatedNote.getStudent());
        existing.setCourse(updatedNote.getCourse());
        existing.setTitle(updatedNote.getTitle());
        existing.setContentHtml(updatedNote.getContentHtml());
        existing.setContentJson(updatedNote.getContentJson());
        existing.setIsImportant(updatedNote.getIsImportant());
        existing.setIsPinned(updatedNote.getIsPinned());
        existing.setIsArchived(updatedNote.getIsArchived());
        existing.setIsDeleted(updatedNote.getIsDeleted());
        existing.setPriority(updatedNote.getPriority());
        existing.setNoteType(updatedNote.getNoteType());
        existing.setColor(updatedNote.getColor());
        existing.setTags(updatedNote.getTags());

        return noteRepository.save(existing);
    }

    public Note getById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id: " + id));
    }

    public List<Note> getAll() {
        return noteRepository.findAll();
    }

    public List<Note> getStudentNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsDeletedFalseAndIsArchivedFalseOrderByIsPinnedDescCreatedAtDesc(studentId);
    }

    public List<Note> getArchivedNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsArchivedTrueAndIsDeletedFalse(studentId);
    }

    public List<Note> getStudentNotesByCourse(Long studentId, Long courseId) {
        return noteRepository.findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalse(studentId, courseId);
    }

    public List<Note> getImportantNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsImportantTrueAndIsDeletedFalse(studentId);
    }

    public List<Note> getPinnedNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsPinnedTrueAndIsDeletedFalse(studentId);
    }

    public List<Note> getNotesByType(Long studentId, com.studentbag.backend.domain.enums.NoteType noteType) {
        return noteRepository.findByStudentIdAndNoteTypeAndIsDeletedFalse(studentId, noteType);
    }

    public List<Note> getNotesByPriority(Long studentId, com.studentbag.backend.domain.enums.NotePriority priority) {
        return noteRepository.findByStudentIdAndPriorityAndIsDeletedFalse(studentId, priority);
    }

    public List<Note> getNotesByColor(Long studentId, String color) {
        return noteRepository.findByStudentIdAndColorIgnoreCaseAndIsDeletedFalse(studentId, color);
    }

    public List<Note> searchByTitle(Long studentId, String title) {
        return noteRepository.findByStudentIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(studentId, title);
    }

    public Note markImportant(Long noteId) {
        Note note = getById(noteId);
        note.markImportant();
        return noteRepository.save(note);
    }

    public Note pin(Long noteId) {
        Note note = getById(noteId);
        note.pin();
        return noteRepository.save(note);
    }

    public Note unpin(Long noteId) {
        Note note = getById(noteId);
        note.unpin();
        return noteRepository.save(note);
    }

    public Note archive(Long noteId) {
        Note note = getById(noteId);
        note.archive();
        return noteRepository.save(note);
    }

    public Note softDelete(Long noteId) {
        Note note = getById(noteId);
        note.delete();
        return noteRepository.save(note);
    }

    public void deletePermanently(Long noteId) {
        Note note = getById(noteId);
        noteRepository.delete(note);
    }

    public NoteAttachment addAttachment(NoteAttachment attachment) {
        return noteAttachmentRepository.save(attachment);
    }

    public List<NoteAttachment> getAttachments(Long noteId) {
        return noteAttachmentRepository.findByNoteId(noteId);
    }
    public List<Note> getDeletedNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsDeletedTrue(studentId);
    }
    public NoteAttachment getAttachmentById(Long attachmentId) {
        return noteAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note attachment not found with id: " + attachmentId));
    }

    public void deleteAttachment(Long attachmentId) {
        NoteAttachment attachment = getAttachmentById(attachmentId);
        noteAttachmentRepository.delete(attachment);
    }
}