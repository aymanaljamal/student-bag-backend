package com.studentbag.backend.chatbot.controller;

import com.studentbag.backend.chatbot.dto.request.CreateAiConversationRequest;
import com.studentbag.backend.chatbot.dto.request.StudentAiChatRequest;
import com.studentbag.backend.chatbot.dto.response.AiConversationResponse;
import com.studentbag.backend.chatbot.dto.response.AiMessageResponse;
import com.studentbag.backend.chatbot.dto.response.StudentAiChatResponse;
import com.studentbag.backend.chatbot.service.AiChatService;
import com.studentbag.backend.chatbot.service.AiConversationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class AiController {

    private final AiChatService aiChatService;
    private final AiConversationService aiConversationService;

    @PostMapping("/chat")
    public StudentAiChatResponse chat(
            @Valid @RequestBody StudentAiChatRequest request
    ) {
        return aiChatService.chat(request);
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public AiConversationResponse createConversation(
            @Valid @RequestBody CreateAiConversationRequest request
    ) {
        return aiConversationService.createConversation(request);
    }

    @GetMapping("/conversations")
    public List<AiConversationResponse> getMyConversations() {
        return aiConversationService.getMyConversations();
    }

    @GetMapping("/conversations/{conversationId}")
    public AiConversationResponse getConversation(
            @PathVariable Long conversationId
    ) {
        return aiConversationService.getConversation(conversationId);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public List<AiMessageResponse> getConversationMessages(
            @PathVariable Long conversationId
    ) {
        return aiConversationService.getConversationMessages(conversationId);
    }

    @PatchMapping("/conversations/{conversationId}/archive")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveConversation(
            @PathVariable Long conversationId
    ) {
        aiConversationService.archiveConversation(conversationId);
    }

    @DeleteMapping("/conversations/{conversationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(
            @PathVariable Long conversationId
    ) {
        aiConversationService.deleteConversation(conversationId);
    }
}