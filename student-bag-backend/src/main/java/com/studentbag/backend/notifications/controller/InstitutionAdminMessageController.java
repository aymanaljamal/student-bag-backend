package com.studentbag.backend.notifications.controller;

import com.studentbag.backend.notifications.dto.request.SendInstitutionAdminMessageRequest;
import com.studentbag.backend.notifications.dto.request.UpdateInstitutionAdminMessageRequest;
import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageResponse;
import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageStatsResponse;
import com.studentbag.backend.notifications.service.InstitutionAdminMessageService;
import com.studentbag.backend.notifications.service.impl.CurrentNotificationUserResolver;
import com.studentbag.backend.users.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class InstitutionAdminMessageController {

    private final CurrentNotificationUserResolver currentUserResolver;
    private final InstitutionAdminMessageService institutionAdminMessageService;

    // =========================================================
    // Sender Side
    // Base path: /api/notifications/institution-messages
    // =========================================================

    @PostMapping("/api/notifications/institution-messages")
    public ResponseEntity<InstitutionAdminMessageResponse> sendMessageToInstitutionAdmins(
            Authentication authentication,
            @Valid @RequestBody SendInstitutionAdminMessageRequest request
    ) {
        System.out.println("ENTERED sendMessageToInstitutionAdmins");

        User sender = currentUserResolver.resolve(authentication);

        InstitutionAdminMessageResponse response =
                institutionAdminMessageService.sendToInstitutionAdmins(sender, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/notifications/institution-messages/my")
    public ResponseEntity<List<InstitutionAdminMessageResponse>> getMySentMessages(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        System.out.println("ENTERED getMySentMessages");
        System.out.println("AUTH = " + authentication);
        System.out.println("AUTHORITIES = " + authentication.getAuthorities());

        User currentUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.getMySentMessages(currentUser, page, size)
        );
    }

    @GetMapping("/api/notifications/institution-messages/{messageId}")
    public ResponseEntity<InstitutionAdminMessageResponse> getMyMessageDetails(
            Authentication authentication,
            @PathVariable Long messageId
    ) {
        System.out.println("ENTERED getMyMessageDetails");

        User currentUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.getMessageDetails(currentUser, messageId)
        );
    }

    @PutMapping("/api/notifications/institution-messages/{messageId}")
    public ResponseEntity<InstitutionAdminMessageResponse> updateMyMessage(
            Authentication authentication,
            @PathVariable Long messageId,
            @Valid @RequestBody UpdateInstitutionAdminMessageRequest request
    ) {
        System.out.println("ENTERED updateMyMessage");

        User currentUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.updateMyMessage(currentUser, messageId, request)
        );
    }

    @DeleteMapping("/api/notifications/institution-messages/{messageId}")
    public ResponseEntity<Void> deleteMyMessage(
            Authentication authentication,
            @PathVariable Long messageId
    ) {
        System.out.println("ENTERED deleteMyMessage");

        User currentUser = currentUserResolver.resolve(authentication);

        institutionAdminMessageService.deleteMyMessage(currentUser, messageId);

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // Admin Side
    // Base path: /api/notifications/institution-admin/messages
    // =========================================================

    @GetMapping("/api/notifications/institution-admin/messages/admin-inbox")
    public ResponseEntity<List<InstitutionAdminMessageResponse>> getAdminInboxMessages(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        System.out.println("ENTERED getAdminInboxMessages");

        User adminUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.getAdminInboxMessages(adminUser, page, size)
        );
    }

    @GetMapping("/api/notifications/institution-admin/messages/{messageId}")
    public ResponseEntity<InstitutionAdminMessageResponse> getAdminMessageDetails(
            Authentication authentication,
            @PathVariable Long messageId
    ) {
        System.out.println("ENTERED getAdminMessageDetails");

        User adminUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.getMessageDetails(adminUser, messageId)
        );
    }

    @PatchMapping("/api/notifications/institution-admin/messages/{messageId}/read")
    public ResponseEntity<InstitutionAdminMessageResponse> markMessageAsReadByAdmin(
            Authentication authentication,
            @PathVariable Long messageId
    ) {
        System.out.println("ENTERED markMessageAsReadByAdmin");

        User adminUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.markMessageAsReadByAdmin(adminUser, messageId)
        );
    }

    @GetMapping("/api/notifications/institution-admin/messages/stats")
    public ResponseEntity<InstitutionAdminMessageStatsResponse> getAdminInstitutionMessageStats(
            Authentication authentication
    ) {
        System.out.println("ENTERED getAdminInstitutionMessageStats");

        User adminUser = currentUserResolver.resolve(authentication);

        return ResponseEntity.ok(
                institutionAdminMessageService.getAdminInstitutionMessageStats(adminUser)
        );
    }
}