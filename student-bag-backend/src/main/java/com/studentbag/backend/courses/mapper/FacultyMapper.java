package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.FacultyRequestDTO;
import com.studentbag.backend.courses.dto.response.FacultyResponseDTO;
import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

@Component
public class FacultyMapper {

    public void toEntity(FacultyRequestDTO request, Faculty faculty, Institution institution) {
        faculty.setNameArabic(request.getNameArabic());
        faculty.setNameEnglish(request.getNameEnglish());
        faculty.setInstitution(institution);
        faculty.setIsActive(request.getIsActive());
    }

    public FacultyResponseDTO toResponse(Faculty faculty) {
        return FacultyResponseDTO.builder()
                .id(faculty.getId())
                .externalId(faculty.getExternalId())
                .nameArabic(faculty.getNameArabic())
                .nameEnglish(faculty.getNameEnglish())
                .institutionId(faculty.getInstitution().getId())
                .institutionName(faculty.getInstitution().getName())
                .isActive(faculty.getIsActive())
                .build();
    }
}