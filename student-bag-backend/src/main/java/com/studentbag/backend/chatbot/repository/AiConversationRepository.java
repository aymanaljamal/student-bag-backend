package com.studentbag.backend.chatbot.repository;


import com.studentbag.backend.chatbot.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    List<AiConversation> findByStudentIdAndArchivedFalseOrderByUpdatedAtDesc(Long studentId);
}