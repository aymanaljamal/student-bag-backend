package com.studentbag.backend.courses.sync.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import com.studentbag.backend.courses.sync.service.RitajParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

/**
 * يقرأ ملف JSON المدمج (courses_v5.json) ويحوّله إلى قائمة RitajCourseDto.
 *
 * ملاحظات التصميم:
 * - مواد SP.TOP: نفس الكود بأسماء مختلفة → كل اسم entry مستقلة.
 * - أكثر من مدرس: الأسماء تكون comma-separated في nameArabic/nameEnglish.
 * - المختبر (Lab): يحتفظ بـ parentLectureSectionNumber ليُربط لاحقاً في SyncService.
 * - الغرف: كل session تحمل building + room منفصلَين (قد تختلف يوماً بيوم).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RitajParserServiceImpl implements RitajParserService {

    private final ObjectMapper objectMapper;

    // ---- JSON field names (كما في courses_v5.json) ----
    private static final String F_CODE             = "code";
    private static final String F_NAME_AR          = "nameArabic";
    private static final String F_NAME_EN          = "nameEnglish";
    private static final String F_ACADEMIC_LEVEL   = "academicLevel";
    private static final String F_CREDIT_HOURS     = "creditHours";
    private static final String F_FAC_AR           = "facultyNameArabic";
    private static final String F_FAC_EN           = "facultyNameEnglish";
    private static final String F_DEPT_AR          = "departmentNameArabic";
    private static final String F_DEPT_EN          = "departmentNameEnglish";
    private static final String F_PROG_AR          = "programNameArabic";
    private static final String F_PROG_EN          = "programNameEnglish";
    private static final String F_SECTIONS         = "sections";
    private static final String F_SEC_NUM          = "sectionNumber";
    private static final String F_SEC_TYPE         = "sectionType";
    private static final String F_INSTR_AR         = "instructorNameArabic";
    private static final String F_INSTR_EN         = "instructorNameEnglish";
    private static final String F_ENROLLED         = "enrolled";
    private static final String F_CAPACITY         = "capacity";
    private static final String F_PARENT_SEC       = "parentLectureSectionNumber";
    private static final String F_SESSIONS         = "sessions";
    private static final String F_DAY              = "dayOfWeek";
    private static final String F_START            = "startTime";
    private static final String F_END              = "endTime";
    private static final String F_ROOM             = "room";
    private static final String F_BUILDING         = "building";
    private static final String F_CAMPUS           = "campus";
    private static final String F_COURSE_CODE      = "courseCode";

    @Override
    public List<RitajCourseDto> parseJson(String jsonContent) {
        if (jsonContent == null || jsonContent.isBlank()) {
            log.warn("⚠️ [Parser] المحتوى فارغ");
            return Collections.emptyList();
        }

        List<RitajCourseDto> result = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(jsonContent);
            if (!root.isArray()) {
                throw new IllegalArgumentException("الملف لا يحتوي على مصفوفة JSON صالحة");
            }

            for (JsonNode courseNode : root) {
                RitajCourseDto dto = parseCourseNode(courseNode);
                if (dto != null && isNotBlank(dto.getCode())) {
                    result.add(dto);
                }
            }

            log.info("✅ [Parser] تم تحليل {} مساق من JSON", result.size());

        } catch (Exception e) {
            log.error("❌ [Parser] فشل تحليل JSON: {}", e.getMessage(), e);
            throw new RuntimeException("فشل تحليل ملف JSON", e);
        }

        return result;
    }

    // ---- Core parsing ----

    private RitajCourseDto parseCourseNode(JsonNode node) {
        String code = text(node, F_CODE);
        if (code == null) return null;

        RitajCourseDto dto = new RitajCourseDto();
        dto.setCode(code);
        dto.setExternalId(code);
        dto.setNameArabic(text(node, F_NAME_AR));
        dto.setNameEnglish(text(node, F_NAME_EN));
        dto.setAcademicLevel(text(node, F_ACADEMIC_LEVEL));
        dto.setCreditHours(intVal(node, F_CREDIT_HOURS));
        dto.setFacultyNameArabic(text(node, F_FAC_AR));
        dto.setFacultyNameEnglish(text(node, F_FAC_EN));
        dto.setDepartmentNameArabic(text(node, F_DEPT_AR));
        dto.setDepartmentNameEnglish(text(node, F_DEPT_EN));
        dto.setProgramNameArabic(text(node, F_PROG_AR));
        dto.setProgramNameEnglish(text(node, F_PROG_EN));

        List<RitajSectionDto> sections = new ArrayList<>();
        JsonNode secArray = node.path(F_SECTIONS);
        if (secArray.isArray()) {
            for (JsonNode secNode : secArray) {
                RitajSectionDto sec = parseSectionNode(secNode, code);
                if (sec != null) sections.add(sec);
            }
        }
        dto.setSections(sections);

        return dto;
    }

    private RitajSectionDto parseSectionNode(JsonNode node, String courseCode) {
        RitajSectionDto sec = new RitajSectionDto();

        String secNum  = text(node, F_SEC_NUM);
        String secType = text(node, F_SEC_TYPE);

        sec.setSectionNumber(secNum != null ? secNum : "1");
        sec.setSectionType(secType != null ? secType : "Lecture");
        sec.setCourseCode(courseCode);

        // أسماء المدرسين — قد تكون comma-separated لأكثر من مدرس
        sec.setInstructorNameArabic(text(node, F_INSTR_AR));
        sec.setInstructorNameEnglish(text(node, F_INSTR_EN));

        sec.setEnrolled(intVal(node, F_ENROLLED));
        sec.setCapacity(intVal(node, F_CAPACITY));

        // parentLectureSectionNumber → للمختبرات
        sec.setParentLectureSectionNumber(text(node, F_PARENT_SEC));

        // ExternalId = courseCode_sectionNumber_sectionType
        sec.setExternalId(courseCode + "_" + sec.getSectionNumber() + "_" + sec.getSectionType());

        // Sessions
        List<RitajClassSessionDto> sessions = new ArrayList<>();
        JsonNode sessArray = node.path(F_SESSIONS);
        if (sessArray.isArray()) {
            for (JsonNode sessNode : sessArray) {
                RitajClassSessionDto sess = parseSessionNode(sessNode);
                if (sess != null) sessions.add(sess);
            }
        }
        sec.setSessions(sessions);

        return sec;
    }

    private RitajClassSessionDto parseSessionNode(JsonNode node) {
        String dayStr = text(node, F_DAY);
        if (dayStr == null) return null;

        DayOfWeek day;
        try {
            day = DayOfWeek.valueOf(dayStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("⚠️ [Parser] يوم غير معروف: {}", dayStr);
            return null;
        }

        RitajClassSessionDto sess = new RitajClassSessionDto();
        sess.setDayOfWeek(day);
        sess.setStartTime(parseTime(text(node, F_START)));
        sess.setEndTime(parseTime(text(node, F_END)));
        sess.setRoom(text(node, F_ROOM));
        sess.setBuilding(text(node, F_BUILDING));
        sess.setCampus(text(node, F_CAMPUS));

        return sess;
    }

    // ---- Helpers ----

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            // يدعم "HH:mm:ss" و "HH:mm"
            String[] parts = raw.split(":");
            int hour   = Integer.parseInt(parts[0].trim());
            int minute = Integer.parseInt(parts[1].trim());
            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            log.warn("⚠️ [Parser] وقت غير صالح: {}", raw);
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode f = node.path(field);
        if (f.isNull() || f.isMissingNode()) return null;
        String val = f.asText("").trim();
        return val.isEmpty() ? null : val;
    }

    private Integer intVal(JsonNode node, String field) {
        JsonNode f = node.path(field);
        if (f.isNull() || f.isMissingNode()) return null;
        return f.isNumber() ? f.asInt() : null;
    }

    private boolean isNotBlank(String s) {
        return s != null && !s.isBlank();
    }

    // ---- Legacy support ----

    @Override
    @Deprecated
    public List<RitajCourseDto> parseCourses(String arContent, String enContent) {
        throw new UnsupportedOperationException(
                "هذه الميثود مهملة. استخدم parseJson(jsonContent) مع قراءة ملف courses_v5.json"
        );
    }
}