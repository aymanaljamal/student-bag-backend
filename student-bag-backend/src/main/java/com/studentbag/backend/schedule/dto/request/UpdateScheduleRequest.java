package com.studentbag.backend.schedule.dto.request;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateScheduleRequest {

    private List<UpdateScheduleEntryRequest> entries = new ArrayList<>();

    /**
     * كل الشعب المختارة في الجدول.
     * السيرفس يستخدمها لإضافة hidden all-day entries للكورسات التي لا تملك entry فعلي.
     */
    private List<Long> selectedCourseSectionIds = new ArrayList<>();
}