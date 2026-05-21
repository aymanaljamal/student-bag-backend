package com.studentbag.backend.chatbot.repository;
import com.studentbag.backend.chatbot.entity.AiContextSource;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface AiContextSourceRepository extends JpaRepository<AiContextSource, Long> {

    List<AiContextSource> findByMessageId(Long messageId);
}