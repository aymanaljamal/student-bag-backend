package com.studentbag.backend.resources.service;

import com.studentbag.backend.resources.dto.response.LinkedNoteSummaryResponse;
import com.studentbag.backend.resources.dto.response.LinkedTaskSummaryResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderDetailsResponse;
import com.studentbag.backend.resources.dto.response.ResourceCourseSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface ResourceIntegrationService {

    List<ResourceCourseSummaryResponse> getActiveScheduleCoursesForLibrary(
            UUID currentUserId,
            Long termId
    );

    List<LinkedNoteSummaryResponse> getLinkedNotesByCourse(
            UUID currentUserId,
            Long courseId
    );

    List<LinkedTaskSummaryResponse> getLinkedTasksByCourse(
            UUID currentUserId,
            Long courseId
    );

    PersonalResourceFolderDetailsResponse buildCourseFolderDetails(
            Long folderId,
            UUID currentUserId
    );
}