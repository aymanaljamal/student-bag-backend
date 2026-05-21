package com.studentbag.backend.chatbot.service;


import com.studentbag.backend.chatbot.dto.openai.OpenAiChatRequest;
import com.studentbag.backend.chatbot.dto.openai.OpenAiChatResponse;

public interface OpenAiClientService {

    OpenAiChatResponse sendChatRequest(OpenAiChatRequest request);
}