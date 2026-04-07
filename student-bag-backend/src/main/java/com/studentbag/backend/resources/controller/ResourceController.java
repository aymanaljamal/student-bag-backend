package com.studentbag.backend.resources.controller;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.courses.service.TermService;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.notes.service.NoteService;
import com.studentbag.backend.resources.dto.request.AdminResourceRequest;
import com.studentbag.backend.resources.dto.request.LearningObjectRequest;
import com.studentbag.backend.resources.dto.request.PersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.PersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.response.AdminResourceResponse;
import com.studentbag.backend.resources.dto.response.LearningObjectResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceItemResponse;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.LearningObject;
import com.studentbag.backend.resources.entity.PersonalResourceFolder;
import com.studentbag.backend.resources.entity.PersonalResourceItem;
import com.studentbag.backend.resources.mapper.ResourceMapper;
import com.studentbag.backend.resources.service.AdminResourceService;
import com.studentbag.backend.resources.service.PersonalResourceService;
import com.studentbag.backend.student.entity.Student;
import com.studentbag.backend.student.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final AdminResourceService adminResourceService;
    private final PersonalResourceService personalResourceService;
    private final ResourceMapper resourceMapper;
    private final InstitutionRepository institutionRepository;
    private final StudentRepository studentRepository;
    private final CourseService courseService;
    private final TermService termService;
    private final NoteService noteService;

    // ---------------- Learning Object ----------------

    @PostMapping("/learning-objects")
    public LearningObjectResponse createLearningObject(@Valid @RequestBody LearningObjectRequest request) {
        LearningObject entity = resourceMapper.toLearningObjectEntity(request);
        return resourceMapper.toLearningObjectResponse(adminResourceService.createLearningObject(entity));
    }

    @PutMapping("/learning-objects/{id}")
    public LearningObjectResponse updateLearningObject(
            @PathVariable Long id,
            @Valid @RequestBody LearningObjectRequest request
    ) {
        LearningObject existing = adminResourceService.getLearningObjectById(id);
        resourceMapper.updateLearningObjectEntity(existing, request);
        return resourceMapper.toLearningObjectResponse(adminResourceService.updateLearningObject(id, existing));
    }

    @GetMapping("/learning-objects/{id}")
    public LearningObjectResponse getLearningObjectById(@PathVariable Long id) {
        return resourceMapper.toLearningObjectResponse(adminResourceService.getLearningObjectById(id));
    }

    @GetMapping("/learning-objects")
    public List<LearningObjectResponse> getAllLearningObjects() {
        return adminResourceService.getAllLearningObjects()
                .stream()
                .map(resourceMapper::toLearningObjectResponse)
                .toList();
    }

    @GetMapping("/learning-objects/active")
    public List<LearningObjectResponse> getActiveLearningObjects() {
        return adminResourceService.getActiveLearningObjects()
                .stream()
                .map(resourceMapper::toLearningObjectResponse)
                .toList();
    }

    @GetMapping("/learning-objects/search")
    public List<LearningObjectResponse> searchLearningObjects(@RequestParam String title) {
        return adminResourceService.searchLearningObjectsByTitle(title)
                .stream()
                .map(resourceMapper::toLearningObjectResponse)
                .toList();
    }

    @DeleteMapping("/learning-objects/{id}")
    public void deleteLearningObject(@PathVariable Long id) {
        adminResourceService.deleteLearningObject(id);
    }

    // ---------------- Admin Resource ----------------

    @PostMapping("/admin")
    public AdminResourceResponse createAdminResource(@Valid @RequestBody AdminResourceRequest request) {
        LearningObject learningObject = adminResourceService.getLearningObjectById(request.getLearningObjectId());

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + request.getInstitutionId()
                ));

        Term term = null;
        if (request.getTermId() != null) {
           // term = termService.getById(request.getTermId());
        }

        Course course = null;
        if (request.getCourseId() != null) {
          //  course = courseService.getById(request.getCourseId());
        }

        AdminResource entity = resourceMapper.toAdminResourceEntity(
                request, learningObject, institution, term, course
        );

        return resourceMapper.toAdminResourceResponse(adminResourceService.createAdminResource(entity));
    }

    @PutMapping("/admin/{id}")
    public AdminResourceResponse updateAdminResource(
            @PathVariable Long id,
            @Valid @RequestBody AdminResourceRequest request
    ) {
        LearningObject learningObject = adminResourceService.getLearningObjectById(request.getLearningObjectId());

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution not found with id: " + request.getInstitutionId()
                ));

        Term term = null;
        if (request.getTermId() != null) {
           // term = termService.getById(request.getTermId());
        }

        Course course = null;
        if (request.getCourseId() != null) {
        //    course = courseService.getById(request.getCourseId());
        }

        AdminResource existing = adminResourceService.getAdminResourceById(id);
        resourceMapper.updateAdminResourceEntity(existing, request, learningObject, institution, term, course);

        return resourceMapper.toAdminResourceResponse(adminResourceService.updateAdminResource(id, existing));
    }

    @GetMapping("/admin/{id}")
    public AdminResourceResponse getAdminResourceById(@PathVariable Long id) {
        return resourceMapper.toAdminResourceResponse(adminResourceService.getAdminResourceById(id));
    }

    @GetMapping("/admin")
    public List<AdminResourceResponse> getAllAdminResources() {
        return adminResourceService.getAllAdminResources()
                .stream()
                .map(resourceMapper::toAdminResourceResponse)
                .toList();
    }

    @GetMapping("/admin/institution/{institutionId}")
    public List<AdminResourceResponse> getByInstitution(@PathVariable Long institutionId) {
        return adminResourceService.getByInstitution(institutionId)
                .stream()
                .map(resourceMapper::toAdminResourceResponse)
                .toList();
    }

    @GetMapping("/admin/institution/{institutionId}/approved")
    public List<AdminResourceResponse> getApprovedByInstitution(@PathVariable Long institutionId) {
        return adminResourceService.getApprovedByInstitution(institutionId)
                .stream()
                .map(resourceMapper::toAdminResourceResponse)
                .toList();
    }

    @GetMapping("/admin/course/{courseId}/approved")
    public List<AdminResourceResponse> getApprovedByCourse(@PathVariable Long courseId) {
        return adminResourceService.getApprovedByCourse(courseId)
                .stream()
                .map(resourceMapper::toAdminResourceResponse)
                .toList();
    }

    @PatchMapping("/admin/{id}/approve")
    public AdminResourceResponse approve(@PathVariable Long id, @RequestParam Long adminId) {
        return resourceMapper.toAdminResourceResponse(adminResourceService.approve(id, adminId));
    }

    @PatchMapping("/admin/{id}/version")
    public AdminResourceResponse incrementVersion(@PathVariable Long id) {
        return resourceMapper.toAdminResourceResponse(adminResourceService.incrementVersion(id));
    }

    @DeleteMapping("/admin/{id}")
    public void deleteAdminResource(@PathVariable Long id) {
        adminResourceService.deleteAdminResource(id);
    }

    // ---------------- Folder ----------------

    @PostMapping("/folders")
    public PersonalResourceFolderResponse createFolder(
            @Valid @RequestBody PersonalResourceFolderRequest request
    ) {
        Student student = studentRepository.findById(request.getOwnerStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getOwnerStudentId()
                ));

        PersonalResourceFolder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = personalResourceService.getFolderById(request.getParentFolderId());
        }

        Course course = null;
        if (request.getCourseId() != null) {
          //  course = courseService.getById(request.getCourseId());
        }

        PersonalResourceFolder entity = resourceMapper.toFolderEntity(request, student, parentFolder, course);
        return resourceMapper.toFolderResponse(personalResourceService.createFolder(entity));
    }

    @PutMapping("/folders/{id}")
    public PersonalResourceFolderResponse updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody PersonalResourceFolderRequest request
    ) {
        Student student = studentRepository.findById(request.getOwnerStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getOwnerStudentId()
                ));

        PersonalResourceFolder parentFolder = null;
        if (request.getParentFolderId() != null) {
            parentFolder = personalResourceService.getFolderById(request.getParentFolderId());
        }

        Course course = null;
        if (request.getCourseId() != null) {
         //   course = courseService.getById(request.getCourseId());
        }

        PersonalResourceFolder existing = personalResourceService.getFolderById(id);
        resourceMapper.updateFolderEntity(existing, request, student, parentFolder, course);

        return resourceMapper.toFolderResponse(personalResourceService.updateFolder(id, existing));
    }

    @GetMapping("/folders/{id}")
    public PersonalResourceFolderResponse getFolderById(@PathVariable Long id) {
        return resourceMapper.toFolderResponse(personalResourceService.getFolderById(id));
    }

    @GetMapping("/folders/student/{studentId}")
    public List<PersonalResourceFolderResponse> getStudentFolders(@PathVariable Long studentId) {
        return personalResourceService.getStudentFolders(studentId)
                .stream()
                .map(resourceMapper::toFolderResponse)
                .toList();
    }

    @GetMapping("/folders/student/{studentId}/root")
    public List<PersonalResourceFolderResponse> getRootFolders(@PathVariable Long studentId) {
        return personalResourceService.getRootFolders(studentId)
                .stream()
                .map(resourceMapper::toFolderResponse)
                .toList();
    }

    @GetMapping("/folders/{parentFolderId}/children")
    public List<PersonalResourceFolderResponse> getChildFolders(@PathVariable Long parentFolderId) {
        return personalResourceService.getChildFolders(parentFolderId)
                .stream()
                .map(resourceMapper::toFolderResponse)
                .toList();
    }

    @DeleteMapping("/folders/{id}")
    public void deleteFolder(@PathVariable Long id) {
        personalResourceService.deleteFolder(id);
    }

    // ---------------- Item ----------------

    @PostMapping("/items")
    public PersonalResourceItemResponse createItem(
            @Valid @RequestBody PersonalResourceItemRequest request
    ) {
        Student student = studentRepository.findById(request.getOwnerStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getOwnerStudentId()
                ));

        PersonalResourceFolder folder = null;
        if (request.getFolderId() != null) {
            folder = personalResourceService.getFolderById(request.getFolderId());
        }

        AdminResource adminResource = null;
        if (request.getLinkedAdminResourceId() != null) {
            adminResource = adminResourceService.getAdminResourceById(request.getLinkedAdminResourceId());
        }

        Note note = null;
        if (request.getNoteId() != null) {
            note = noteService.getById(request.getNoteId());
        }

        PersonalResourceItem entity = resourceMapper.toItemEntity(request, student, folder, adminResource, note);
        return resourceMapper.toItemResponse(personalResourceService.createItem(entity));
    }

    @PutMapping("/items/{id}")
    public PersonalResourceItemResponse updateItem(
            @PathVariable Long id,
            @Valid @RequestBody PersonalResourceItemRequest request
    ) {
        Student student = studentRepository.findById(request.getOwnerStudentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + request.getOwnerStudentId()
                ));

        PersonalResourceFolder folder = null;
        if (request.getFolderId() != null) {
            folder = personalResourceService.getFolderById(request.getFolderId());
        }

        AdminResource adminResource = null;
        if (request.getLinkedAdminResourceId() != null) {
            adminResource = adminResourceService.getAdminResourceById(request.getLinkedAdminResourceId());
        }

        Note note = null;
        if (request.getNoteId() != null) {
            note = noteService.getById(request.getNoteId());
        }

        PersonalResourceItem existing = personalResourceService.getItemById(id);
        resourceMapper.updateItemEntity(existing, request, student, folder, adminResource, note);

        return resourceMapper.toItemResponse(personalResourceService.updateItem(id, existing));
    }

    @GetMapping("/items/{id}")
    public PersonalResourceItemResponse getItemById(@PathVariable Long id) {
        return resourceMapper.toItemResponse(personalResourceService.getItemById(id));
    }

    @GetMapping("/items/student/{studentId}")
    public List<PersonalResourceItemResponse> getStudentItems(@PathVariable Long studentId) {
        return personalResourceService.getStudentItems(studentId)
                .stream()
                .map(resourceMapper::toItemResponse)
                .toList();
    }

    @GetMapping("/items/folder/{folderId}")
    public List<PersonalResourceItemResponse> getFolderItems(@PathVariable Long folderId) {
        return personalResourceService.getFolderItems(folderId)
                .stream()
                .map(resourceMapper::toItemResponse)
                .toList();
    }

    @GetMapping("/items/student/{studentId}/important")
    public List<PersonalResourceItemResponse> getImportantItems(@PathVariable Long studentId) {
        return personalResourceService.getImportantItems(studentId)
                .stream()
                .map(resourceMapper::toItemResponse)
                .toList();
    }

    @GetMapping("/items/student/{studentId}/exam-preparation")
    public List<PersonalResourceItemResponse> getExamPreparationItems(@PathVariable Long studentId) {
        return personalResourceService.getExamPreparationItems(studentId)
                .stream()
                .map(resourceMapper::toItemResponse)
                .toList();
    }

    @PatchMapping("/items/{id}/important")
    public PersonalResourceItemResponse markImportant(@PathVariable Long id) {
        return resourceMapper.toItemResponse(personalResourceService.markImportant(id));
    }

    @PatchMapping("/items/{id}/move")
    public PersonalResourceItemResponse moveToFolder(@PathVariable Long id, @RequestParam Long folderId) {
        return resourceMapper.toItemResponse(personalResourceService.moveToFolder(id, folderId));
    }

    @DeleteMapping("/items/{id}")
    public void deleteItem(@PathVariable Long id) {
        personalResourceService.deleteItem(id);
    }
}