package com.studentbag.backend.chatbot.service;


import com.studentbag.backend.chatbot.entity.enums.AiContextSourceType;

public interface AiContextSourceService {

    void saveSource(
            Long messageId,
            AiContextSourceType sourceType,
            Long sourceId,
            String sourceTitle
    );
}