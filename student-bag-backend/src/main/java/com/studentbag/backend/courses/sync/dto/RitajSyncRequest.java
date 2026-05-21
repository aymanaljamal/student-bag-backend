package com.studentbag.backend.courses.sync.dto;

import lombok.Data;

@Data
public class RitajSyncRequest {

    private Long institutionId;

    /**
     * مثال:
     * course_data/STUDENT_BAG_FINAL_DATA
     * أو:
     * course_data/STUDENT_BAG_FINAL_DATA.json
     */
    private String sourceFile;

    /**
     * true = يمسح بيانات الكورسات القديمة للمؤسسة قبل السنِك
     * false/null = يعمل sync بدون حذف
     */
    private Boolean clearOldData = true;
}