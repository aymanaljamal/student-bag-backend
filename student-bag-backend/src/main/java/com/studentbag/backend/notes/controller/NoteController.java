package com.studentbag.backend.notes.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.notes.dto.request.NoteAttachmentRequest;
import com.studentbag.backend.notes.dto.request.NoteRequest;
import com.studentbag.backend.notes.dto.response.NoteAttachmentResponse;
import com.studentbag.backend.notes.dto.response.NoteResponse;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.notes.mapper.NoteMapper;
import com.studentbag.backend.notes.service.NoteService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;
    private final StudentRepository studentRepository;
    private final CourseService courseService;

    @PostMapping
    public NoteResponse create(@Valid @RequestBody NoteRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getStudentId()
                ));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseService.getById(request.getCourseId());
        }

        Note note = noteMapper.toEntity(request, student, course);
        Note saved = noteService.create(note);

        return noteMapper.toResponse(saved, noteService.getAttachments(saved.getId()));
    }

    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable Long id, @Valid @RequestBody NoteRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getStudentId()
                ));

        Course course = null;
        if (request.getCourseId() != null) {
            course = courseService.getById(request.getCourseId());
        }

        Note existing = noteService.getById(id);
        noteMapper.updateEntity(existing, request, student, course);

        Note updated = noteService.update(id, existing);
        return noteMapper.toResponse(updated, noteService.getAttachments(updated.getId()));
    }

    @GetMapping("/{id}")
    public NoteResponse getById(@PathVariable Long id) {
        Note note = noteService.getById(id);
        return noteMapper.toResponse(note, noteService.getAttachments(id));
    }

    @GetMapping
    public List<NoteResponse> getAll() {
        return noteService.getAll()
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    @GetMapping("/student/{studentId}")
    public List<NoteResponse> getStudentNotes(@PathVariable Long studentId) {
        return noteService.getStudentNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    @GetMapping("/student/{studentId}/course/{courseId}")
    public List<NoteResponse> getStudentNotesByCourse(
            @PathVariable Long studentId,
            @PathVariable Long courseId
    ) {
        return noteService.getStudentNotesByCourse(studentId, courseId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    @GetMapping("/student/{studentId}/important")
    public List<NoteResponse> getImportantNotes(@PathVariable Long studentId) {
        return noteService.getImportantNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    @GetMapping("/student/{studentId}/search")
    public List<NoteResponse> searchByTitle(
            @PathVariable Long studentId,
            @RequestParam String title
    ) {
        return noteService.searchByTitle(studentId, title)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    @PatchMapping("/{id}/important")
    public NoteResponse markImportant(@PathVariable Long id) {
        Note note = noteService.markImportant(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    @PatchMapping("/{id}/archive")
    public NoteResponse archive(@PathVariable Long id) {
        Note note = noteService.archive(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    @DeleteMapping("/{id}")
    public void deletePermanently(@PathVariable Long id) {
        noteService.deletePermanently(id);
    }

    @PostMapping("/{noteId}/attachments")
    public NoteAttachmentResponse addAttachment(
            @PathVariable Long noteId,
            @Valid @RequestBody NoteAttachmentRequest request
    ) {
        Note note = noteService.getById(noteId);

        NoteAttachmentRequest normalizedRequest = request;
        normalizedRequest.setNoteId(noteId);

        NoteAttachment attachment = noteMapper.toAttachmentEntity(normalizedRequest, note);
        return noteMapper.toAttachmentResponse(noteService.addAttachment(attachment));
    }

    @GetMapping("/{noteId}/attachments")
    public List<NoteAttachmentResponse> getAttachments(@PathVariable Long noteId) {
        return noteService.getAttachments(noteId)
                .stream()
                .map(noteMapper::toAttachmentResponse)
                .toList();
    }

    @DeleteMapping("/attachments/{attachmentId}")
    public void deleteAttachment(@PathVariable Long attachmentId) {
        noteService.deleteAttachment(attachmentId);
    }
}