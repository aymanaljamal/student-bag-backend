package com.studentbag.backend.courses.controller;
import com.studentbag.backend.courses.dto.response.TermResponseDTO;
import com.studentbag.backend.courses.service.TermService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/v1/terms")
@RequiredArgsConstructor
public class TermController {
    private final TermService termService;

    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<TermResponseDTO>> getAllTerms(@PathVariable Long institutionId) {
        return ResponseEntity.ok(termService.getTermsByInstitution(institutionId));
    }
    @GetMapping("/institution/{institutionId}/current")
    public ResponseEntity<TermResponseDTO> getCurrentTerm(@PathVariable Long institutionId) {
        return ResponseEntity.ok(termService.getCurrentTerm(institutionId));
    }
}