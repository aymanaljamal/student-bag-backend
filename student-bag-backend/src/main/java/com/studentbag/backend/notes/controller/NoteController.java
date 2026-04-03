package com.studentbag.backend.notes.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.domain.enums.NotePriority;
import com.studentbag.backend.domain.enums.NoteType;
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

/**
 * REST controller responsible for managing student notes and note attachments.
 *
 * <h2>Purpose</h2>
 * This controller exposes all APIs needed by the frontend to:
 * <ul>
 *     <li>Create a new note</li>
 *     <li>Update an existing note</li>
 *     <li>Read one note or multiple notes</li>
 *     <li>Filter notes by student, course, importance, pin state, archive state, type, priority, and color</li>
 *     <li>Search notes by title</li>
 *     <li>Mark notes as important</li>
 *     <li>Pin / unpin notes</li>
 *     <li>Archive notes</li>
 *     <li>Delete notes permanently</li>
 *     <li>Add / list / delete note attachments</li>
 * </ul>
 *
 * <h2>Frontend integration notes</h2>
 * Frontend developers should know the following:
 * <ul>
 *     <li>All note creation and update operations use {@link NoteRequest}</li>
 *     <li>All note read endpoints return {@link NoteResponse}</li>
 *     <li>Attachment endpoints return {@link NoteAttachmentResponse}</li>
 *     <li>Archived notes are intentionally separated from active notes</li>
 *     <li>Main active notes endpoint returns non-deleted and non-archived notes</li>
 *     <li>Pinned notes can be shown first in the UI</li>
 *     <li>Priority and type filters can be used to build chips, dropdowns, or tabs in Flutter</li>
 * </ul>
 *
 * <h2>Base URL</h2>
 * <pre>/api/notes</pre>
 */
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;
    private final StudentRepository studentRepository;
    private final CourseService courseService;

    /**
     * Creates a new note.
     *
     * <h3>What this endpoint does</h3>
     * Creates a note for a specific student, optionally linked to a course.
     *
     * <h3>Frontend usage</h3>
     * Use this when the user presses "Save Note" for a brand-new note.
     *
     * <h3>Required request body fields</h3>
     * <ul>
     *     <li>studentId</li>
     *     <li>title</li>
     *     <li>contentHtml</li>
     * </ul>
     *
     * <h3>Optional request body fields</h3>
     * <ul>
     *     <li>courseId</li>
     *     <li>isImportant</li>
     *     <li>isPinned</li>
     *     <li>isArchived</li>
     *     <li>isDeleted</li>
     *     <li>priority</li>
     *     <li>noteType</li>
     *     <li>color</li>
     *     <li>tags</li>
     * </ul>
     *
     * <h3>Response</h3>
     * Returns the created note including attachments list (usually empty at first).
     *
     * @param request request payload containing note data
     * @return created note response
     */
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

    /**
     * Updates an existing note.
     *
     * <h3>What this endpoint does</h3>
     * Updates all editable fields of an existing note.
     *
     * <h3>Frontend usage</h3>
     * Use this when the user edits a note and presses "Update" or "Save Changes".
     *
     * <h3>Important behavior</h3>
     * This endpoint:
     * <ul>
     *     <li>Loads the existing note by path variable id</li>
     *     <li>Loads the target student from request.studentId</li>
     *     <li>Loads the course if courseId exists</li>
     *     <li>Maps request fields into the existing entity</li>
     *     <li>Saves the updated entity</li>
     * </ul>
     *
     * <h3>Response</h3>
     * Returns the updated note with its attachments.
     *
     * @param id note id to update
     * @param request updated note data
     * @return updated note response
     */
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

        // We save the already-updated entity directly.
        // This works because create(note) internally delegates to repository.save(note).
        Note updated = noteService.create(existing);

        return noteMapper.toResponse(updated, noteService.getAttachments(updated.getId()));
    }

    /**
     * Gets a single note by id.
     *
     * <h3>Frontend usage</h3>
     * Use this when opening note details screen.
     *
     * <h3>Response</h3>
     * Returns the note and all of its attachments.
     *
     * @param id note id
     * @return note response
     */
    @GetMapping("/{id}")
    public NoteResponse getById(@PathVariable Long id) {
        Note note = noteService.getById(id);
        return noteMapper.toResponse(note, noteService.getAttachments(id));
    }

    /**
     * Gets all notes in the system.
     *
     * <h3>Warning</h3>
     * This endpoint is usually more useful for admin/testing purposes.
     * For student-facing UI, prefer student-specific endpoints.
     *
     * @return list of all notes
     */
    @GetMapping
    public List<NoteResponse> getAll() {
        return noteService.getAll()
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets active notes for a specific student.
     *
     * <h3>Expected behavior</h3>
     * This should normally return:
     * <ul>
     *     <li>non-deleted notes</li>
     *     <li>non-archived notes</li>
     *     <li>optionally sorted with pinned notes first, depending on service/repository implementation</li>
     * </ul>
     *
     * <h3>Frontend usage</h3>
     * This should be the main endpoint for the student's notes list screen.
     *
     * @param studentId student id
     * @return list of active notes for the student
     */
    @GetMapping("/student/{studentId}")
    public List<NoteResponse> getStudentNotes(@PathVariable Long studentId) {
        return noteService.getStudentNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets archived notes for a specific student.
     *
     * <h3>Frontend usage</h3>
     * Use this for archive tab / archive screen.
     *
     * @param studentId student id
     * @return archived notes
     */
    @GetMapping("/student/{studentId}/archived")
    public List<NoteResponse> getArchivedNotes(@PathVariable Long studentId) {
        return noteService.getArchivedNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets notes for a student limited to a single course.
     *
     * <h3>Frontend usage</h3>
     * Use when user filters notes by selected course.
     *
     * @param studentId student id
     * @param courseId course id
     * @return notes matching the student and course
     */
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

    /**
     * Gets important notes for a specific student.
     *
     * <h3>Frontend usage</h3>
     * Use this for "Important" filter/tab.
     *
     * @param studentId student id
     * @return important notes
     */
    @GetMapping("/student/{studentId}/important")
    public List<NoteResponse> getImportantNotes(@PathVariable Long studentId) {
        return noteService.getImportantNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets pinned notes for a specific student.
     *
     * <h3>Frontend usage</h3>
     * Useful for a dedicated pinned section or quick-access UI.
     *
     * @param studentId student id
     * @return pinned notes
     */
    @GetMapping("/student/{studentId}/pinned")
    public List<NoteResponse> getPinnedNotes(@PathVariable Long studentId) {
        return noteService.getPinnedNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets student notes filtered by note type.
     *
     * <h3>Example frontend request</h3>
     * <pre>GET /api/notes/student/5/type?type=HOMEWORK</pre>
     *
     * @param studentId student id
     * @param type note type enum value
     * @return notes filtered by type
     */
    @GetMapping("/student/{studentId}/type")
    public List<NoteResponse> getByType(
            @PathVariable Long studentId,
            @RequestParam NoteType type
    ) {
        return noteService.getNotesByType(studentId, type)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets student notes filtered by priority.
     *
     * <h3>Example frontend request</h3>
     * <pre>GET /api/notes/student/5/priority?priority=HIGH</pre>
     *
     * @param studentId student id
     * @param priority note priority enum value
     * @return notes filtered by priority
     */
    @GetMapping("/student/{studentId}/priority")
    public List<NoteResponse> getByPriority(
            @PathVariable Long studentId,
            @RequestParam NotePriority priority
    ) {
        return noteService.getNotesByPriority(studentId, priority)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Gets student notes filtered by color.
     *
     * <h3>Example frontend request</h3>
     * <pre>GET /api/notes/student/5/color?color=yellow</pre>
     *
     * @param studentId student id
     * @param color note color string
     * @return notes filtered by color
     */
    @GetMapping("/student/{studentId}/color")
    public List<NoteResponse> getByColor(
            @PathVariable Long studentId,
            @RequestParam String color
    ) {
        return noteService.getNotesByColor(studentId, color)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }

    /**
     * Searches student notes by title.
     *
     * <h3>Frontend usage</h3>
     * Use this for search bar input.
     *
     * <h3>Example</h3>
     * <pre>GET /api/notes/student/5/search?title=math</pre>
     *
     * @param studentId student id
     * @param title title keyword
     * @return notes whose title contains the given keyword
     */
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

    /**
     * Marks a note as important.
     *
     * <h3>Frontend usage</h3>
     * Use this when user toggles a note into the important state.
     *
     * @param id note id
     * @return updated note response
     */
    @PatchMapping("/{id}/important")
    public NoteResponse markImportant(@PathVariable Long id) {
        Note note = noteService.markImportant(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    /**
     * Pins a note.
     *
     * <h3>Frontend usage</h3>
     * Use this when the user chooses "Pin".
     *
     * @param id note id
     * @return updated note response
     */
    @PatchMapping("/{id}/pin")
    public NoteResponse pin(@PathVariable Long id) {
        Note note = noteService.pin(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    /**
     * Unpins a note.
     *
     * <h3>Frontend usage</h3>
     * Use this when the user removes a note from pinned state.
     *
     * @param id note id
     * @return updated note response
     */
    @PatchMapping("/{id}/unpin")
    public NoteResponse unpin(@PathVariable Long id) {
        Note note = noteService.unpin(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    /**
     * Archives a note.
     *
     * <h3>Frontend usage</h3>
     * Use this instead of hard delete when the product wants recoverable hidden notes.
     *
     * @param id note id
     * @return updated note response
     */
    @PatchMapping("/{id}/archive")
    public NoteResponse archive(@PathVariable Long id) {
        Note note = noteService.archive(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    /**
     * Soft deletes a note.
     *
     * <h3>Behavior</h3>
     * Soft delete means:
     * <ul>
     *     <li>the note stays in the database</li>
     *     <li>the note is flagged as deleted</li>
     *     <li>normal active-note endpoints should stop returning it</li>
     * </ul>
     *
     * <h3>Frontend usage</h3>
     * Use this if your UI has a trash behavior and you do not want permanent removal immediately.
     *
     * @param id note id
     * @return updated note response
     */
    @PatchMapping("/{id}/soft-delete")
    public NoteResponse softDelete(@PathVariable Long id) {
        Note note = noteService.softDelete(id);
        return noteMapper.toResponse(note, noteService.getAttachments(note.getId()));
    }

    /**
     * Permanently deletes a note from the database.
     *
     * <h3>Warning</h3>
     * This is irreversible.
     *
     * <h3>Frontend usage</h3>
     * Use this only if product requirements really need permanent deletion.
     *
     * @param id note id
     */
    @DeleteMapping("/{id}")
    public void deletePermanently(@PathVariable Long id) {
        noteService.deletePermanently(id);
    }

    /**
     * Adds an attachment to a note.
     *
     * <h3>Supported use cases</h3>
     * Attachment can represent:
     * <ul>
     *     <li>image</li>
     *     <li>pdf</li>
     *     <li>audio</li>
     *     <li>document</li>
     *     <li>other file types supported by the frontend/backend workflow</li>
     * </ul>
     *
     * <h3>Frontend usage</h3>
     * Upload the file first to storage, obtain its URL, then call this endpoint with metadata.
     *
     * @param noteId note id
     * @param request attachment request payload
     * @return created attachment response
     */
    @PostMapping("/{noteId}/attachments")
    public NoteAttachmentResponse addAttachment(
            @PathVariable Long noteId,
            @Valid @RequestBody NoteAttachmentRequest request
    ) {
        Note note = noteService.getById(noteId);
        NoteAttachment attachment = noteMapper.toAttachmentEntity(request, note);
        return noteMapper.toAttachmentResponse(noteService.addAttachment(attachment));
    }

    /**
     * Gets all attachments for a note.
     *
     * <h3>Frontend usage</h3>
     * Use this to display attachment list below note details.
     *
     * @param noteId note id
     * @return list of attachments
     */
    @GetMapping("/{noteId}/attachments")
    public List<NoteAttachmentResponse> getAttachments(@PathVariable Long noteId) {
        return noteService.getAttachments(noteId)
                .stream()
                .map(noteMapper::toAttachmentResponse)
                .toList();
    }
    @GetMapping("/student/{studentId}/deleted")
    public List<NoteResponse> getDeletedNotes(@PathVariable Long studentId) {
        return noteService.getDeletedNotes(studentId)
                .stream()
                .map(note -> noteMapper.toResponse(note, noteService.getAttachments(note.getId())))
                .toList();
    }
    /**
     * Deletes a single attachment by attachment id.
     *
     * <h3>Frontend usage</h3>
     * Use this when user removes a file from a note.
     *
     * @param attachmentId attachment id
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public void deleteAttachment(@PathVariable Long attachmentId) {
        noteService.deleteAttachment(attachmentId);
    }
}