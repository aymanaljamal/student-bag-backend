package com.studentbag.backend.notifications.service;

import com.studentbag.backend.notifications.dto.request.SendInstitutionAdminMessageRequest;
import com.studentbag.backend.notifications.dto.request.UpdateInstitutionAdminMessageRequest;
import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageResponse;
import com.studentbag.backend.notifications.dto.response.InstitutionAdminMessageStatsResponse;
import com.studentbag.backend.users.entity.User;

import java.util.List;

public interface InstitutionAdminMessageService {

    InstitutionAdminMessageResponse sendToInstitutionAdmins(
            User sender,
            SendInstitutionAdminMessageRequest request
    );

    List<InstitutionAdminMessageResponse> getMySentMessages(
            User currentUser,
            int page,
            int size
    );

    List<InstitutionAdminMessageResponse> getAdminInboxMessages(
            User adminUser,
            int page,
            int size
    );

    InstitutionAdminMessageResponse getMessageDetails(
            User currentUser,
            Long messageId
    );

    InstitutionAdminMessageResponse updateMyMessage(
            User currentUser,
            Long messageId,
            UpdateInstitutionAdminMessageRequest request
    );

    void deleteMyMessage(
            User currentUser,
            Long messageId
    );

    InstitutionAdminMessageResponse markMessageAsReadByAdmin(
            User adminUser,
            Long messageId
    );

    InstitutionAdminMessageStatsResponse getAdminInstitutionMessageStats(
            User adminUser
    );
}