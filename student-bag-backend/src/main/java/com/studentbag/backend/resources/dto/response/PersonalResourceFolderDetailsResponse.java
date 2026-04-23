package com.studentbag.backend.resources.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PersonalResourceFolderDetailsResponse {
    private PersonalResourceFolderResponse folder;
    private List<PersonalResourceFolderResponse> childFolders;
    private List<PersonalResourceItemResponse> items;
    private List<LinkedNoteSummaryResponse> linkedNotes;
    private List<LinkedTaskSummaryResponse> linkedTasks;
}