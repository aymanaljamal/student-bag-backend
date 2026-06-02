package com.studentbag.backend.chatbot.service.impl;

import com.studentbag.backend.chatbot.entity.enums.AiConversationType;
import com.studentbag.backend.chatbot.service.AiIntentResolverService;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Service
public class AiIntentResolverServiceImpl implements AiIntentResolverService {

    @Override
    public AiConversationType resolveIntent(String question) {
        if (question == null || question.isBlank()) {
            return AiConversationType.GENERAL;
        }

        String q = normalize(question);

        Map<AiConversationType, Integer> scores = new EnumMap<>(AiConversationType.class);

        addScore(
                scores,
                AiConversationType.QUIZ_GENERATION,
                q,
                4,
                "quiz",
                "quizzes",
                "question",
                "questions",
                "exam",
                "test",
                "mcq",
                "multiple choice",
                "true false",
                "كويز",
                "اختبار",
                "امتحان",
                "اسئله",
                "اسئلة",
                "سؤال",
                "صح وخطا",
                "صح او خطا",
                "اختيارات",
                "اختيار من متعدد",
                "تدريب",
                "دربني"
        );

        addScore(
                scores,
                AiConversationType.STUDY_PLAN,
                q,
                4,
                "study plan",
                "plan",
                "study schedule",
                "revision plan",
                "prepare for exam",
                "how to study",
                "خطة",
                "خطه",
                "ادرس",
                "اذاكر",
                "ماذا ادرس",
                "شو ادرس",
                "كيف ادرس",
                "راجعلي",
                "مراجعه",
                "مراجعة",
                "تحضير",
                "نظملي",
                "رتبلي دراستي"
        );

        addScore(
                scores,
                AiConversationType.TASK_HELP,
                q,
                4,
                "task",
                "tasks",
                "deadline",
                "deadlines",
                "assignment",
                "assignments",
                "todo",
                "to do",
                "due",
                "due date",
                "مهمه",
                "مهمة",
                "مهام",
                "تاسك",
                "تاسكات",
                "واجب",
                "واجبات",
                "موعد تسليم",
                "تسليم",
                "دедلاين",
                "ديدلاين",
                "المطلوب مني",
                "شو علي"
        );

        addScore(
                scores,
                AiConversationType.SCHEDULE_HELP,
                q,
                4,
                "schedule",
                "timetable",
                "calendar",
                "class",
                "classes",
                "lecture",
                "lectures",
                "room",
                "hall",
                "location",
                "today class",
                "next class",
                "جدول",
                "جداول",
                "محاضره",
                "محاضرة",
                "محاضرات",
                "سكشن",
                "كلاس",
                "قاعة",
                "قاعه",
                "غرفة",
                "غرفه",
                "مكان المحاضره",
                "مكان المحاضرة",
                "وين محاضرتي",
                "عندي اليوم"
        );

        addScore(
                scores,
                AiConversationType.NOTE_EXPLANATION,
                q,
                4,
                "note",
                "notes",
                "explain",
                "explanation",
                "summary",
                "summarize",
                "simplify",
                "understand",
                "شرح",
                "اشرح",
                "فسر",
                "وضح",
                "لخص",
                "تلخيص",
                "ملخص",
                "ملاحظه",
                "ملاحظة",
                "ملاحظات",
                "نوت",
                "نوتس",
                "افهمني",
                "بسطلي"
        );

        addScore(
                scores,
                AiConversationType.RESOURCE_SEARCH,
                q,
                4,
                "resource",
                "resources",
                "file",
                "files",
                "folder",
                "folders",
                "attachment",
                "attachments",
                "link",
                "pdf",
                "document",
                "material",
                "materials",
                "ملف",
                "ملفات",
                "مصدر",
                "مصادر",
                "مرفق",
                "مرفقات",
                "رابط",
                "روابط",
                "فولدر",
                "مجلد",
                "pdf",
                "بي دي اف",
                "مادة",
                "مواد",
                "مرجع",
                "مراجع"
        );

        addScore(
                scores,
                AiConversationType.EVENT_HELP,
                q,
                4,
                "event",
                "events",
                "opportunity",
                "opportunities",
                "workshop",
                "training",
                "internship",
                "volunteer",
                "registration",
                "فعاليه",
                "فعالية",
                "فعاليات",
                "حدث",
                "ايفنت",
                "فرصه",
                "فرصة",
                "فرص",
                "تدريب",
                "ورشه",
                "ورشة",
                "تطوع",
                "تسجيل",
                "سجلني",
                "شركة",
                "شركه"
        );

        addScore(
                scores,
                AiConversationType.GRADE_ANALYSIS,
                q,
                4,
                "grade",
                "grades",
                "gpa",
                "cgpa",
                "mark",
                "marks",
                "average",
                "percentage",
                "calculate gpa",
                "علامه",
                "علامة",
                "علامات",
                "معدل",
                "معدلي",
                "المعدل",
                "نسبة",
                "نسبه",
                "احسب معدلي",
                "احسب العلامه",
                "احسب العلامة",
                "كم بجيب",
                "كم لازم اجيب",
                "تراكمي",
                "فصلي"
        );

        addScore(
                scores,
                AiConversationType.DASHBOARD_HELP,
                q,
                4,
                "dashboard",
                "overview",
                "summary",
                "status",
                "progress",
                "today focus",
                "my day",
                "داشبورد",
                "لوحة التحكم",
                "ملخصي",
                "ملخص",
                "وضعي",
                "وضع",
                "تقدمي",
                "اليوم",
                "شو عندي",
                "نظرة عامة",
                "نظره عامه"
        );

        AiConversationType bestType = AiConversationType.GENERAL;
        int bestScore = 0;

        for (Map.Entry<AiConversationType, Integer> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestType = entry.getKey();
            }
        }

        return bestScore > 0 ? bestType : AiConversationType.GENERAL;
    }

    private void addScore(
            Map<AiConversationType, Integer> scores,
            AiConversationType type,
            String text,
            int weight,
            String... keywords
    ) {
        int score = 0;

        for (String keyword : keywords) {
            String normalizedKeyword = normalize(keyword);

            if (normalizedKeyword.isBlank()) {
                continue;
            }

            if (text.contains(normalizedKeyword)) {
                score += weight;
            }
        }

        if (score > 0) {
            scores.merge(type, score, Integer::sum);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        return value
                .toLowerCase(Locale.ROOT)
                .replace('أ', 'ا')
                .replace('إ', 'ا')
                .replace('آ', 'ا')
                .replace('ى', 'ي')
                .replace('ة', 'ه')
                .replace('ؤ', 'و')
                .replace('ئ', 'ي')
                .replaceAll("[\\u064B-\\u065F\\u0670]", "")
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}