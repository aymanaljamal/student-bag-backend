package com.studentbag.backend.grades.service;

import com.studentbag.backend.grades.dto.request.CreateGradeCalculationRequest;
import com.studentbag.backend.grades.dto.request.GradeCourseItemRequest;
import com.studentbag.backend.grades.dto.request.GradeWhatIfRequest;
import com.studentbag.backend.grades.dto.request.UpdateGradeCalculationRequest;
import com.studentbag.backend.grades.dto.response.GradeCalculationResponse;
import com.studentbag.backend.grades.dto.response.GradeInsightsResponse;
import com.studentbag.backend.grades.dto.response.GradeWhatIfResponse;

import java.util.List;

public interface GradeCalculationService {

    GradeCalculationResponse createManual(Long studentId, CreateGradeCalculationRequest request);

    GradeCalculationResponse createFromActiveSchedule(Long studentId, CreateGradeCalculationRequest request);

    List<GradeCalculationResponse> getStudentCalculations(Long studentId);

    GradeCalculationResponse getCalculation(Long calculationId, Long studentId);

    GradeCalculationResponse updateCalculation(Long calculationId, Long studentId, UpdateGradeCalculationRequest request);

    GradeCalculationResponse addItem(Long calculationId, Long studentId, GradeCourseItemRequest request);

    GradeCalculationResponse deleteItem(Long calculationId, Long studentId, Long itemId);

    GradeCalculationResponse recalculate(Long calculationId, Long studentId);
    GradeInsightsResponse getInsights(Long calculationId, Long studentId);

    GradeWhatIfResponse analyzeWhatIf(Long calculationId, Long studentId, GradeWhatIfRequest request);
    void deleteCalculation(Long calculationId, Long studentId);
}