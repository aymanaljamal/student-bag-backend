package com.studentbag.backend.chatbot.repository;
import com.studentbag.backend.chatbot.entity.AiUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
    List<AiUsageLog> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}