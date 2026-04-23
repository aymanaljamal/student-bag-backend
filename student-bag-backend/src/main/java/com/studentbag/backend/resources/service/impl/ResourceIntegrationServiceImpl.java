package com.studentbag.backend.resources.service.impl;

import com.studentbag.backend.domain.enums.tasks.TaskStatus;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.entity.NoteAttachment;
import com.studentbag.backend.notes.repository.NoteAttachmentRepository;
import com.studentbag.backend.notes.repository.NoteRepository;
import com.studentbag.backend.resources.dto.response.*;
import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import com.studentbag.backend.resources.mapper.PersonalResourceFolderMapper;
import com.studentbag.backend.resources.mapper.PersonalResourceItemMapper;
import com.studentbag.backend.resources.mapper.ResourceIntegrationMapper;
import com.studentbag.backend.resources.repository.PersonalResourceFolderRepository;
import com.studentbag.backend.resources.repository.PersonalResourceItemRepository;
import com.studentbag.backend.resources.service.ResourceIntegrationService;
import com.studentbag.backend.schedule.dto.response.ActiveScheduleCourseDTO;
import com.studentbag.backend.schedule.service.ScheduleManagementService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.tasks.entity.Task;
import com.studentbag.backend.tasks.repository.TaskRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of {@link ResourceIntegrationService}.
 *
 * <p>This service composes Resource Hub data with:
 * <ul>
 *     <li>Active schedule courses</li>
 *     <li>Linked notes by course</li>
 *     <li>Linked tasks by course</li>
 *     <li>Folder details screen payload</li>
 * </ul>
 *
 * <p>Business rules:
 * <ul>
 *     <li>Linked notes must not be deleted or archived</li>
 *     <li>Linked tasks must not be deleted, archived, or completed</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResourceIntegrationServiceImpl implements ResourceIntegrationService {

    private final ScheduleManagementService scheduleManagementService;
    private final PersonalResourceFolderRepository folderRepository;
    private final PersonalResourceItemRepository itemRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final NoteRepository noteRepository;
    private final NoteAttachmentRepository noteAttachmentRepository;
    private final TaskRepository taskRepository;

    @Override
    public List<ResourceCourseSummaryResponse> getActiveScheduleCoursesForLibrary(
            UUID currentUserId,
            Long termId
    ) {
        Student student = getStudentByUserId(currentUserId);

        List<ActiveScheduleCourseDTO> activeCourses =
                scheduleManagementService.getActiveScheduleCourses(student.getId(), termId);

        return activeCourses.stream()
                .map(course -> ResourceCourseSummaryResponse.builder()
                        .id(course.getId())
                        .externalId(course.getExternalId())
                        .code(course.getCode())
                        .nameArabic(course.getNameArabic())
                        .nameEnglish(course.getNameEnglish())
                        .description(course.getDescription())
                        .creditHours(course.getCreditHours())
                        .build())
                .toList();
    }

    @Override
    public List<LinkedNoteSummaryResponse> getLinkedNotesByCourse(
            UUID currentUserId,
            Long courseId
    ) {
        Student student = getStudentByUserId(currentUserId);

        List<Note> notes = noteRepository
                .findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalse(student.getId(), courseId);

        return notes.stream()
                .map(note -> {
                    List<NoteAttachment> attachments = noteAttachmentRepository.findByNoteId(note.getId());
                    return ResourceIntegrationMapper.toLinkedNoteResponse(note, attachments);
                })
                .toList();
    }

    @Override
    public List<LinkedTaskSummaryResponse> getLinkedTasksByCourse(
            UUID currentUserId,
            Long courseId
    ) {
        Student student = getStudentByUserId(currentUserId);

        List<Task> tasks = taskRepository
                .findByStudentIdAndCourseIdAndIsDeletedFalse(student.getId(), courseId)
                .stream()
                .filter(task -> !Boolean.TRUE.equals(task.getArchived()))
                .filter(task -> task.getStatus() != TaskStatus.COMPLETED)
                .toList();

        return tasks.stream()
                .map(ResourceIntegrationMapper::toLinkedTaskResponse)
                .toList();
    }

    @Override
    public PersonalResourceFolderDetailsResponse buildCourseFolderDetails(
            Long folderId,
            UUID currentUserId
    ) {
        Student student = getStudentByUserId(currentUserId);

        PersonalResourceFolder folder = folderRepository.findByIdAndStudentIdAndIsDeletedFalse(folderId, student.getId())
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));

        List<PersonalResourceFolderResponse> childFolders = folderRepository
                .findByStudentIdAndParentFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(student.getId(), folder.getId())
                .stream()
                .map(PersonalResourceFolderMapper::toResponse)
                .toList();

        List<PersonalResourceItemResponse> items = itemRepository
                .findByStudentIdAndFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(student.getId(), folder.getId())
                .stream()
                .map(PersonalResourceItemMapper::toResponse)
                .toList();

        List<LinkedNoteSummaryResponse> linkedNotes =
                Boolean.TRUE.equals(folder.getShowLinkedNotes()) && folder.getCourse() != null
                        ? getLinkedNotesByCourse(currentUserId, folder.getCourse().getId())
                        : List.of();

        List<LinkedTaskSummaryResponse> linkedTasks =
                Boolean.TRUE.equals(folder.getShowLinkedTasks()) && folder.getCourse() != null
                        ? getLinkedTasksByCourse(currentUserId, folder.getCourse().getId())
                        : List.of();

        return PersonalResourceFolderDetailsResponse.builder()
                .folder(PersonalResourceFolderMapper.toResponse(folder))
                .childFolders(childFolders)
                .items(items)
                .linkedNotes(linkedNotes)
                .linkedTasks(linkedTasks)
                .build();
    }

    private Student getStudentByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }
}