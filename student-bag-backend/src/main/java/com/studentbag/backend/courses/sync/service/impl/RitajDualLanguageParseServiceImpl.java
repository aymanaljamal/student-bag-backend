package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;
import com.studentbag.backend.courses.sync.service.RitajDualLanguageParseService;
import com.studentbag.backend.courses.sync.service.RitajFetchService;
import com.studentbag.backend.courses.sync.service.RitajParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RitajDualLanguageParseServiceImpl implements RitajDualLanguageParseService {

    private final RitajFetchService ritajFetchService;
    private final RitajParserService ritajParserService;

    @Override
    public List<RitajCourseDto> fetchParseAndMerge(String baseFileName) {
        log.info("[Dual Parser] قراءة ملف JSON normalized: {}", baseFileName);

        RitajFetchedPagesDto pages = ritajFetchService.fetchJsonFile(baseFileName);

        return ritajParserService.parseJson(pages.getJsonContent());
    }
}
