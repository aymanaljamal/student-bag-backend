package com.studentbag.backend.chatbot.repository;


import com.studentbag.backend.chatbot.entity.AiSavedPrompt;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiSavedPromptRepository extends JpaRepository<AiSavedPrompt, Long> {

    Optional<AiSavedPrompt> findFirstByTypeAndActiveTrueOrderByCreatedAtDesc(AiConversationType type);
}