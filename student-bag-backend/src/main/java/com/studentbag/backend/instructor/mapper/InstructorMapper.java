package com.studentbag.backend.instructor.mapper;

import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.users.entity.User;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {

    public InstructorProfileResponse toProfileResponse(Instructor instructor) {
        Department department = instructor.getDepartment();
        Institution institution = instructor.getInstitution();
        User user = instructor.getUser();

        return InstructorProfileResponse.builder()
                .id(instructor.getId())
                .externalId(instructor.getExternalId())
                .fullNameArabic(instructor.getFullNameArabic())
                .fullNameEnglish(instructor.getFullNameEnglish())

                .departmentId(department != null ? department.getId() : null)
                .departmentNameArabic(department != null ? department.getNameArabic() : null)
                .departmentNameEnglish(department != null ? department.getNameEnglish() : null)

                .institutionId(institution != null ? institution.getId() : null)
                .institutionName(institution != null ? institution.getName() : null)

                .email(user != null ? user.getEmail() : null)
                .phone(user != null ? user.getPhone() : null)
                .avatarUrl(user != null ? user.getAvatarUrl() : null)
                .active(user != null ? user.isActive() : null)
                .build();
    }
}