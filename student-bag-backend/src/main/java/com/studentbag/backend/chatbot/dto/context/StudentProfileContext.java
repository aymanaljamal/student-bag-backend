package com.studentbag.backend.chatbot.dto.context;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileContext {

    private Long studentId;
    private String fullName;
    private String email;
    private String languageCode;

    private String academicLevel;
    private String schoolGrade;
    private String universityMajor;

    private String institutionName;
}