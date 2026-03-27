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
        existing.setIsImportant(updatedNote.getIsImportant());
        existing.setIsDeleted(updatedNote.getIsDeleted());
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
        return noteRepository.findByStudentIdAndIsDeletedFalse(studentId);
    }

    public List<Note> getStudentNotesByCourse(Long studentId, Long courseId) {
        return noteRepository.findByStudentIdAndCourseIdAndIsDeletedFalse(studentId, courseId);
    }

    public List<Note> getImportantNotes(Long studentId) {
        return noteRepository.findByStudentIdAndIsImportantTrueAndIsDeletedFalse(studentId);
    }

    public List<Note> searchByTitle(Long studentId, String title) {
        return noteRepository.findByStudentIdAndTitleContainingIgnoreCaseAndIsDeletedFalse(studentId, title);
    }

    public Note markImportant(Long noteId) {
        Note note = getById(noteId);
        note.markImportant();
        return noteRepository.save(note);
    }

    public Note archive(Long noteId) {
        Note note = getById(noteId);
        note.archive();
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

    public NoteAttachment getAttachmentById(Long attachmentId) {
        return noteAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Note attachment not found with id: " + attachmentId));
    }

    public void deleteAttachment(Long attachmentId) {
        NoteAttachment attachment = getAttachmentById(attachmentId);
        noteAttachmentRepository.delete(attachment);
    }
}