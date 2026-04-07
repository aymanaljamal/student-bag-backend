package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.DepartmentRequestDTO;
import com.studentbag.backend.courses.dto.response.DepartmentResponseDTO;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.entity.Faculty;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public void toEntity(DepartmentRequestDTO request, Department department, Faculty faculty) {
        department.setNameArabic(request.getNameArabic());
        department.setNameEnglish(request.getNameEnglish());
        department.setProgramNameArabic(request.getProgramNameArabic());
        department.setProgramNameEnglish(request.getProgramNameEnglish());
        department.setFaculty(faculty);
        department.setIsActive(request.getIsActive());
    }

    public DepartmentResponseDTO toResponse(Department department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .externalId(department.getExternalId())
                .nameArabic(department.getNameArabic())
                .nameEnglish(department.getNameEnglish())
                .programNameArabic(department.getProgramNameArabic())
                .programNameEnglish(department.getProgramNameEnglish())
                .facultyId(department.getFaculty().getId())
                .isActive(department.getIsActive())
                .build();
    }
}