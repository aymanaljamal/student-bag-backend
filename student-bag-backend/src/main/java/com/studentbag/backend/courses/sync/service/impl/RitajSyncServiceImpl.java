package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.entity.*;
import com.studentbag.backend.courses.repository.*;
import com.studentbag.backend.courses.sync.dto.*;
import com.studentbag.backend.courses.sync.helper.RitajTermParserHelper;
import com.studentbag.backend.courses.sync.mapper.*;
import com.studentbag.backend.courses.sync.service.RitajFetchService;
import com.studentbag.backend.courses.sync.service.RitajParserService;
import com.studentbag.backend.courses.sync.service.RitajSyncService;
import com.studentbag.backend.domain.enums.courses.SectionType;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.instructor.service.InstructorAccountIdentityService;
import com.studentbag.backend.instructor.service.InstructorAccountPasswordService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RitajSyncServiceImpl implements RitajSyncService {

    private final InstitutionRepository institutionRepository;
    private final TermRepository termRepository;
    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseRepository courseRepository;
    private final CourseSectionRepository courseSectionRepository;
    private final ClassSessionRepository classSessionRepository;
    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;

    private final RitajFetchService ritajFetchService;
    private final RitajParserService ritajParserService;
    private final RitajTermParserHelper ritajTermParserHelper;

    private final TermSyncMapper termSyncMapper;
    private final FacultySyncMapper facultySyncMapper;
    private final DepartmentSyncMapper departmentSyncMapper;
    private final CourseSyncMapper courseSyncMapper;
    private final CourseSectionSyncMapper courseSectionSyncMapper;
    private final ClassSessionSyncMapper classSessionSyncMapper;

    private final InstructorAccountIdentityService identityService;
    private final InstructorAccountPasswordService passwordService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RitajSyncResult syncTermFromRitaj(Long institutionId, String baseFileName) {
        log.info("🚀 [Ritaj Sync] بدء المزامنة الموحدة والترجمة التلقائية للملف: {}", baseFileName);

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("المؤسسة غير موجودة"));

        // 1. جلب محتوى الصفحات (العربية والإنجليزية) من الـ Storage
        RitajFetchedPagesDto pages = ritajFetchService.fetchArabicAndEnglishPages(baseFileName);

        // 2. تحديد الفصل الدراسي (مثلاً: الفصل الأول 2025/2026)
        Term term = resolveOrCreateTermFromText(pages.getArabicContent(), institution);

        // 3. استدعاء الـ Parser الذي يقوم بدمج الملفين (Ar + En) في قائمة DTOs واحدة
        List<RitajCourseDto> mergedCourses = ritajParserService.parseCourses(
                pages.getArabicContent(),
                pages.getEnglishContent()
        );

        SyncCounters counters = new SyncCounters();

        // 4. معالجة البيانات المدمجة وتخزينها في قاعدة البيانات
        for (RitajCourseDto courseDto : mergedCourses) {
            // فلترة المواد الفارغة (null codes) لضمان نظافة البيانات
            if (courseDto.getCode() == null || courseDto.getCode().isBlank()) continue;

            try {
                processCourseSync(courseDto, institution, term, counters);
            } catch (Exception e) {
                log.error("❌ [Ritaj Sync] فشل مزامنة المساق {}: {}", courseDto.getCode(), e.getMessage());
                // اختياري: يمكنك إكمال الحلقة أو عمل throw حسب رغبتك في الـ Rollback
            }
        }

        log.info("✅ [Ritaj Sync] اكتملت المهمة بنجاح: {}", counters);
        return buildSyncResult(counters);
    }

    private void processCourseSync(RitajCourseDto dto, Institution inst, Term term, SyncCounters counters) {
        // تحديث/إنشاء الكلية والقسم مع دعم الأسماء الإنجليزية
        Faculty faculty = upsertFaculty(dto, inst, counters);
        Department dept = upsertDepartment(dto, faculty, counters);

        // تحديث/إنشاء المساق (تخزين الاسم الإنجليزي ARCH131 -> Introduction to...)
        Course course = upsertCourse(dto, inst, dept, counters);

        for (RitajSectionDto secDto : dto.getSections()) {
            // معالجة المدرس (تحويل TBD وتخزين الاسم العربي والإنجليزي)
            Instructor instructor = upsertInstructor(
                    secDto.getInstructorNameArabic(),
                    secDto.getInstructorNameEnglish(),
                    inst, dept, dto.getCode(), secDto.getSectionNumber(), counters
            );

            // تحديث/إنشاء الشعبة (تحويل "محاضرة" إلى LECTURE Enum)
            CourseSection section = upsertSection(secDto, course, term, instructor, counters);

            // تحديث أوقات المحاضرات (Sessions)
            refreshSectionSessions(section, secDto, counters);
        }

        // ربط المختبرات بالمحاضرات الأساسية
        linkLabsToLectures(course, term, dto.getSections());
    }

    private Faculty upsertFaculty(RitajCourseDto dto, Institution inst, SyncCounters counters) {
        String extId = firstNonBlank(dto.getFacultyExternalId(), dto.getFacultyNameArabic(), "Unknown Faculty");
        return facultyRepository.findByExternalIdAndInstitution(extId, inst)
                .map(f -> {
                    facultySyncMapper.map(dto, f, inst);
                    f.setNameEnglish(dto.getFacultyNameEnglish()); // تأكيد تخزين الإنجليزي
                    return facultyRepository.save(f);
                })
                .orElseGet(() -> {
                    Faculty f = new Faculty();
                    facultySyncMapper.map(dto, f, inst);
                    f.setExternalId(extId);
                    f.setNameEnglish(dto.getFacultyNameEnglish());
                    counters.facultiesCreated++;
                    return facultyRepository.save(f);
                });
    }

    private Department upsertDepartment(RitajCourseDto dto, Faculty faculty, SyncCounters counters) {
        String extId = firstNonBlank(dto.getDepartmentExternalId(), dto.getDepartmentNameArabic(), "Unknown Department");
        return departmentRepository.findByExternalIdAndFaculty(extId, faculty)
                .map(d -> {
                    departmentSyncMapper.map(dto, d, faculty);
                    d.setNameEnglish(dto.getDepartmentNameEnglish()); // تأكيد تخزين الإنجليزي
                    return departmentRepository.save(d);
                })
                .orElseGet(() -> {
                    Department d = new Department();
                    departmentSyncMapper.map(dto, d, faculty);
                    d.setExternalId(extId);
                    d.setNameEnglish(dto.getDepartmentNameEnglish());
                    counters.departmentsCreated++;
                    return departmentRepository.save(d);
                });
    }

    private Course upsertCourse(RitajCourseDto dto, Institution inst, Department dept, SyncCounters counters) {
        return courseRepository.findByCodeAndInstitution(dto.getCode(), inst)
                .map(c -> {
                    courseSyncMapper.map(dto, c, inst, dept);
                    // تحديث الاسم الإنجليزي الحقيقي من الملف المدمج
                    if (dto.getNameEnglish() != null && !dto.getNameEnglish().equalsIgnoreCase(dto.getCode())) {
                        c.setNameEnglish(dto.getNameEnglish());
                    }
                    counters.coursesUpdated++;
                    return courseRepository.save(c);
                })
                .orElseGet(() -> {
                    Course c = new Course();
                    courseSyncMapper.map(dto, c, inst, dept);
                    c.setNameEnglish(dto.getNameEnglish());
                    counters.coursesCreated++;
                    return courseRepository.save(c);
                });
    }

    private Instructor upsertInstructor(String ar, String en, Institution inst, Department dept, String code, String sec, SyncCounters c) {
        String lookupName = firstNonBlank(ar, en);
        if (lookupName == null || lookupName.equalsIgnoreCase("N/A")) return null;

        // تنظيف TBD لتظهر بشكل لائق للمستخدم
        String finalEn = "TBD".equalsIgnoreCase(en) ? "To Be Determined" : en;

        return instructorRepository.findByFullNameArabicAndInstitution(ar, inst)
                .map(ins -> {
                    if (finalEn != null) ins.setFullNameEnglish(finalEn);
                    return instructorRepository.save(ins);
                })
                .orElseGet(() -> {
                    User user = new User();
                    user.setFullName(lookupName);
                    user.setEmail(identityService.generateSystemEmail(lookupName, inst));
                    user.setPasswordHash(passwordEncoder.encode(passwordService.generateInitialPassword(lookupName, code, sec)));
                    user.setRole(UserRole.INSTRUCTOR);
                    user.setActive(true);
                    userRepository.save(user);

                    Instructor ins = new Instructor();
                    ins.setFullNameArabic(ar);
                    ins.setFullNameEnglish(finalEn);
                    ins.setInstitution(inst);
                    ins.setDepartment(dept);
                    ins.setUser(user);
                    c.instructorsCreated++;
                    return instructorRepository.save(ins);
                });
    }

    private CourseSection upsertSection(RitajSectionDto dto, Course c, Term t, Instructor ins, SyncCounters counters) {
        // تحويل "محاضرة" -> SectionType.LECTURE عبر الـ Mapper
        SectionType type = courseSectionSyncMapper.mapSectionType(dto.getSectionType());

        return courseSectionRepository.findByCourseAndTermAndSectionNumberAndSectionType(c, t, dto.getSectionNumber(), type)
                .map(s -> {
                    courseSectionSyncMapper.map(dto, s, c, t, ins);
                    s.setSectionType(type); // تأكيد النوع المترجم
                    counters.sectionsUpdated++;
                    return courseSectionRepository.save(s);
                })
                .orElseGet(() -> {
                    CourseSection s = new CourseSection();
                    courseSectionSyncMapper.map(dto, s, c, t, ins);
                    s.setSectionType(type);
                    counters.sectionsCreated++;
                    return courseSectionRepository.save(s);
                });
    }

    private void refreshSectionSessions(CourseSection sec, RitajSectionDto dto, SyncCounters c) {
        classSessionRepository.deleteByCourseSection(sec);
        if (dto.getSessions() != null) {
            for (RitajClassSessionDto sDto : dto.getSessions()) {
                classSessionRepository.save(classSessionSyncMapper.toEntity(sDto, sec));
                c.sessionsCreated++;
            }
        }
    }

    private void linkLabsToLectures(Course course, Term term, List<RitajSectionDto> sectionDtos) {
        // 1. جلب كل الشعب اللي تخزنت في قاعدة البيانات لهاد المساق في هاد الفصل
        List<CourseSection> savedSections = courseSectionRepository.findByCourseAndTerm(course, term);

        for (CourseSection section : savedSections) {
            // 2. إذا كانت الشعبة الحالية "مختبر" (LAB)
            if (section.getSectionType() == SectionType.LAB) {

                // 3. ابحث عن "محاضرة" (LECTURE) بيعطيها نفس الدكتور (Instructor)
                Optional<CourseSection> parentLecture = savedSections.stream()
                        .filter(s -> s.getSectionType() == SectionType.LECTURE) // لازم تكون محاضرة
                        .filter(s -> s.getInstructor() != null && section.getInstructor() != null)
                        // المقارنة عن طريق الـ ID تبع المدرس (الأضمن)
                        .filter(s -> s.getInstructor().getId().equals(section.getInstructor().getId()))
                        .findFirst();

                if (parentLecture.isPresent()) {
                    section.setParentLectureSection(parentLecture.get());
                    courseSectionRepository.save(section);
                    log.info("🔗 [Link Success] تم ربط مختبر (شعبة {}) بالمحاضرة الأساسية للدكتور: {}",
                            section.getSectionNumber(), section.getInstructor().getFullNameArabic());
                } else {
                    log.warn("⚠️ [Link Fail] لم نجد محاضرة أساسية لنفس الدكتور لمختبر شعبة {}", section.getSectionNumber());
                }
            }
        }
    }

    private Term resolveOrCreateTermFromText(String text, Institution institution) {
        String termName = ritajTermParserHelper.extractTermFromText(text);
        if (termName == null) throw new IllegalStateException("لم نتمكن من تحديد الفصل الدراسي من الملف");

        String code = termName.trim().replace(" ", "_").toUpperCase().replaceAll("[^A-Z0-9_]", "");
        Term term = termRepository.findByTermCodeAndInstitution(code, institution)
                .orElseGet(() -> termRepository.save(termSyncMapper.mapToEntity(termName, institution)));

        // تعيين الفصل كفصل حالي (Is Current)
        termRepository.findAllByInstitution(institution).forEach(t -> {
            t.setIsCurrent(t.getId().equals(term.getId()));
            termRepository.save(t);
        });
        return term;
    }

    private String firstNonBlank(String... vs) {
        for (String v : vs) if (v != null && !v.isBlank()) return v.trim();
        return null;
    }

    private RitajSyncResult buildSyncResult(SyncCounters c) {
        return RitajSyncResult.builder()
                .facultiesCreated(c.facultiesCreated).departmentsCreated(c.departmentsCreated)
                .coursesCreated(c.coursesCreated).coursesUpdated(c.coursesUpdated)
                .instructorsCreated(c.instructorsCreated).sectionsCreated(c.sectionsCreated)
                .sectionsUpdated(c.sectionsUpdated).sessionsCreated(c.sessionsCreated)
                .build();
    }

    private static class SyncCounters {
        int facultiesCreated, departmentsCreated, coursesCreated, coursesUpdated, instructorsCreated, sectionsCreated, sectionsUpdated, sessionsCreated;
        @Override
        public String toString() {
            return String.format("النتائج: [كليات: %d, دوائر: %d, مساقات جديدة: %d, شعب: %d]",
                    facultiesCreated, departmentsCreated, coursesCreated, sectionsCreated);
        }
    }
}