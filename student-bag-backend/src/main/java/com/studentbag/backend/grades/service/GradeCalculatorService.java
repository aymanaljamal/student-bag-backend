package com.studentbag.backend.grades.service;

import com.studentbag.backend.grades.entity.GradeCalculation;

public interface GradeCalculatorService {
    void recalculate(GradeCalculation calculation);
}