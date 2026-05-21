package com.studentbag.backend.chatbot.service.impl;
import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import com.studentbag.backend.chatbot.service.AiIntentResolverService;
import org.springframework.stereotype.Service;

@Service
public class AiIntentResolverServiceImpl implements AiIntentResolverService {

    @Override
    public AiConversationType resolveIntent(String question) {
        if (question == null || question.isBlank()) {
            return AiConversationType.GENERAL;
        }

        String q = question.toLowerCase();

        if (containsAny(q, "quiz", "كويز", "اختبار", "اسئلة", "أسئلة")) {
            return AiConversationType.QUIZ_GENERATION;
        }

        if (containsAny(q, "study plan", "خطة", "ادرس", "أدرس", "شو ادرس", "شو أدرس")) {
            return AiConversationType.STUDY_PLAN;
        }

        if (containsAny(q, "task", "deadline", "تاسك", "مهمة", "واجب", "موعد")) {
            return AiConversationType.TASK_HELP;
        }

        if (containsAny(q, "schedule", "جدول", "محاضرة", "قاعة", "غرفة", "كلاس")) {
            return AiConversationType.SCHEDULE_HELP;
        }

        if (containsAny(q, "note", "نوت", "ملاحظة", "شرح", "لخص", "تلخيص")) {
            return AiConversationType.NOTE_EXPLANATION;
        }

        if (containsAny(q, "resource", "file", "folder", "ملف", "مصدر", "رابط", "مرفق")) {
            return AiConversationType.RESOURCE_SEARCH;
        }

        if (containsAny(q, "event", "ايفنت", "فعالية", "حدث", "opportunity", "فرصة")) {
            return AiConversationType.EVENT_HELP;
        }

        if (containsAny(q, "grade", "gpa", "علامة", "معدل", "حساب")) {
            return AiConversationType.GRADE_ANALYSIS;
        }

        if (containsAny(q, "dashboard", "داشبورد", "ملخص", "وضعي")) {
            return AiConversationType.DASHBOARD_HELP;
        }

        return AiConversationType.GENERAL;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}