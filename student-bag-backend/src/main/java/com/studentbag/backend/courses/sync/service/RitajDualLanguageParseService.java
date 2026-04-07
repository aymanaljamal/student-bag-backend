package com.studentbag.backend.courses.sync.service;

import com.studentbag.backend.courses.sync.dto.RitajCourseDto;

import java.util.List;

public interface RitajDualLanguageParseService {

    List<RitajCourseDto> fetchParseAndMerge(String baseUrl);
}