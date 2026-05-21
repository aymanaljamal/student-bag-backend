package com.studentbag.backend.chatbot.service;


import com.studentbag.backend.chatbot.entity.enums.AiConversationType;

public interface AiIntentResolverService {

    AiConversationType resolveIntent(String question);
}