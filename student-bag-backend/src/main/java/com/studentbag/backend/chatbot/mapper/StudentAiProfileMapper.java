package com.studentbag.backend.chatbot.mapper;
import com.studentbag.backend.chatbot.dto.context.StudentProfileContext;
import com.studentbag.backend.student.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentAiProfileMapper {

    public StudentProfileContext toContext(Student student) {
        if (student == null) return null;

        var user = student.getUser();
        var institution = student.getInstitution();

        return StudentProfileContext.builder()
                .studentId(student.getId())
                .fullName(user != null ? user.getFullName() : null)
                .email(user != null ? user.getEmail() : null)
                .languageCode(user != null ? user.getLanguageCode() : null)
                .academicLevel(student.getAcademicLevel())
                .schoolGrade(student.getSchoolGrade())
                .universityMajor(student.getUniversityMajor())
                .institutionName(institution != null ? institution.getName() : null)
                .build();
    }
}