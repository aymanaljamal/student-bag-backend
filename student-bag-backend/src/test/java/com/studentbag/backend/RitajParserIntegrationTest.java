package com.studentbag.backend;

import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import com.studentbag.backend.courses.sync.service.impl.RitajParserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RitajParserIntegrationTest {

    @Autowired
    private RitajParserServiceImpl parserService;

    @Test
    @DisplayName("فحص مساقات دراسات إسلامية - الدكتور مشهور مشاهرة")
    void testHadithCourseParsing() {
        // نص يحاكي تماماً اللي بعتته مع المسافات الكبيرة (Tabs/Multiple Spaces)
        String content =
                "HADITH (ISLAMIC TRADITION) LITERATURE AND SCHOLARSHIP\n" +
                        "دراسات في الحديث النبوي\n" +
                        "Lecture 1  Mashhour Mousa Mashhour Mshahreh   46 S, M, W    12:00 - 12:50  A.Shaheen266\n" +
                        "Lecture 2  Mashhour Mousa Mashhour Mshahreh   49 T, R   11:00 - 12:20  A.Shaheen252\n" +
                        "Lecture 3  Mashhour Mousa Mashhour Mshahreh   45 S  10:00 - 10:50  A.Shaheen202\n";

        List<RitajCourseDto> courses = parserService.parseCourses(content, "");

        assertFalse(courses.isEmpty(), "يجب العثور على مساق الحديث");
        RitajCourseDto course = courses.get(0);

        // 1. فحص الشعبة الأولى (S, M, W)
        RitajSectionDto sec1 = course.getSections().get(0);
        assertEquals("Mashhour Mousa Mashhour Mshahreh", sec1.getInstructorNameArabic());
        assertEquals("A.Shaheen266", sec1.getSessions().get(0).getBuilding());
        assertEquals("266", sec1.getSessions().get(0).getRoom());

        // 2. فحص الشعبة الثانية (T, R)
        RitajSectionDto sec2 = course.getSections().get(1);
        assertEquals("A.Shaheen252", sec2.getSessions().get(0).getBuilding());
        assertEquals("252", sec2.getSessions().get(0).getRoom());

        // 3. فحص الشعبة الثالثة (تأكد إنها ما طلعت UNKNOWN)
        RitajSectionDto sec3 = course.getSections().get(2);
        assertNotEquals("UNKNOWN", sec3.getSessions().get(0).getBuilding(), "يجب أن لا يكون المبنى UNKNOWN");
    }
}