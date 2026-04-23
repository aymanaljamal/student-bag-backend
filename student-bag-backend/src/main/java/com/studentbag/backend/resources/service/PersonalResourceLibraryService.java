package com.studentbag.backend.resources.service;

import com.studentbag.backend.domain.enums.resources.ResourceCategory;
import com.studentbag.backend.domain.enums.resources.ResourceType;
import com.studentbag.backend.resources.dto.request.CopyAdminResourceToPersonalRequest;
import com.studentbag.backend.resources.dto.request.CopyPersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.CreatePersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.CreatePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.GenerateFoldersFromActiveScheduleRequest;
import com.studentbag.backend.resources.dto.request.MovePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.request.UpdatePersonalResourceFolderRequest;
import com.studentbag.backend.resources.dto.request.UpdatePersonalResourceItemRequest;
import com.studentbag.backend.resources.dto.response.PersonalLibraryOverviewResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderDetailsResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceFolderResponse;
import com.studentbag.backend.resources.dto.response.PersonalResourceItemResponse;
import com.studentbag.backend.resources.dto.response.ResourceOperationResponse;

import java.util.List;
import java.util.UUID;

public interface PersonalResourceLibraryService {

    PersonalLibraryOverviewResponse getLibraryOverview(UUID currentUserId);

    PersonalResourceFolderResponse getOrCreateRootFolder(UUID currentUserId);

    PersonalResourceFolderResponse createFolder(
            UUID currentUserId,
            CreatePersonalResourceFolderRequest request
    );

    PersonalResourceFolderResponse updateFolder(
            Long folderId,
            UUID currentUserId,
            UpdatePersonalResourceFolderRequest request
    );

    PersonalResourceFolderResponse getFolderById(
            Long folderId,
            UUID currentUserId
    );

    List<PersonalResourceFolderResponse> getTopFolders(UUID currentUserId);

    List<PersonalResourceFolderResponse> getChildFolders(
            Long parentFolderId,
            UUID currentUserId
    );

    List<PersonalResourceFolderResponse> getFoldersByCourse(
            UUID currentUserId,
            Long courseId
    );

    PersonalResourceFolderDetailsResponse getFolderDetails(
            Long folderId,
            UUID currentUserId
    );

    ResourceOperationResponse softDeleteFolder(
            Long folderId,
            UUID currentUserId
    );

    ResourceOperationResponse archiveFolder(
            Long folderId,
            UUID currentUserId
    );

    List<PersonalResourceFolderResponse> generateFoldersFromActiveSchedule(
            UUID currentUserId,
            GenerateFoldersFromActiveScheduleRequest request
    );

    PersonalResourceItemResponse createItem(
            UUID currentUserId,
            CreatePersonalResourceItemRequest request
    );

    PersonalResourceItemResponse updateItem(
            Long itemId,
            UUID currentUserId,
            UpdatePersonalResourceItemRequest request
    );

    PersonalResourceItemResponse getItemById(
            Long itemId,
            UUID currentUserId
    );

    List<PersonalResourceItemResponse> getItemsByFolder(
            Long folderId,
            UUID currentUserId
    );

    List<PersonalResourceItemResponse> getItemsByCourse(
            UUID currentUserId,
            Long courseId
    );

    List<PersonalResourceItemResponse> getItemsByCategory(
            UUID currentUserId,
            ResourceCategory category
    );

    List<PersonalResourceItemResponse> getItemsByType(
            UUID currentUserId,
            ResourceType resourceType
    );

    ResourceOperationResponse moveItem(
            Long itemId,
            UUID currentUserId,
            MovePersonalResourceItemRequest request
    );

    PersonalResourceItemResponse copyAdminResourceToPersonal(
            Long adminResourceId,
            UUID currentUserId,
            CopyAdminResourceToPersonalRequest request
    );

    PersonalResourceItemResponse copyPersonalItem(
            Long itemId,
            UUID currentUserId,
            CopyPersonalResourceItemRequest request
    );

    ResourceOperationResponse archiveItem(
            Long itemId,
            UUID currentUserId
    );

    ResourceOperationResponse softDeleteItem(
            Long itemId,
            UUID currentUserId
    );
}