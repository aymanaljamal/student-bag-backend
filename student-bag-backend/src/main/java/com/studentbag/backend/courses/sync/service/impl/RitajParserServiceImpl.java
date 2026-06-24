package com.studentbag.backend.courses.sync.service.impl;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RitajParserServiceImpl implements RitajParserService {

    private final ObjectMapper objectMapper;

    private static final String F_EXTERNAL_ID = "externalId";
    private static final String F_CODE = "code";
    private static final String F_COURSE_INTERNAL_ID = "courseInternalId";

    private static final String F_NAME_AR = "nameArabic";
    private static final String F_NAME_EN = "nameEnglish";

    private static final String F_DESCRIPTION = "description";
    private static final String F_DESC_AR = "descriptionArabic";
    private static final String F_DESC_EN = "descriptionEnglish";

    private static final String F_ACADEMIC_LEVEL = "academicLevel";
    private static final String F_CREDIT_HOURS = "creditHours";

    private static final String F_FAC_EXT_ID = "facultyExternalId";
    private static final String F_DEPT_EXT_ID = "departmentExternalId";

    private static final String F_FAC_AR = "facultyNameArabic";
    private static final String F_FAC_EN = "facultyNameEnglish";
    private static final String F_DEPT_AR = "departmentNameArabic";
    private static final String F_DEPT_EN = "departmentNameEnglish";

    private static final String F_PROG_AR = "programNameArabic";
    private static final String F_PROG_EN = "programNameEnglish";

    private static final String F_IS_GENERATED_NAME = "isGeneratedName";
    private static final String F_IS_GENERATED_DESCRIPTION = "isGeneratedDescription";
    private static final String F_IS_LAB_COURSE = "isLabCourse";

    private static final String F_SECTIONS = "sections";
    private static final String F_SECTION_INTERNAL_ID = "sectionInternalId";
    private static final String F_SEC_NUM = "sectionNumber";
    private static final String F_SEC_TYPE = "sectionType";
    private static final String F_INSTR_AR = "instructorNameArabic";
    private static final String F_INSTR_EN = "instructorNameEnglish";
    private static final String F_COURSE_CODE = "courseCode";
    private static final String F_ENROLLED = "enrolled";
    private static final String F_CAPACITY = "capacity";
    private static final String F_PARENT_SEC = "parentLectureSectionNumber";
    private static final String F_IS_GENERATED_CAPACITY = "isGeneratedCapacity";

    private static final String F_SESSIONS = "sessions";
    private static final String F_DAY = "dayOfWeek";
    private static final String F_START = "startTime";
    private static final String F_END = "endTime";
    private static final String F_ROOM = "room";
    private static final String F_BUILDING = "building";
    private static final String F_CAMPUS = "campus";
    private static final String F_SESSION_INTERNAL_ID = "sessionInternalId";
    private static final String F_IS_GENERATED_SESSION = "isGeneratedSession";

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

            log.info("✅ [Parser] تم تحليل {} مساق من JSON normalized", result.size());

        } catch (Exception e) {
            log.error("❌ [Parser] فشل تحليل JSON: {}", e.getMessage(), e);
            throw new RuntimeException("فشل تحليل ملف JSON", e);
        }

        return result;
    }

    private RitajCourseDto parseCourseNode(JsonNode node) {
        String code = text(node, F_CODE);
        if (code == null) return null;

        RitajCourseDto dto = new RitajCourseDto();

        dto.setCode(code);
        dto.setCourseInternalId(text(node, F_COURSE_INTERNAL_ID));
        dto.setExternalId(firstNonBlank(
                text(node, F_EXTERNAL_ID),
                dto.getCourseInternalId(),
                code
        ));

        dto.setNameArabic(text(node, F_NAME_AR));
        dto.setNameEnglish(text(node, F_NAME_EN));

        dto.setDescription(text(node, F_DESCRIPTION));
        dto.setDescriptionArabic(text(node, F_DESC_AR));
        dto.setDescriptionEnglish(text(node, F_DESC_EN));

        dto.setAcademicLevel(text(node, F_ACADEMIC_LEVEL));
        dto.setCreditHours(intVal(node, F_CREDIT_HOURS));

        dto.setFacultyExternalId(text(node, F_FAC_EXT_ID));
        dto.setDepartmentExternalId(text(node, F_DEPT_EXT_ID));

        dto.setFacultyNameArabic(text(node, F_FAC_AR));
        dto.setFacultyNameEnglish(text(node, F_FAC_EN));
        dto.setDepartmentNameArabic(text(node, F_DEPT_AR));
        dto.setDepartmentNameEnglish(text(node, F_DEPT_EN));

        dto.setProgramNameArabic(text(node, F_PROG_AR));
        dto.setProgramNameEnglish(text(node, F_PROG_EN));

        dto.setIsGeneratedName(boolVal(node, F_IS_GENERATED_NAME));
        dto.setIsGeneratedDescription(boolVal(node, F_IS_GENERATED_DESCRIPTION));
        dto.setIsLabCourse(boolVal(node, F_IS_LAB_COURSE));

        List<RitajSectionDto> sections = new ArrayList<>();
        JsonNode sectionArray = node.path(F_SECTIONS);

        if (sectionArray.isArray()) {
            for (JsonNode sectionNode : sectionArray) {
                RitajSectionDto section = parseSectionNode(sectionNode, code);
                if (section != null) {
                    sections.add(section);
                }
            }
        }

        dto.setSections(sections);

        return dto;
    }

    private RitajSectionDto parseSectionNode(JsonNode node, String fallbackCourseCode) {
        RitajSectionDto section = new RitajSectionDto();

        String sectionNumber = firstNonBlank(text(node, F_SEC_NUM), "1");
        String sectionType = firstNonBlank(text(node, F_SEC_TYPE), "Lecture");
        String courseCode = firstNonBlank(text(node, F_COURSE_CODE), fallbackCourseCode);

        section.setSectionNumber(sectionNumber);
        section.setSectionType(sectionType);
        section.setCourseCode(courseCode);

        section.setSectionInternalId(text(node, F_SECTION_INTERNAL_ID));
        section.setExternalId(firstNonBlank(
                text(node, F_EXTERNAL_ID),
                section.getSectionInternalId(),
                courseCode + "-" + sectionType + "-" + sectionNumber
        ));

        section.setInstructorNameArabic(text(node, F_INSTR_AR));
        section.setInstructorNameEnglish(text(node, F_INSTR_EN));

        section.setCapacity(intVal(node, F_CAPACITY));
        section.setEnrolled(intVal(node, F_ENROLLED));

        section.setParentLectureSectionNumber(text(node, F_PARENT_SEC));
        section.setIsGeneratedCapacity(boolVal(node, F_IS_GENERATED_CAPACITY));

        List<RitajClassSessionDto> sessions = new ArrayList<>();
        JsonNode sessionArray = node.path(F_SESSIONS);

        if (sessionArray.isArray()) {
            for (JsonNode sessionNode : sessionArray) {
                RitajClassSessionDto session = parseSessionNode(sessionNode);
                if (session != null) {
                    sessions.add(session);
                }
            }
        }

        section.setSessions(sessions);

        return section;
    }

    private RitajClassSessionDto parseSessionNode(JsonNode node) {
        String dayStr = text(node, F_DAY);
        if (dayStr == null) {
            return null;
        }

        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(dayStr.trim().toUpperCase());
        } catch (Exception e) {
            log.warn("⚠️ [Parser] يوم غير معروف: {}", dayStr);
            return null;
        }

        LocalTime startTime = parseTime(text(node, F_START));
        LocalTime endTime = parseTime(text(node, F_END));

        if (startTime == null || endTime == null) {
            log.warn("⚠️ [Parser] وقت غير صالح: start={}, end={}", text(node, F_START), text(node, F_END));
            return null;
        }

        RitajClassSessionDto session = new RitajClassSessionDto();
        session.setDayOfWeek(dayOfWeek);
        session.setStartTime(startTime);
        session.setEndTime(endTime);

        session.setRoom(text(node, F_ROOM));
        session.setBuilding(text(node, F_BUILDING));
        session.setCampus(text(node, F_CAMPUS));

        session.setSessionInternalId(text(node, F_SESSION_INTERNAL_ID));
        session.setIsGeneratedSession(boolVal(node, F_IS_GENERATED_SESSION));

        return session;
    }

    private LocalTime parseTime(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        try {
            String[] parts = raw.trim().split(":");
            int hour = Integer.parseInt(parts[0].trim());
            int minute = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 0;

            return LocalTime.of(hour, minute);
        } catch (Exception e) {
            return null;
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) return null;

        String text = value.asText("").trim();
        return text.isEmpty() || text.equalsIgnoreCase("null") ? null : text;
    }

    private Integer intVal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) return null;

        if (value.isNumber()) {
            return value.asInt();
        }

        try {
            String text = value.asText("").trim();
            return text.isEmpty() ? null : Integer.parseInt(text);
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean boolVal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNull() || value.isMissingNode()) return null;

        if (value.isBoolean()) {
            return value.asBoolean();
        }

        String text = value.asText("").trim();
        if (text.isEmpty()) return null;

        if ("true".equalsIgnoreCase(text)) return true;
        if ("false".equalsIgnoreCase(text)) return false;

        return null;
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    @Override
    @Deprecated
    public List<RitajCourseDto> parseCourses(String arContent, String enContent) {
        throw new UnsupportedOperationException(
                "هذه الميثود مهملة. استخدم parseJson(jsonContent) مع ملف STUDENT_BAG_FINAL_DATA_normalized.json"
        );
    }
}
