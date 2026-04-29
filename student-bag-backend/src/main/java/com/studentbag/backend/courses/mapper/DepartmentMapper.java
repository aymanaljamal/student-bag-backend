package com.studentbag.backend.courses.mapper;

import com.studentbag.backend.courses.dto.request.DepartmentRequestDTO;
import com.studentbag.backend.courses.dto.response.DepartmentResponseDTO;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.entity.Faculty;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public void toEntity(
            DepartmentRequestDTO request,
            Department department,
            Faculty faculty
    ) {
        department.setNameArabic(request.getNameArabic());
        department.setNameEnglish(request.getNameEnglish());
        department.setProgramNameArabic(request.getProgramNameArabic());
        department.setProgramNameEnglish(request.getProgramNameEnglish());
        department.setFaculty(faculty);
        department.setIsActive(request.getIsActive());
    }

    public DepartmentResponseDTO toResponse(Department department) {
        Faculty faculty = department.getFaculty();

        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .externalId(department.getExternalId())
                .nameArabic(department.getNameArabic())
                .nameEnglish(department.getNameEnglish())
                .programNameArabic(department.getProgramNameArabic())
                .programNameEnglish(department.getProgramNameEnglish())
                .facultyId(faculty.getId())
                .facultyNameArabic(faculty.getNameArabic())
                .facultyNameEnglish(faculty.getNameEnglish())
                .institutionId(faculty.getInstitution().getId())
                .institutionName(faculty.getInstitution().getName())
                .isActive(department.getIsActive())
                .build();
    }
}