package com.studentbag.backend.chatbot.repository;
import com.studentbag.backend.chatbot.entity.AiMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    List<AiMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    Optional<AiMessage> findTopByConversationIdOrderByCreatedAtDesc(Long conversationId);
}