package com.studentbag.backend.resources.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.domain.enums.VisibilityScope;
import com.studentbag.backend.resources.entity.AdminResource;
import com.studentbag.backend.resources.entity.LearningObject;
import com.studentbag.backend.resources.repository.AdminResourceRepository;
import com.studentbag.backend.resources.repository.LearningObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminResourceService {

    private final AdminResourceRepository adminResourceRepository;
    private final LearningObjectRepository learningObjectRepository;

    public LearningObject createLearningObject(LearningObject learningObject) {
        return learningObjectRepository.save(learningObject);
    }

    public LearningObject updateLearningObject(Long id, LearningObject updated) {
        LearningObject existing = getLearningObjectById(id);

        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setKeywords(updated.getKeywords());
        existing.setLanguage(updated.getLanguage());
        existing.setFormat(updated.getFormat());
        existing.setDifficulty(updated.getDifficulty());
        existing.setIntendedEndUserRole(updated.getIntendedEndUserRole());
        existing.setEducationalLevel(updated.getEducationalLevel());
        existing.setResourceType(updated.getResourceType());
        existing.setTypicalLearningTimeMinutes(updated.getTypicalLearningTimeMinutes());
        existing.setUrl(updated.getUrl());
        existing.setThumbnailUrl(updated.getThumbnailUrl());
        existing.setIsActive(updated.getIsActive());
        existing.setCreatedByUserId(updated.getCreatedByUserId());

        return learningObjectRepository.save(existing);
    }

    public LearningObject getLearningObjectById(Long id) {
        return learningObjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Learning object not found with id: " + id));
    }

    public List<LearningObject> getAllLearningObjects() {
        return learningObjectRepository.findAll();
    }

    public List<LearningObject> getActiveLearningObjects() {
        return learningObjectRepository.findByIsActiveTrue();
    }

    public List<LearningObject> searchLearningObjectsByTitle(String title) {
        return learningObjectRepository.findByTitleContainingIgnoreCaseAndIsActiveTrue(title);
    }

    public AdminResource createAdminResource(AdminResource adminResource) {
        return adminResourceRepository.save(adminResource);
    }

    public AdminResource updateAdminResource(Long id, AdminResource updated) {
        AdminResource existing = getAdminResourceById(id);

        existing.setLearningObject(updated.getLearningObject());
        existing.setInstitution(updated.getInstitution());
        existing.setTerm(updated.getTerm());
        existing.setCourse(updated.getCourse());
        existing.setGradeOrLevel(updated.getGradeOrLevel());
        existing.setVisibilityScope(updated.getVisibilityScope());
        existing.setVersion(updated.getVersion());
        existing.setIsApproved(updated.getIsApproved());
        existing.setApprovedByAdminId(updated.getApprovedByAdminId());
        existing.setApprovedAt(updated.getApprovedAt());

        return adminResourceRepository.save(existing);
    }

    public AdminResource getAdminResourceById(Long id) {
        return adminResourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin resource not found with id: " + id));
    }

    public List<AdminResource> getAllAdminResources() {
        return adminResourceRepository.findAll();
    }

    public List<AdminResource> getByInstitution(Long institutionId) {
        return adminResourceRepository.findByInstitutionId(institutionId);
    }

    public List<AdminResource> getApprovedByInstitution(Long institutionId) {
        return adminResourceRepository.findByInstitutionIdAndIsApprovedTrue(institutionId);
    }

    public List<AdminResource> getApprovedByCourse(Long courseId) {
        return adminResourceRepository.findByCourseIdAndIsApprovedTrue(courseId);
    }

    public List<AdminResource> getByVisibility(VisibilityScope visibilityScope) {
        return adminResourceRepository.findByVisibilityScopeAndIsApprovedTrue(visibilityScope);
    }

    public AdminResource approve(Long adminResourceId, Long adminId) {
        AdminResource resource = getAdminResourceById(adminResourceId);
        resource.approve(adminId);
        return adminResourceRepository.save(resource);
    }

    public AdminResource incrementVersion(Long adminResourceId) {
        AdminResource resource = getAdminResourceById(adminResourceId);
        resource.updateVersion();
        return adminResourceRepository.save(resource);
    }

    public void deleteLearningObject(Long id) {
        LearningObject learningObject = getLearningObjectById(id);
        learningObjectRepository.delete(learningObject);
    }

    public void deleteAdminResource(Long id) {
        AdminResource resource = getAdminResourceById(id);
        adminResourceRepository.delete(resource);
    }
}