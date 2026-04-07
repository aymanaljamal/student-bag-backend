package com.studentbag.backend.instructor.service;

public interface InstructorAccountPasswordService {

    String generateInitialPassword(String fullNameArabic, String courseCode, String sectionNumber);
}