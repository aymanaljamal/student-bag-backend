package com.studentbag.backend.courses.service.impl;

import com.studentbag.backend.courses.dto.request.DepartmentRequestDTO;
import com.studentbag.backend.courses.dto.response.DepartmentResponseDTO;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.courses.mapper.DepartmentMapper;
import com.studentbag.backend.courses.repository.DepartmentRepository;
import com.studentbag.backend.courses.repository.FacultyRepository;
import com.studentbag.backend.courses.service.DepartmentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentResponseDTO create(DepartmentRequestDTO request) {

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        Department department = new Department();
        departmentMapper.toEntity(request, department, faculty);

        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    public DepartmentResponseDTO update(Long id, DepartmentRequestDTO request) {

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));

        Faculty faculty = facultyRepository.findById(request.getFacultyId())
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        departmentMapper.toEntity(request, department, faculty);

        return departmentMapper.toResponse(departmentRepository.save(department));
    }

    @Override
    public DepartmentResponseDTO getById(Long id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Department not found"));
    }

    @Override
    public List<DepartmentResponseDTO> getAll() {
        return departmentRepository.findAll().stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new EntityNotFoundException("Department not found");
        }
        departmentRepository.deleteById(id);
    }

    @Override
    public List<DepartmentResponseDTO> getAllByInstitution(Long institutionId) {
        return departmentRepository.findByFacultyInstitutionId(institutionId)
                .stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Override
    public Page<DepartmentResponseDTO> search(
            String keyword,
            Long institutionId,
            Long facultyId,
            Boolean isActive,
            Pageable pageable
    ) {
        return departmentRepository.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";
                predicates = cb.and(
                        predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("nameArabic")), like),
                                cb.like(cb.lower(root.get("nameEnglish")), like),
                                cb.like(cb.lower(root.get("programNameArabic")), like),
                                cb.like(cb.lower(root.get("programNameEnglish")), like)
                        )
                );
            }

            if (facultyId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("faculty").get("id"), facultyId)
                );
            }

            if (institutionId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("faculty").get("institution").get("id"), institutionId)
                );
            }

            if (isActive != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("isActive"), isActive)
                );
            }

            return predicates;
        }, pageable).map(departmentMapper::toResponse);
    }
}