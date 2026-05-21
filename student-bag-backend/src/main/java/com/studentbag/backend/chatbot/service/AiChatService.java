package com.studentbag.backend.chatbot.service;


import com.studentbag.backend.chatbot.dto.request.StudentAiChatRequest;
import com.studentbag.backend.chatbot.dto.response.StudentAiChatResponse;

public interface AiChatService {

    StudentAiChatResponse chat(StudentAiChatRequest request);
}