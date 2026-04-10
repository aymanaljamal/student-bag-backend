package com.studentbag.backend.courses.service;

import com.studentbag.backend.courses.dto.response.TermResponseDTO;
import java.util.List;

public interface TermService {

    List<TermResponseDTO> getTermsByInstitution(Long institutionId);


    TermResponseDTO getCurrentTerm(Long institutionId);
}