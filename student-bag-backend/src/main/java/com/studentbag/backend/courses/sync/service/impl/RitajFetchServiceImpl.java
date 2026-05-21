package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;
import com.studentbag.backend.courses.sync.service.RitajFetchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class RitajFetchServiceImpl implements RitajFetchService {

    private static final String STATIC_PATH = "static/";

    @Override
    public RitajFetchedPagesDto fetchJsonFile(String baseFileName) {
        String fileName = baseFileName.endsWith(".json") ? baseFileName : baseFileName + ".json";
        String fullPath = STATIC_PATH + fileName;

        log.info("📂 [FetchService] قراءة الملف: {}", fullPath);

        try {
            ClassPathResource resource = new ClassPathResource(fullPath);
            if (!resource.exists()) {
                throw new IllegalArgumentException(
                        "الملف غير موجود في resources/static: " + fileName +
                                "\nتأكد من وضع الملف في: src/main/resources/static/" + fileName
                );
            }

            String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            log.info("✅ [FetchService] تمت قراءة الملف بنجاح ({} حرف)", content.length());

            return new RitajFetchedPagesDto(content, null, null);

        } catch (IOException e) {
            throw new RuntimeException("فشل قراءة الملف: " + fileName, e);
        }
    }

    @Override
    @Deprecated
    public RitajFetchedPagesDto fetchArabicAndEnglishPages(String baseFileName) {
        // إعادة التوجيه إلى الميثود الجديد للتوافق مع الكود القديم
        return fetchJsonFile(baseFileName);
    }
}