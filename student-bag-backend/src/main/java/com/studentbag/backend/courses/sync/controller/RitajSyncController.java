package com.studentbag.backend.courses.sync.controller;

import com.studentbag.backend.courses.sync.dto.RitajSyncRequest;
import com.studentbag.backend.courses.sync.dto.RitajSyncResult;
import com.studentbag.backend.courses.sync.service.RitajSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ritaj-sync")
@RequiredArgsConstructor
public class RitajSyncController {

    private final RitajSyncService ritajSyncService;

    @PostMapping("/sync-term")
    public ResponseEntity<RitajSyncResult> sync(@RequestBody RitajSyncRequest request) {
        log.info("📥 [API Request] بدء مزامنة الفصل للمؤسسة رقم: {} باستخدام الملف: {}",
                request.getInstitutionId(), request.getSourceUrl());

        try {
            // تنفيذ المزامنة الموحدة (التي تدمج العربي والإنجليزي وتصلح الأخطاء)
            RitajSyncResult result = ritajSyncService.syncTermFromRitaj(
                    request.getInstitutionId(),
                    request.getSourceUrl() // هنا نرسل اسم الملف الأساسي مثل "Course"
            );

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("❌ خطأ في المدخلات: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ فشل غير متوقع في عملية المزامنة: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}