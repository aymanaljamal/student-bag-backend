package com.studentbag.backend.courses.sync.mapper;

import com.studentbag.backend.courses.entity.*;
import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import com.studentbag.backend.courses.sync.helper.CourseCodeMetadataHelper;
import com.studentbag.backend.courses.sync.helper.RoomParsingHelper;
import com.studentbag.backend.domain.enums.courses.SectionType;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.instructor.entity.Instructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RitajSyncMapper {

    private final CourseCodeMetadataHelper courseCodeMetadataHelper;
    private final RoomParsingHelper roomParsingHelper;

    public void mapCourseDtoToFaculty(RitajCourseDto dto, Faculty faculty, Institution institution) {
        faculty.setInstitution(institution);
        // نستخدم الـ Faculty Name Arabic كمعرف خارجي فريد للكلية
        String nameAr = safeOrDefault(dto.getFacultyNameArabic(), "كليات أخرى");
        faculty.setExternalId(nameAr);
        faculty.setNameArabic(nameAr);
        faculty.setNameEnglish(safeOrDefault(dto.getFacultyNameEnglish(), nameAr));
        faculty.setIsActive(true);
    }

    public void mapCourseDtoToDepartment(RitajCourseDto dto, Department department, Faculty faculty) {
        department.setFaculty(faculty);
        String nameAr = safeOrDefault(dto.getDepartmentNameArabic(), "دائرة عامة");
        department.setExternalId(nameAr);
        department.setNameArabic(nameAr);
        department.setNameEnglish(safeOrDefault(dto.getDepartmentNameEnglish(), nameAr));
        department.setIsActive(true);
    }

    public void mapCourseDtoToCourse(RitajCourseDto dto, Course course, Institution institution, Department department) {
        String code = safeOrDefault(dto.getCode(), "UNKNOWN");
        course.setInstitution(institution);
        course.setDepartment(department);
        course.setExternalId(code); // في بيرزيت كود المادة هو المعرف الفريد
        course.setCode(code);
        course.setNameArabic(safeOrDefault(dto.getNameArabic(), code));
        course.setNameEnglish(safeOrDefault(dto.getNameEnglish(), code));

        // إذا لم تكن الساعات موجودة في الملف، نستنتجها من الكود (مثلاً COMP231 -> 3 ساعات)
        int hours = (dto.getCreditHours() != null && dto.getCreditHours() > 0)
                ? dto.getCreditHours()
                : courseCodeMetadataHelper.extractCreditHours(code);
        course.setCreditHours(hours);

        course.setLevel(courseCodeMetadataHelper.extractAcademicLevel(code));
        course.setIsActive(true);
    }

    public void mapSectionDtoToCourseSection(
            RitajSectionDto dto,
            CourseSection section,
            Course course,
            Term term,
            Instructor instructor
    ) {
        section.setCourse(course);
        section.setTerm(term);
        // المعرف الخارجي للشعبة يكون (كود المادة + رقم الشعبة) لضمان عدم التكرار
        section.setExternalId(course.getCode() + "_" + dto.getSectionNumber());
        section.setSectionNumber(safeOrDefault(dto.getSectionNumber(), "1"));
        section.setSectionType(mapSectionType(dto.getSectionType()));
        section.setInstructor(instructor);

        // ضبط السعات الافتراضية
        section.setCapacity(dto.getCapacity() == null || dto.getCapacity() == 0 ? 40 : dto.getCapacity());
        section.setEnrolled(dto.getEnrolled() == null ? 0 : dto.getEnrolled());
        section.setIsOfficial(true);
    }

    public ClassSession toClassSessionEntity(RitajClassSessionDto dto, CourseSection section) {
        ClassSession session = new ClassSession();
        session.setCourseSection(section);
        session.setDayOfWeek(dto.getDayOfWeek());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());

        // استخدام الهيلبر المطور لتحليل "مكان المحاضرة"
        // نرسل نص الغرفة (الذي يحتوي الآن على المبنى والقاعة معاً من الـ Parser)
        RoomParsingHelper.ParsedRoom parsed = roomParsingHelper.parse(dto.getRoom());

        // تخزين الاسم العربي للمبنى في حقل المبنى وقرار الأونلاين بناءً على الكود
        session.setBuilding(parsed.buildingArabic());
        session.setRoom(parsed.room());
        session.setIsOnline("ONLINE".equalsIgnoreCase(parsed.buildingCode()));

        return session;
    }

    public SectionType mapSectionType(String value) {
        if (value == null) return SectionType.LECTURE;
        String normalized = value.trim().toUpperCase();
        if (normalized.contains("LAB") || normalized.contains("مختبر")) return SectionType.LAB;
        if (normalized.contains("DISCUSSION") || normalized.contains("نقاش")) return SectionType.LECTURE;
        return SectionType.LECTURE;
    }

    private String safeTrim(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return (trimmed.isEmpty() || trimmed.equalsIgnoreCase("N/A")) ? null : trimmed;
    }

    private String safeOrDefault(String value, String defaultValue) {
        String trimmed = safeTrim(value);
        return trimmed == null ? defaultValue : trimmed;
    }
}