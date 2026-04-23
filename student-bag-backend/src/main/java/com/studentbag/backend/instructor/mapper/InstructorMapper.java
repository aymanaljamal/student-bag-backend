package com.studentbag.backend.instructor.mapper;

import com.studentbag.backend.instructor.dto.response.InstructorProfileResponse;
import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {

    public InstructorProfileResponse toProfileResponse(Instructor instructor) {
        return InstructorProfileResponse.builder()
                .id(instructor.getId())
                .externalId(instructor.getExternalId())
                .fullNameArabic(instructor.getFullNameArabic())
                .fullNameEnglish(instructor.getFullNameEnglish())

                .departmentId(
                        instructor.getDepartment() != null ? instructor.getDepartment().getId() : null
                )
                .departmentNameArabic(
                        instructor.getDepartment() != null ? instructor.getDepartment().getNameArabic() : null
                )
                .departmentNameEnglish(
                        instructor.getDepartment() != null ? instructor.getDepartment().getNameEnglish() : null
                )

                .institutionId(
                        instructor.getInstitution() != null ? instructor.getInstitution().getId() : null
                )
                .institutionName(
                        instructor.getInstitution() != null ? instructor.getInstitution().getName() : null
                )

                .email(instructor.getUser().getEmail())
                .phone(instructor.getUser().getPhone())
                .avatarUrl(instructor.getUser().getAvatarUrl())
                .languageCode(instructor.getUser().getLanguageCode())

                .accountConfirmed(instructor.isAccountConfirmed())
                .active(instructor.getUser().isActive())
                .emailVerified(instructor.getUser().isEmailVerified())
                .phoneVerified(instructor.getUser().isPhoneVerified())
                .build();
    }
}