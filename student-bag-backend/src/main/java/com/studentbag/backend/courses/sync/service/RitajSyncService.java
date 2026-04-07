package com.studentbag.backend.courses.sync.service;

import com.studentbag.backend.courses.sync.dto.RitajSyncResult;

public interface RitajSyncService {

    RitajSyncResult syncTermFromRitaj(Long institutionId, String sourceUrl);
}