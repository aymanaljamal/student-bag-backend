package com.studentbag.backend.instructor.service.impl;

import com.studentbag.backend.instructor.service.InstructorAccountPasswordService;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class InstructorAccountPasswordServiceImpl implements InstructorAccountPasswordService {

    private final Random random = new Random();

    @Override
    public String generateInitialPassword(String fullNameArabic, String courseCode, String sectionNumber) {
        String codePart = courseCode == null ? "course" : courseCode.replaceAll("[^A-Za-z0-9]", "");
        String sectionPart = sectionNumber == null ? "1" : sectionNumber.replaceAll("[^A-Za-z0-9]", "");
        int suffix = 1000 + random.nextInt(9000);

        return codePart + "_" + sectionPart + "_" + suffix + "!";
    }
}