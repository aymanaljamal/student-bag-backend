package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.domain.enums.Season;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TermSyncMapper {

    /**
     * يحول الاسم المستخرج من ريتاج إلى كائن Term مهيكل.
     */
    public Term mapToEntity(String extractedTermName, Institution institution) {
        if (extractedTermName == null || extractedTermName.isBlank()) {
            return null;
        }

        Term term = new Term();
        term.setInstitution(institution);
        term.setName(extractedTermName.trim());

        String year = extractAcademicYear(extractedTermName);
        Season season = extractSeason(extractedTermName);

        term.setAcademicYear(year);
        term.setSeason(season);

        // توليد كود فريد وقصير للفصل الدراسي (مثلاً: 2025-2026-SPRING)
        term.setTermCode(generateCleanCode(year, season));

        term.setIsCurrent(true);

        // تواريخ تقريبية (يمكن تحديثها يدوياً من لوحة التحكم لاحقاً)
        term.setStartDate(LocalDate.now());
        term.setEndDate(LocalDate.now().plusMonths(4));

        return term;
    }

    private String generateCleanCode(String year, Season season) {
        // تحويل السنة من 2025/2026 إلى 2025-2026
        String cleanYear = year.replace("/", "-");
        return String.format("%s-%s", cleanYear, season.name());
    }

    private String extractAcademicYear(String termName) {
        // يدعم 2025/2026 أو 2025-2026
        Matcher matcher = Pattern.compile("(\\d{4}[/-]\\d{4})").matcher(termName);
        return matcher.find() ? matcher.group(1).replace("-", "/") : "UNKNOWN";
    }

    private Season extractSeason(String termName) {
        String lower = termName.toLowerCase();

        if (lower.contains("أول") || lower.contains("first") || lower.contains("fall")) {
            return Season.FALL;
        }
        if (lower.contains("ثاني") || lower.contains("second") || lower.contains("spring")) {
            return Season.SPRING;
        }
        if (lower.contains("صيف") || lower.contains("summer")) {
            return Season.SUMMER;
        }
        if (lower.contains("شتو") || lower.contains("winter")) {
            return Season.WINTER;
        }

        return Season.FALL;
    }
}