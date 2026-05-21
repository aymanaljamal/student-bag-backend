package com.studentbag.backend.resources.service.impl;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import com.studentbag.backend.resources.dto.request.CopyAdminResourceToPersonalRequest;
import com.studentbag.backend.resources.dto.request.CopyPersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.CreatePersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.CreatePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.GenerateFoldersFromActiveScheduleRequest;
import com.studentbag.backend.resources.dto.request.MovePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.UpdatePersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.UpdatePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.response.LinkedNoteSummaryResponse;
import com.studentbag.backend.resources.dto.response.LinkedTaskSummaryResponse;
import com.studentbag.backend.resources.dto.response.PersonalLibraryOverviewResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderDetailsResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceItemResponse;
import com.studentbag.backend.resources.dto.response.ResourceCourseSummaryResponse;
import com.studentbag.backend.resources.dto.response.ResourceOperationResponse;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import com.studentbag.backend.resources.entity.PersonalResourceItem;
import com.studentbag.backend.resources.entity.ResourceShareCopyLog;
import com.studentbag.backend.resources.mapper.PersonalResourceFolderMapper;
import com.studentbag.backend.resources.mapper.PersonalResourceItemMapper;
import com.studentbag.backend.resources.repository.AdminResourceRepository;
import com.studentbag.backend.resources.repository.PersonalResourceFolderRepository;
import com.studentbag.backend.resources.repository.PersonalResourceItemRepository;
import com.studentbag.backend.resources.repository.ResourceShareCopyLogRepository;
import com.studentbag.backend.resources.service.PersonalResourceLibraryService;
import com.studentbag.backend.resources.service.ResourceIntegrationService;
import com.studentbag.backend.schedule.dto.response.ActiveScheduleCourseDTO;
import com.studentbag.backend.schedule.service.ScheduleManagementService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PersonalResourceLibraryServiceImpl implements PersonalResourceLibraryService {

    private final PersonalResourceFolderRepository folderRepository;
    private final PersonalResourceItemRepository itemRepository;
    private final ResourceShareCopyLogRepository copyLogRepository;
    private final AdminResourceRepository adminResourceRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final TermRepository termRepository;
    private final ScheduleManagementService scheduleManagementService;
    private final ResourceIntegrationService resourceIntegrationService;

    @Override
    @Transactional
    public PersonalLibraryOverviewResponse getLibraryOverview(UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceFolder root = getOrCreateRootFolderEntity(student);

        Long currentTermId = getCurrentTermId();

        List<ResourceCourseSummaryResponse> activeCourses =
                resourceIntegrationService.getActiveScheduleCoursesForLibrary(currentUserId, currentTermId);

        // مهم: أنشئ فولدرات للكورسات النشطة تلقائيًا إذا مش موجودة
        ensureCourseFoldersForActiveCourses(student, root, activeCourses);

        // بعد الإنشاء، اقرأ الفولدرات من جديد
        List<PersonalResourceFolderResponse> topFolders = folderRepository
                .findByStudentIdAndParentFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
                        student.getId(),
                        root.getId()
                )
                .stream()
                .map(PersonalResourceFolderMapper::toResponse)
                .toList();

        return PersonalLibraryOverviewResponse.builder()
                .rootFolder(PersonalResourceFolderMapper.toResponse(root))
                .topFolders(topFolders)
                .activeScheduleCourses(activeCourses)
                .build();
    }
    @Override
    public PersonalResourceFolderResponse getOrCreateRootFolder(UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        return PersonalResourceFolderMapper.toResponse(getOrCreateRootFolderEntity(student));
    }
    private void ensureCourseFoldersForActiveCourses(
            Student student,
            PersonalResourceFolder root,
            List<ResourceCourseSummaryResponse> activeCourses
    ) {
        if (activeCourses == null || activeCourses.isEmpty()) {
            return;
        }

        for (ResourceCourseSummaryResponse courseDto : activeCourses) {
            List<PersonalResourceFolder> existingFolders =
                    folderRepository.findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
                            student.getId(),
                            courseDto.getId()
                    );

            if (!existingFolders.isEmpty()) {
                continue;
            }

            Course course = getCourse(courseDto.getId());

            PersonalResourceFolder folder = PersonalResourceFolder.builder()
                    .name(course.getNameEnglish() != null && !course.getNameEnglish().isBlank()
                            ? course.getNameEnglish()
                            : course.getNameArabic())
                    .description("System-generated folder from active schedule")
                    .student(student)
                    .parentFolder(root)
                    .course(course)
                    .isRoot(false)
                    .isSystemGenerated(true)
                    .isDeleted(false)
                    .isArchived(false)
                    .showLinkedNotes(true)
                    .showLinkedTasks(true)
                    .build();

            folderRepository.save(folder);
        }
    }
    @Override
    public PersonalResourceFolderResponse createFolder(
            UUID currentUserId,
            CreatePersonalResourceFolderRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);

        PersonalResourceFolder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = getFolderForStudent(request.getParentFolderId(), student.getId());
        }

        validateFolderName(student.getId(), parentFolder, request.getName());

        Course course = null;
        if (request.getCourseId() != null) {
            course = getCourse(request.getCourseId());
        }

        PersonalResourceFolder folder = PersonalResourceFolderMapper.toEntity(request);
        folder.setStudent(student);
        folder.setParentFolder(parentFolder);
        folder.setCourse(course);

        PersonalResourceFolder saved = folderRepository.save(folder);
        return PersonalResourceFolderMapper.toResponse(saved);
    }

    @Override
    public PersonalResourceFolderResponse updateFolder(
            Long folderId,
            UUID currentUserId,
            UpdatePersonalResourceFolderRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceFolder folder = getFolderForStudent(folderId, student.getId());

        if (request.getParentFolderId() != null) {
            PersonalResourceFolder newParent = getFolderForStudent(request.getParentFolderId(), student.getId());
            if (folder.getId().equals(newParent.getId())) {
                throw new IllegalArgumentException("Folder cannot be its own parent");
            }
            folder.setParentFolder(newParent);
        }

        PersonalResourceFolderMapper.updateEntity(folder, request);

        PersonalResourceFolder saved = folderRepository.save(folder);
        return PersonalResourceFolderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PersonalResourceFolderResponse getFolderById(Long folderId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        return PersonalResourceFolderMapper.toResponse(getFolderForStudent(folderId, student.getId()));
    }

    @Override
    @Transactional
    public List<PersonalResourceFolderResponse> getTopFolders(UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceFolder root = getOrCreateRootFolderEntity(student);

        return folderRepository
                .findByStudentIdAndParentFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
                        student.getId(),
                        root.getId()
                )
                .stream()
                .map(PersonalResourceFolderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalResourceFolderResponse> getChildFolders(Long parentFolderId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        getFolderForStudent(parentFolderId, student.getId());

        return folderRepository
                .findByStudentIdAndParentFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
                        student.getId(),
                        parentFolderId
                )
                .stream()
                .map(PersonalResourceFolderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalResourceFolderResponse> getFoldersByCourse(UUID currentUserId, Long courseId) {
        Student student = getStudentByUserId(currentUserId);

        return folderRepository
                .findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
                        student.getId(),
                        courseId
                )
                .stream()
                .map(PersonalResourceFolderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalResourceFolderDetailsResponse getFolderDetails(Long folderId, UUID currentUserId) {
        return resourceIntegrationService.buildCourseFolderDetails(folderId, currentUserId);
    }

    @Override
    public ResourceOperationResponse softDeleteFolder(Long folderId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceFolder folder = getFolderForStudent(folderId, student.getId());

        folder.setIsDeleted(true);
        folderRepository.save(folder);

        return ResourceOperationResponse.builder()
                .targetId(folderId)
                .operation("DELETE_FOLDER")
                .success(true)
                .message("Folder deleted successfully")
                .build();
    }

    @Override
    public ResourceOperationResponse archiveFolder(Long folderId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceFolder folder = getFolderForStudent(folderId, student.getId());

        folder.setIsArchived(true);
        folderRepository.save(folder);

        return ResourceOperationResponse.builder()
                .targetId(folderId)
                .operation("ARCHIVE_FOLDER")
                .success(true)
                .message("Folder archived successfully")
                .build();
    }

    @Override
    public List<PersonalResourceFolderResponse> generateFoldersFromActiveSchedule(
            UUID currentUserId,
            GenerateFoldersFromActiveScheduleRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceFolder root = getOrCreateRootFolderEntity(student);

        Long termId = request != null && request.getTermId() != null
                ? request.getTermId()
                : getCurrentTermId();

        List<ActiveScheduleCourseDTO> courses;

        try {
            courses = scheduleManagementService.getActiveScheduleCourses(student.getId(), termId);
        } catch (Exception e) {
            return List.of();
        }

        return courses.stream()
                .map(courseDto -> {
                    List<PersonalResourceFolder> existingFolders =
                            folderRepository.findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtAsc(
                                    student.getId(),
                                    courseDto.getId()
                            );

                    if (!existingFolders.isEmpty()
                            && (request == null || !Boolean.TRUE.equals(request.getOverwriteExisting()))) {
                        return PersonalResourceFolderMapper.toResponse(existingFolders.get(0));
                    }

                    Course course = getCourse(courseDto.getId());

                    PersonalResourceFolder folder = PersonalResourceFolder.builder()
                            .name(course.getNameEnglish() != null && !course.getNameEnglish().isBlank()
                                    ? course.getNameEnglish()
                                    : course.getNameArabic())
                            .description("System-generated folder from active schedule")
                            .student(student)
                            .parentFolder(root)
                            .course(course)
                            .isRoot(false)
                            .isSystemGenerated(true)
                            .isDeleted(false)
                            .isArchived(false)
                            .showLinkedNotes(true)
                            .showLinkedTasks(true)
                            .build();

                    return PersonalResourceFolderMapper.toResponse(folderRepository.save(folder));
                })
                .toList();
    }
    @Override
    public PersonalResourceItemResponse createItem(
            UUID currentUserId,
            CreatePersonalResourceItemRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);

        PersonalResourceFolder folder = null;
        if (request.getFolderId() != null) {
            folder = getFolderForStudent(request.getFolderId(), student.getId());
        }

        Course course = null;
        if (request.getCourseId() != null) {
            course = getCourse(request.getCourseId());
        }

        PersonalResourceItem item = PersonalResourceItemMapper.toEntity(request);
        item.setStudent(student);
        item.setFolder(folder);
        item.setCourse(course);

        PersonalResourceItem saved = itemRepository.save(item);
        return PersonalResourceItemMapper.toResponse(saved);
    }

    @Override
    public PersonalResourceItemResponse updateItem(
            Long itemId,
            UUID currentUserId,
            UpdatePersonalResourceItemRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceItem item = getItemForStudent(itemId, student.getId());

        PersonalResourceItemMapper.updateEntity(item, request);

        if (request.getFolderId() != null) {
            item.setFolder(getFolderForStudent(request.getFolderId(), student.getId()));
        }

        if (request.getCourseId() != null) {
            item.setCourse(getCourse(request.getCourseId()));
        }

        PersonalResourceItem saved = itemRepository.save(item);
        return PersonalResourceItemMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalResourceItemResponse getItemById(Long itemId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        return PersonalResourceItemMapper.toResponse(getItemForStudent(itemId, student.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalResourceItemResponse> getItemsByFolder(Long folderId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        getFolderForStudent(folderId, student.getId());

        return itemRepository
                .findByStudentIdAndFolderIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
                        student.getId(),
                        folderId
                )
                .stream()
                .map(PersonalResourceItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalResourceItemResponse> getItemsByCourse(UUID currentUserId, Long courseId) {
        Student student = getStudentByUserId(currentUserId);

        return itemRepository
                .findByStudentIdAndCourseIdAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
                        student.getId(),
                        courseId
                )
                .stream()
                .map(PersonalResourceItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalResourceItemResponse> getItemsByCategory(
            UUID currentUserId,
            ResourceCategory category
    ) {
        Student student = getStudentByUserId(currentUserId);

        return itemRepository
                .findByStudentIdAndCategoryAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
                        student.getId(),
                        category
                )
                .stream()
                .map(PersonalResourceItemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalResourceItemResponse> getItemsByType(
            UUID currentUserId,
            ResourceType resourceType
    ) {
        Student student = getStudentByUserId(currentUserId);

        return itemRepository
                .findByStudentIdAndResourceTypeAndIsDeletedFalseAndIsArchivedFalseOrderByCreatedAtDesc(
                        student.getId(),
                        resourceType
                )
                .stream()
                .map(PersonalResourceItemMapper::toResponse)
                .toList();
    }

    @Override
    public ResourceOperationResponse moveItem(
            Long itemId,
            UUID currentUserId,
            MovePersonalResourceItemRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceItem item = getItemForStudent(itemId, student.getId());
        PersonalResourceFolder targetFolder = getFolderForStudent(request.getTargetFolderId(), student.getId());

        item.setFolder(targetFolder);
        itemRepository.save(item);

        return ResourceOperationResponse.builder()
                .targetId(itemId)
                .operation("MOVE_ITEM")
                .success(true)
                .message("Item moved successfully")
                .build();
    }

    @Override
    public PersonalResourceItemResponse copyAdminResourceToPersonal(
            Long adminResourceId,
            UUID currentUserId,
            CopyAdminResourceToPersonalRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);

        AdminResource source = adminResourceRepository.findById(adminResourceId)
                .orElseThrow(() -> new IllegalArgumentException("Admin resource not found"));

        PersonalResourceFolder targetFolder = resolveTargetFolder(
                student,
                request != null ? request.getTargetFolderId() : null
        );

        PersonalResourceItem item = PersonalResourceItem.builder()
                .title(source.getTitle())
                .description(source.getDescription())
                .resourceType(source.getResourceType())
                .category(source.getCategory())
                .student(student)
                .folder(targetFolder)
                .course(source.getCourse())
                .fileUrl(source.getFileUrl())
                .externalLink(source.getExternalLink())
                .thumbnailUrl(source.getThumbnailUrl())
                .mimeType(source.getMimeType())
                .fileName(source.getFileName())
                .fileSizeBytes(source.getFileSizeBytes())
                .copiedFromAdminResourceId(source.getId())
                .isDeleted(false)
                .isArchived(false)
                .build();

        PersonalResourceItem saved = itemRepository.save(item);

        copyLogRepository.save(ResourceShareCopyLog.builder()
                .student(student)
                .sourceAdminResourceId(source.getId())
                .targetFolderId(targetFolder.getId())
                .createdPersonalItemId(saved.getId())
                .build());

        return PersonalResourceItemMapper.toResponse(saved);
    }

    @Override
    public PersonalResourceItemResponse copyPersonalItem(
            Long itemId,
            UUID currentUserId,
            CopyPersonalResourceItemRequest request
    ) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceItem source = getItemForStudent(itemId, student.getId());

        PersonalResourceFolder targetFolder = resolveTargetFolder(
                student,
                request != null ? request.getTargetFolderId() : null
        );

        PersonalResourceItem copy = PersonalResourceItem.builder()
                .title(source.getTitle())
                .description(source.getDescription())
                .resourceType(source.getResourceType())
                .category(source.getCategory())
                .student(student)
                .folder(targetFolder)
                .course(source.getCourse())
                .fileUrl(source.getFileUrl())
                .externalLink(source.getExternalLink())
                .thumbnailUrl(source.getThumbnailUrl())
                .mimeType(source.getMimeType())
                .fileName(source.getFileName())
                .fileSizeBytes(source.getFileSizeBytes())
                .copiedFromPersonalItemId(source.getId())
                .linkedNoteId(source.getLinkedNoteId())
                .linkedTaskId(source.getLinkedTaskId())
                .isDeleted(false)
                .isArchived(false)
                .build();

        PersonalResourceItem saved = itemRepository.save(copy);

        copyLogRepository.save(ResourceShareCopyLog.builder()
                .student(student)
                .sourcePersonalItemId(source.getId())
                .targetFolderId(targetFolder.getId())
                .createdPersonalItemId(saved.getId())
                .build());

        return PersonalResourceItemMapper.toResponse(saved);
    }

    @Override
    public ResourceOperationResponse archiveItem(Long itemId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceItem item = getItemForStudent(itemId, student.getId());

        item.setIsArchived(true);
        itemRepository.save(item);

        return ResourceOperationResponse.builder()
                .targetId(itemId)
                .operation("ARCHIVE_ITEM")
                .success(true)
                .message("Item archived successfully")
                .build();
    }

    @Override
    public ResourceOperationResponse softDeleteItem(Long itemId, UUID currentUserId) {
        Student student = getStudentByUserId(currentUserId);
        PersonalResourceItem item = getItemForStudent(itemId, student.getId());

        item.setIsDeleted(true);
        itemRepository.save(item);

        return ResourceOperationResponse.builder()
                .targetId(itemId)
                .operation("DELETE_ITEM")
                .success(true)
                .message("Item deleted successfully")
                .build();
    }

    private Student getStudentByUserId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
    }

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    private PersonalResourceFolder getFolderForStudent(Long folderId, Long studentId) {
        return folderRepository.findByIdAndStudentIdAndIsDeletedFalse(folderId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found"));
    }

    private PersonalResourceItem getItemForStudent(Long itemId, Long studentId) {
        return itemRepository.findByIdAndStudentIdAndIsDeletedFalse(itemId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
    }

    private PersonalResourceFolder getOrCreateRootFolderEntity(Student student) {
        return folderRepository.findByStudentIdAndIsRootTrueAndIsDeletedFalse(student.getId())
                .orElseGet(() -> folderRepository.save(
                        PersonalResourceFolder.builder()
                                .name("My Resources")
                                .description("Root personal resource folder")
                                .student(student)
                                .parentFolder(null)
                                .course(null)
                                .isRoot(true)
                                .isSystemGenerated(true)
                                .isDeleted(false)
                                .isArchived(false)
                                .showLinkedNotes(true)
                                .showLinkedTasks(true)
                                .build()
                ));
    }

    private PersonalResourceFolder resolveTargetFolder(Student student, Long targetFolderId) {
        if (targetFolderId != null) {
            return getFolderForStudent(targetFolderId, student.getId());
        }

        return getOrCreateRootFolderEntity(student);
    }

    private Long getCurrentTermId() {
        return termRepository.findAll()
                .stream()
                .filter(term -> Boolean.TRUE.equals(term.getIsCurrent()))
                .map(Term::getId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Current term not found"));
    }

    private void validateFolderName(Long studentId, PersonalResourceFolder parentFolder, String name) {
        boolean exists = parentFolder == null
                ? folderRepository.existsByStudentIdAndParentFolderIsNullAndNameIgnoreCaseAndIsDeletedFalse(
                studentId,
                name
        )
                : folderRepository.existsByStudentIdAndParentFolderIdAndNameIgnoreCaseAndIsDeletedFalse(
                studentId,
                parentFolder.getId(),
                name
        );

        if (exists) {
            throw new IllegalArgumentException("Folder with the same name already exists");
        }
    }
}