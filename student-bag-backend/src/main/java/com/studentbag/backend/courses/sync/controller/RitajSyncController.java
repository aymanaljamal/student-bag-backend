package com.studentbag.backend.courses.sync.controller;

import com.studentbag.backend.courses.sync.dto.RitajSyncRequest;
import com.studentbag.backend.courses.sync.dto.RitajSyncResult;
import com.studentbag.backend.courses.sync.service.RitajDatabaseCleanupService;
import com.studentbag.backend.courses.sync.service.RitajSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ritaj-sync")
@RequiredArgsConstructor
public class RitajSyncController {

    private static final String DEFAULT_SOURCE_FILE =
            "course_data/STUDENT_BAG_FINAL_DATA";

    private final RitajSyncService ritajSyncService;
    private final RitajDatabaseCleanupService ritajDatabaseCleanupService;

    @PostMapping("/sync-term")
    public ResponseEntity<?> sync(@RequestBody RitajSyncRequest request) {
        Long institutionId = request.getInstitutionId();

        if (institutionId == null) {
            return ResponseEntity.badRequest().body("institutionId is required");
        }

        String sourceFile = StringUtils.hasText(request.getSourceFile())
                ? request.getSourceFile().trim()
                : DEFAULT_SOURCE_FILE;

        boolean clearOldData = request.getClearOldData() == null || request.getClearOldData();

        log.info("📥 [RitajSync] بدء مزامنة بيانات ريتاج للمؤسسة: {}, الملف: {}, clearOldData: {}",
                institutionId, sourceFile, clearOldData);

        try {
            if (clearOldData) {
                log.warn("🧹 [RitajSync] سيتم حذف بيانات الكورسات القديمة للمؤسسة رقم: {}", institutionId);
                ritajDatabaseCleanupService.clearInstitutionCourseData(institutionId);
            }

            RitajSyncResult result = ritajSyncService.syncTermFromRitaj(
                    institutionId,
                    sourceFile
            );

            log.info("✅ [RitajSync] تمت المزامنة بنجاح: {}", result);

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("❌ [RitajSync] خطأ في المدخلات: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("❌ [RitajSync] فشل غير متوقع في عملية المزامنة", e);
            return ResponseEntity.internalServerError()
                    .body("فشل غير متوقع في عملية المزامنة: " + e.getMessage());
        }
    }
}