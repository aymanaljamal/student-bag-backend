package com.studentbag.backend.courses.service.impl;

import com.studentbag.backend.courses.dto.request.FacultyRequestDTO;
import com.studentbag.backend.courses.dto.response.FacultyResponseDTO;
import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.courses.mapper.FacultyMapper;
import com.studentbag.backend.courses.repository.FacultyRepository;
import com.studentbag.backend.courses.service.FacultyService;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyServiceImpl implements FacultyService {

    private final FacultyRepository facultyRepository;
    private final InstitutionRepository institutionRepository;
    private final FacultyMapper facultyMapper;

    @Override
    public FacultyResponseDTO create(FacultyRequestDTO request) {

        var institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institution not found"));

        Faculty faculty = new Faculty();
        facultyMapper.toEntity(request, faculty, institution);

        return facultyMapper.toResponse(facultyRepository.save(faculty));
    }

    @Override
    public FacultyResponseDTO update(Long id, FacultyRequestDTO request) {

        Faculty faculty = facultyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));

        var institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institution not found"));

        facultyMapper.toEntity(request, faculty, institution);

        return facultyMapper.toResponse(facultyRepository.save(faculty));
    }

    @Override
    public FacultyResponseDTO getById(Long id) {
        return facultyRepository.findById(id)
                .map(facultyMapper::toResponse)
                .orElseThrow(() -> new EntityNotFoundException("Faculty not found"));
    }

    @Override
    public List<FacultyResponseDTO> getAll() {
        return facultyRepository.findAll().stream()
                .map(facultyMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!facultyRepository.existsById(id)) {
            throw new EntityNotFoundException("Faculty not found");
        }
        facultyRepository.deleteById(id);
    }

    @Override
    public Page<FacultyResponseDTO> search(
            String keyword,
            Long institutionId,
            Boolean isActive,
            Pageable pageable
    ) {
        return facultyRepository.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();

            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword.trim().toLowerCase() + "%";

                predicates = cb.and(
                        predicates,
                        cb.or(
                                cb.like(cb.lower(root.get("nameArabic")), like),
                                cb.like(cb.lower(root.get("nameEnglish")), like)
                        )
                );
            }

            if (institutionId != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("institution").get("id"), institutionId)
                );
            }

            if (isActive != null) {
                predicates = cb.and(
                        predicates,
                        cb.equal(root.get("isActive"), isActive)
                );
            }

            return predicates;
        }, pageable).map(facultyMapper::toResponse);
    }
}