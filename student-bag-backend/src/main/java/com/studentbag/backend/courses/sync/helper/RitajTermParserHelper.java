package com.studentbag.backend.courses.sync.helper;

import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RitajTermParserHelper {

    // نمط للبحث عن الفصول الدراسية (مثال: الفصل الثاني 2025/2026 أو Second Semester 2025/2026)
    private static final Pattern TERM_PATTERN = Pattern.compile(
            "(الفصل\\s+(الأول|الثاني|الصيفي)\\s+\\d{4}/\\d{4})|((First|Second|Summer)\\s+Semester\\s+\\d{4}/\\d{4})",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * يستخرج اسم الفصل الدراسي من النص الكامل للملف
     */
    public String extractTermFromText(String text) {
        if (text == null || text.isBlank()) return null;

        // الطريقة الأولى: البحث باستخدام Regex (الأكثر دقة)
        Matcher matcher = TERM_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group().trim();
        }

        // الطريقة الثانية: البحث عن سطر يحتوي على التاريخ كخطة احتياطية
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            // ريتاج عادة يضع الفصل الدراسي في سطر يحتوي على "202" (للسنوات الحالية)
            if (trimmed.contains("202") && (trimmed.contains("Semester") || trimmed.contains("الفصل"))) {
                return trimmed;
            }
        }

        return null;
    }

    /**
     * ميثود قديمة (للخلف) في حال كنت لا تزال تستخدم Jsoup في أماكن أخرى،
     * لكن يفضل الاعتماد على extractTermFromText
     */
    @Deprecated
    public String extractCurrentDisplayedTerm(org.jsoup.nodes.Document doc) {
        if (doc == null) return null;
        org.jsoup.nodes.Element selectedOption = doc.select("select#term option[selected]").first();
        return selectedOption != null ? selectedOption.text().trim() : null;
    }
}