package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PersonalLibraryOverviewResponse {
    private PersonalResourceFolderResponse rootFolder;
    private List<PersonalResourceFolderResponse> topFolders;
    private List<ResourceCourseSummaryResponse> activeScheduleCourses;
}