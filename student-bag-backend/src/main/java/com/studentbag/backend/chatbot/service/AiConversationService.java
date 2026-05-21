package com.studentbag.backend.chatbot.service;


import com.studentbag.backend.chatbot.dto.request.CreateAiConversationRequest;
import com.studentbag.backend.chatbot.dto.response.AiConversationResponse;
import com.studentbag.backend.chatbot.dto.response.AiMessageResponse;

import java.util.List;

public interface AiConversationService {

    AiConversationResponse createConversation(CreateAiConversationRequest request);

    List<AiConversationResponse> getMyConversations();

    AiConversationResponse getConversation(Long conversationId);

    List<AiMessageResponse> getConversationMessages(Long conversationId);

    void archiveConversation(Long conversationId);

    void deleteConversation(Long conversationId);
}