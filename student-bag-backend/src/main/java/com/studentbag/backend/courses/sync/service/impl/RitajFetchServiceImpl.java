package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;
import com.studentbag.backend.courses.sync.service.RitajFetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class RitajFetchServiceImpl implements RitajFetchService {

    private final ResourceLoader resourceLoader;

    public RitajFetchServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Reads a single .txt file from classpath:static/
     */
    @Override
    public String fetchText(String fileName) {
        String path = "classpath:static/" + fileName;
        try {
            Resource resource = resourceLoader.getResource(path);
            if (!resource.exists()) {
                log.error("[Fetch] File not found: {}", path);
                throw new RuntimeException("الملف غير موجود: " + path);
            }
            log.info("[Fetch] Reading: {}", path);
            try (InputStreamReader reader =
                         new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                return FileCopyUtils.copyToString(reader);
            }
        } catch (IOException e) {
            log.error("[Fetch] IO error reading {}: {}", path, e.getMessage());
            throw new RuntimeException("فشل قراءة الملف: " + e.getMessage(), e);
        }
    }

    /**
     * Reads Arabic and English versions of the schedule file.
     *
     * Expects two files in classpath:static/:
     *   {baseFileName}_ar.txt  ← Arabic schedule page
     *   {baseFileName}_en.txt  ← English schedule page
     *
     * Both files are the plain-text (or HTML-stripped) exports from Birzeit Ritaj.
     * Tab characters must be preserved as column separators.
     */
    @Override
    public RitajFetchedPagesDto fetchArabicAndEnglishPages(String baseFileName) {
        log.info("[Fetch] Loading schedule files for base: {}", baseFileName);

        String arabicContent  = fetchText(baseFileName + "_ar.txt");
        String englishContent = fetchText(baseFileName + "_en.txt");

        log.info("[Fetch] Arabic content length: {} chars", arabicContent.length());
        log.info("[Fetch] English content length: {} chars", englishContent.length());

        return new RitajFetchedPagesDto(arabicContent, englishContent);
    }
}