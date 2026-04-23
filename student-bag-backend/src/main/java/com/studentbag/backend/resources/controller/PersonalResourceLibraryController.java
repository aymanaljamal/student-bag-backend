package com.studentbag.backend.resources.controller;

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
import com.studentbag.backend.resources.service.PersonalResourceLibraryService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for the student's private/personal resource library.
 *
 * <p>This controller supports:
 * <ul>
 *     <li>Root folder management</li>
 *     <li>Manual folder creation/update/archive/delete</li>
 *     <li>Folder generation from active schedule</li>
 *     <li>Personal resource items CRUD-like operations</li>
 *     <li>Copying public resources into personal library</li>
 *     <li>Copying personal items</li>
 *     <li>Moving, archiving, deleting items</li>
 *     <li>Folder detail payload including linked notes/tasks</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/resources/personal")
@RequiredArgsConstructor
public class PersonalResourceLibraryController {

    private final PersonalResourceLibraryService personalResourceLibraryService;
    private final UserRepository userRepository;

    /**
     * Resolves the current authenticated user UUID from JWT email.
     */
    private UUID getCurrentUserId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("Unauthorized");
        }

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        return user.getId();
    }

    /**
     * Returns overview of the personal library:
     * root folder, top folders, and active schedule courses.
     */
    @GetMapping("/overview")
    public ResponseEntity<PersonalLibraryOverviewResponse> getLibraryOverview(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getLibraryOverview(currentUserId)
        );
    }

    /**
     * Returns existing root folder or creates one automatically.
     */
    @GetMapping("/root")
    public ResponseEntity<PersonalResourceFolderResponse> getOrCreateRootFolder(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getOrCreateRootFolder(currentUserId)
        );
    }

    /**
     * Creates a new personal folder.
     */
    @PostMapping("/folders")
    public ResponseEntity<PersonalResourceFolderResponse> createFolder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePersonalResourceFolderRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.createFolder(currentUserId, request)
        );
    }

    /**
     * Updates a personal folder.
     */
    @PutMapping("/folders/{folderId}")
    public ResponseEntity<PersonalResourceFolderResponse> updateFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePersonalResourceFolderRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.updateFolder(folderId, currentUserId, request)
        );
    }

    /**
     * Returns a folder by id.
     */
    @GetMapping("/folders/{folderId}")
    public ResponseEntity<PersonalResourceFolderResponse> getFolderById(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getFolderById(folderId, currentUserId)
        );
    }

    /**
     * Returns top-level folders under the root folder.
     */
    @GetMapping("/folders/top")
    public ResponseEntity<List<PersonalResourceFolderResponse>> getTopFolders(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getTopFolders(currentUserId)
        );
    }

    /**
     * Returns child folders for a given parent folder.
     */
    @GetMapping("/folders/{parentFolderId}/children")
    public ResponseEntity<List<PersonalResourceFolderResponse>> getChildFolders(
            @PathVariable Long parentFolderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getChildFolders(parentFolderId, currentUserId)
        );
    }

    /**
     * Returns folders linked to a specific course.
     */
    @GetMapping("/folders/course/{courseId}")
    public ResponseEntity<List<PersonalResourceFolderResponse>> getFoldersByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getFoldersByCourse(currentUserId, courseId)
        );
    }

    /**
     * Returns full folder details:
     * child folders, items, linked notes, linked tasks.
     */
    @GetMapping("/folders/{folderId}/details")
    public ResponseEntity<PersonalResourceFolderDetailsResponse> getFolderDetails(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getFolderDetails(folderId, currentUserId)
        );
    }

    /**
     * Soft deletes a folder.
     */
    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<ResourceOperationResponse> softDeleteFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.softDeleteFolder(folderId, currentUserId)
        );
    }

    /**
     * Archives a folder.
     */
    @PostMapping("/folders/{folderId}/archive")
    public ResponseEntity<ResourceOperationResponse> archiveFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.archiveFolder(folderId, currentUserId)
        );
    }

    /**
     * Generates system folders based on active schedule courses.
     */
    @PostMapping("/folders/generate-from-active-schedule")
    public ResponseEntity<List<PersonalResourceFolderResponse>> generateFoldersFromActiveSchedule(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) GenerateFoldersFromActiveScheduleRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        GenerateFoldersFromActiveScheduleRequest safeRequest =
                request != null ? request : new GenerateFoldersFromActiveScheduleRequest();

        return ResponseEntity.ok(
                personalResourceLibraryService.generateFoldersFromActiveSchedule(currentUserId, safeRequest)
        );
    }

    /**
     * Creates a personal resource item.
     *
     * <p>The file itself is assumed to have already been uploaded to Firebase,
     * and the request contains fileUrl / metadata only.</p>
     */
    @PostMapping("/items")
    public ResponseEntity<PersonalResourceItemResponse> createItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreatePersonalResourceItemRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.createItem(currentUserId, request)
        );
    }

    /**
     * Updates a personal resource item.
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<PersonalResourceItemResponse> updateItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePersonalResourceItemRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.updateItem(itemId, currentUserId, request)
        );
    }

    /**
     * Returns a personal resource item by id.
     */
    @GetMapping("/items/{itemId}")
    public ResponseEntity<PersonalResourceItemResponse> getItemById(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getItemById(itemId, currentUserId)
        );
    }

    /**
     * Returns all items inside a specific folder.
     */
    @GetMapping("/items/folder/{folderId}")
    public ResponseEntity<List<PersonalResourceItemResponse>> getItemsByFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getItemsByFolder(folderId, currentUserId)
        );
    }

    /**
     * Returns all items linked to a specific course.
     */
    @GetMapping("/items/course/{courseId}")
    public ResponseEntity<List<PersonalResourceItemResponse>> getItemsByCourse(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getItemsByCourse(currentUserId, courseId)
        );
    }

    /**
     * Returns all items filtered by category.
     */
    @GetMapping("/items/category/{category}")
    public ResponseEntity<List<PersonalResourceItemResponse>> getItemsByCategory(
            @PathVariable ResourceCategory category,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getItemsByCategory(currentUserId, category)
        );
    }

    /**
     * Returns all items filtered by resource type.
     */
    @GetMapping("/items/type/{resourceType}")
    public ResponseEntity<List<PersonalResourceItemResponse>> getItemsByType(
            @PathVariable ResourceType resourceType,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.getItemsByType(currentUserId, resourceType)
        );
    }

    /**
     * Moves a personal resource item to another folder.
     */
    @PostMapping("/items/{itemId}/move")
    public ResponseEntity<ResourceOperationResponse> moveItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MovePersonalResourceItemRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.moveItem(itemId, currentUserId, request)
        );
    }

    /**
     * Copies an approved public resource into the student's personal library.
     */
    @PostMapping("/items/copy-from-admin/{adminResourceId}")
    public ResponseEntity<PersonalResourceItemResponse> copyAdminResourceToPersonal(
            @PathVariable Long adminResourceId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) CopyAdminResourceToPersonalRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        CopyAdminResourceToPersonalRequest safeRequest =
                request != null ? request : new CopyAdminResourceToPersonalRequest();

        return ResponseEntity.ok(
                personalResourceLibraryService.copyAdminResourceToPersonal(adminResourceId, currentUserId, safeRequest)
        );
    }

    /**
     * Copies a personal item inside the student's own library.
     */
    @PostMapping("/items/{itemId}/copy")
    public ResponseEntity<PersonalResourceItemResponse> copyPersonalItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody(required = false) CopyPersonalResourceItemRequest request
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);

        CopyPersonalResourceItemRequest safeRequest =
                request != null ? request : new CopyPersonalResourceItemRequest();

        return ResponseEntity.ok(
                personalResourceLibraryService.copyPersonalItem(itemId, currentUserId, safeRequest)
        );
    }

    /**
     * Archives a personal item.
     */
    @PostMapping("/items/{itemId}/archive")
    public ResponseEntity<ResourceOperationResponse> archiveItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.archiveItem(itemId, currentUserId)
        );
    }

    /**
     * Soft deletes a personal item.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ResourceOperationResponse> softDeleteItem(
            @PathVariable Long itemId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UUID currentUserId = getCurrentUserId(userDetails);
        return ResponseEntity.ok(
                personalResourceLibraryService.softDeleteItem(itemId, currentUserId)
        );
    }
}