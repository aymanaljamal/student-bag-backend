package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.entity.Faculty;
import com.studentbag.backend.courses.entity.Term;
import com.studentbag.backend.courses.repository.ClassSessionRepository;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.courses.repository.DepartmentRepository;
import com.studentbag.backend.courses.repository.FacultyRepository;
import com.studentbag.backend.courses.repository.TermRepository;
import com.studentbag.backend.courses.sync.dto.RitajClassSessionDto;
import com.studentbag.backend.courses.sync.dto.RitajCourseDto;
import com.studentbag.backend.courses.sync.dto.RitajFetchedPagesDto;
import com.studentbag.backend.courses.sync.dto.RitajSectionDto;
import com.studentbag.backend.courses.sync.dto.RitajSyncResult;
import com.studentbag.backend.courses.sync.mapper.ClassSessionSyncMapper;
import com.studentbag.backend.courses.sync.mapper.CourseSectionSyncMapper;
import com.studentbag.backend.courses.sync.mapper.CourseSyncMapper;
import com.studentbag.backend.courses.sync.mapper.DepartmentSyncMapper;
import com.studentbag.backend.courses.sync.mapper.FacultySyncMapper;
import com.studentbag.backend.courses.sync.service.RitajFetchService;
import com.studentbag.backend.courses.sync.service.RitajParserService;
import com.studentbag.backend.courses.sync.service.RitajSyncService;
import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.domain.enums.courses.Season;
import com.studentbag.backend.domain.enums.courses.SectionType;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import com.studentbag.backend.instructor.entity.Instructor;
import com.studentbag.backend.instructor.repository.InstructorRepository;
import com.studentbag.backend.instructor.service.InstructorAccountIdentityService;
import com.studentbag.backend.instructor.service.InstructorAccountPasswordService;
import com.studentbag.backend.users.entity.User;
import com.studentbag.backend.users.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RitajSyncServiceImpl implements RitajSyncService {

    private static final String DEFAULT_TERM_CODE = "2025-2026-SPRING";
    private static final String DEFAULT_TERM_NAME = "Second Semester 2025/2026";
    private static final String DEFAULT_ACADEMIC_YEAR = "2025/2026";

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

    private final FacultySyncMapper facultySyncMapper;
    private final DepartmentSyncMapper departmentSyncMapper;
    private final CourseSyncMapper courseSyncMapper;
    private final CourseSectionSyncMapper courseSectionSyncMapper;
    private final ClassSessionSyncMapper classSessionSyncMapper;

    private final InstructorAccountIdentityService identityService;
    private final InstructorAccountPasswordService passwordService;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public RitajSyncResult syncTermFromRitaj(Long institutionId, String baseFileName) {
        log.info("🚀 [Ritaj Sync] بدء مزامنة ملف JSON normalized: {}", baseFileName);

        Institution institution = institutionRepository.findById(institutionId)
                .orElseThrow(() -> new IllegalArgumentException("المؤسسة غير موجودة: " + institutionId));

        RitajFetchedPagesDto pages = ritajFetchService.fetchJsonFile(baseFileName);

        List<RitajCourseDto> courses = ritajParserService.parseJson(pages.getJsonContent());

        if (courses == null || courses.isEmpty()) {
            throw new IllegalArgumentException("ملف JSON لا يحتوي على مساقات صالحة");
        }

        prepareStorageCodes(courses);

        Term term = getOrCreateCurrentRitajTerm(institution);

        SyncCounters counters = new SyncCounters();

        for (RitajCourseDto courseDto : courses) {
            if (storageCourseCode(courseDto) == null) {
                continue;
            }

            try {
                processCourseSync(courseDto, institution, term, counters);
            } catch (Exception e) {
                log.error("❌ [Ritaj Sync] فشل مزامنة المساق originalCode={}, storageCode={}: {}",
                        courseDto.getCode(),
                        courseDto.getStorageCode(),
                        e.getMessage(),
                        e
                );
                throw e;
            }
        }

        log.info("✅ [Ritaj Sync] اكتملت المهمة بنجاح: {}", counters);

        return buildSyncResult(counters);
    }

    private void prepareStorageCodes(List<RitajCourseDto> courses) {
        Map<String, Integer> codeCounts = new LinkedHashMap<>();

        for (RitajCourseDto dto : courses) {
            String code = normalizeKey(dto.getCode());
            if (code == null) continue;
            codeCounts.put(code, codeCounts.getOrDefault(code, 0) + 1);
        }

        for (RitajCourseDto dto : courses) {
            String originalCode = firstNonBlank(dto.getCode());
            String normalized = normalizeKey(originalCode);
            boolean duplicatedCode = normalized != null && codeCounts.getOrDefault(normalized, 0) > 1;

            if (duplicatedCode) {
                dto.setStorageCode(firstNonBlank(dto.getCourseInternalId(), originalCode));
            } else {
                dto.setStorageCode(originalCode);
            }
        }
    }

    private Term getOrCreateCurrentRitajTerm(Institution institution) {
        Term term = termRepository.findByTermCodeAndInstitution(DEFAULT_TERM_CODE, institution)
                .orElseGet(() -> {
                    Term newTerm = new Term();
                    newTerm.setInstitution(institution);
                    newTerm.setExternalId(DEFAULT_TERM_CODE);
                    newTerm.setTermCode(DEFAULT_TERM_CODE);
                    newTerm.setName(DEFAULT_TERM_NAME);
                    newTerm.setAcademicYear(DEFAULT_ACADEMIC_YEAR);
                    newTerm.setSeason(Season.SPRING);
                    newTerm.setStartDate(LocalDate.of(2026, 1, 25));
                    newTerm.setEndDate(LocalDate.of(2026, 5, 30));
                    newTerm.setIsCurrent(true);

                    return termRepository.save(newTerm);
                });

        termRepository.findAllByInstitution(institution).forEach(existingTerm -> {
            existingTerm.setIsCurrent(existingTerm.getId().equals(term.getId()));
            termRepository.save(existingTerm);
        });

        return term;
    }

    private void processCourseSync(
            RitajCourseDto dto,
            Institution institution,
            Term term,
            SyncCounters counters
    ) {
        Faculty faculty = upsertFaculty(dto, institution, counters);
        Department department = upsertDepartment(dto, faculty, counters);
        Course course = upsertCourse(dto, institution, department, counters);

        if (dto.getSections() == null || dto.getSections().isEmpty()) {
            return;
        }

        for (RitajSectionDto sectionDto : dto.getSections()) {
            Instructor instructor = upsertInstructor(
                    sectionDto.getInstructorNameArabic(),
                    sectionDto.getInstructorNameEnglish(),
                    institution,
                    department,
                    dto.getCode(),
                    sectionDto.getSectionNumber(),
                    counters
            );

            CourseSection section = upsertSection(sectionDto, course, term, instructor, counters);

            refreshSectionSessions(section, sectionDto, counters);
        }

        linkChildSectionsToLectures(course, term, dto, counters);
    }

    private Faculty upsertFaculty(RitajCourseDto dto, Institution institution, SyncCounters counters) {
        String externalId = firstNonBlank(
                dto.getFacultyExternalId(),
                dto.getFacultyNameArabic(),
                dto.getFacultyNameEnglish(),
                "Unknown Faculty"
        );

        return facultyRepository.findByExternalIdAndInstitution(externalId, institution)
                .map(faculty -> {
                    facultySyncMapper.map(dto, faculty, institution);
                    faculty.setExternalId(externalId);
                    faculty.setNameEnglish(firstNonBlank(dto.getFacultyNameEnglish(), faculty.getNameArabic()));
                    faculty.setIsActive(true);
                    return facultyRepository.save(faculty);
                })
                .orElseGet(() -> {
                    Faculty faculty = new Faculty();
                    facultySyncMapper.map(dto, faculty, institution);
                    faculty.setExternalId(externalId);
                    faculty.setNameEnglish(firstNonBlank(dto.getFacultyNameEnglish(), faculty.getNameArabic()));
                    faculty.setIsActive(true);
                    counters.facultiesCreated++;
                    return facultyRepository.save(faculty);
                });
    }

    private Department upsertDepartment(RitajCourseDto dto, Faculty faculty, SyncCounters counters) {
        String nameArabic = firstNonBlank(dto.getDepartmentNameArabic(), "Unknown Department");

        String externalId = firstNonBlank(
                dto.getDepartmentExternalId(),
                dto.getDepartmentNameArabic(),
                dto.getDepartmentNameEnglish(),
                "Unknown Department"
        );

        Optional<Department> existingDepartment = findDepartmentSafely(externalId, nameArabic, faculty);

        return existingDepartment
                .map(department -> {
                    departmentSyncMapper.map(dto, department, faculty);
                    department.setExternalId(firstNonBlank(department.getExternalId(), externalId));
                    department.setNameArabic(nameArabic);
                    department.setNameEnglish(firstNonBlank(dto.getDepartmentNameEnglish(), nameArabic));
                    department.setProgramNameArabic(dto.getProgramNameArabic());
                    department.setProgramNameEnglish(dto.getProgramNameEnglish());
                    department.setFaculty(faculty);
                    department.setIsActive(true);
                    return departmentRepository.save(department);
                })
                .orElseGet(() -> {
                    Department department = new Department();
                    departmentSyncMapper.map(dto, department, faculty);
                    department.setExternalId(externalId);
                    department.setNameArabic(nameArabic);
                    department.setNameEnglish(firstNonBlank(dto.getDepartmentNameEnglish(), nameArabic));
                    department.setProgramNameArabic(dto.getProgramNameArabic());
                    department.setProgramNameEnglish(dto.getProgramNameEnglish());
                    department.setFaculty(faculty);
                    department.setIsActive(true);

                    counters.departmentsCreated++;
                    return departmentRepository.save(department);
                });
    }

    private Optional<Department> findDepartmentSafely(
            String externalId,
            String nameArabic,
            Faculty faculty
    ) {
        if (externalId != null && !externalId.isBlank()) {
            Optional<Department> byExternalIdAndFaculty =
                    departmentRepository.findByExternalIdAndFaculty(externalId, faculty);

            if (byExternalIdAndFaculty.isPresent()) {
                return byExternalIdAndFaculty;
            }
        }

        if (nameArabic == null || nameArabic.isBlank()) {
            return Optional.empty();
        }

        List<Department> byNameArabicAndFaculty = entityManager.createQuery("""
                        SELECT d
                        FROM Department d
                        WHERE d.nameArabic = :nameArabic
                          AND d.faculty = :faculty
                        """, Department.class)
                .setParameter("nameArabic", nameArabic)
                .setParameter("faculty", faculty)
                .setMaxResults(1)
                .getResultList();

        if (!byNameArabicAndFaculty.isEmpty()) {
            return Optional.of(byNameArabicAndFaculty.get(0));
        }

        return Optional.empty();
    }

    private Course upsertCourse(
            RitajCourseDto dto,
            Institution institution,
            Department department,
            SyncCounters counters
    ) {
        String storageCode = storageCourseCode(dto);

        return courseRepository.findByCodeAndInstitution(storageCode, institution)
                .map(course -> {
                    courseSyncMapper.map(dto, course, institution, department);
                    counters.coursesUpdated++;
                    return courseRepository.save(course);
                })
                .orElseGet(() -> {
                    Course course = new Course();
                    courseSyncMapper.map(dto, course, institution, department);
                    counters.coursesCreated++;
                    return courseRepository.save(course);
                });
    }

    private Instructor upsertInstructor(
            String nameArabic,
            String nameEnglish,
            Institution institution,
            Department department,
            String courseCode,
            String sectionNumber,
            SyncCounters counters
    ) {
        String lookupName = firstNonBlank(nameArabic, nameEnglish);

        if (lookupName == null
                || lookupName.equalsIgnoreCase("N/A")
                || lookupName.equalsIgnoreCase("NA")
                || lookupName.equalsIgnoreCase("TBD")
                || lookupName.equalsIgnoreCase("To Be Determined")) {
            return null;
        }

        String finalArabic = firstNonBlank(nameArabic, lookupName);
        String finalEnglish = "TBD".equalsIgnoreCase(nameEnglish)
                ? "To Be Determined"
                : firstNonBlank(nameEnglish, lookupName);

        return instructorRepository.findByFullNameArabicAndInstitution(finalArabic, institution)
                .map(instructor -> {
                    instructor.setFullNameEnglish(finalEnglish);
                    instructor.setDepartment(department);
                    return instructorRepository.save(instructor);
                })
                .orElseGet(() -> {
                    String rawPassword = passwordService.generateInitialPassword(
                            lookupName,
                            courseCode,
                            sectionNumber
                    );

                    User user = new User();
                    user.setFullName(lookupName);
                    user.setEmail(identityService.generateSystemEmail(lookupName, institution));
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    user.setRole(UserRole.INSTRUCTOR);
                    user.setActive(true);
                    user.setEmailVerified(false);
                    user.setPhoneVerified(false);

                    userRepository.save(user);

                    log.warn("👨‍🏫 [Instructor Account] created email={}, password={}",
                            user.getEmail(),
                            rawPassword
                    );

                    Instructor instructor = new Instructor();
                    instructor.setExternalId(null);
                    instructor.setFullNameArabic(finalArabic);
                    instructor.setFullNameEnglish(finalEnglish);
                    instructor.setInstitution(institution);
                    instructor.setDepartment(department);
                    instructor.setUser(user);
                    instructor.setAccountConfirmed(false);

                    counters.instructorsCreated++;

                    return instructorRepository.save(instructor);
                });
    }

    private CourseSection upsertSection(
            RitajSectionDto dto,
            Course course,
            Term term,
            Instructor instructor,
            SyncCounters counters
    ) {
        SectionType sectionType = courseSectionSyncMapper.mapSectionType(dto.getSectionType());
        String sectionNumber = firstNonBlank(dto.getSectionNumber(), "1");

        return courseSectionRepository
                .findByCourseAndTermAndSectionNumberAndSectionType(
                        course,
                        term,
                        sectionNumber,
                        sectionType
                )
                .map(section -> {
                    courseSectionSyncMapper.map(dto, section, course, term, instructor);
                    section.setSectionType(sectionType);
                    counters.sectionsUpdated++;
                    return courseSectionRepository.save(section);
                })
                .orElseGet(() -> {
                    CourseSection section = new CourseSection();
                    courseSectionSyncMapper.map(dto, section, course, term, instructor);
                    section.setSectionType(sectionType);
                    counters.sectionsCreated++;
                    return courseSectionRepository.save(section);
                });
    }

    private void refreshSectionSessions(
            CourseSection section,
            RitajSectionDto dto,
            SyncCounters counters
    ) {
        classSessionRepository.deleteByCourseSection(section);

        if (dto.getSessions() == null || dto.getSessions().isEmpty()) {
            return;
        }

        for (RitajClassSessionDto sessionDto : dto.getSessions()) {
            ClassSession session = classSessionSyncMapper.toEntity(sessionDto, section);
            classSessionRepository.save(session);
            counters.sessionsCreated++;
        }
    }

    private void linkChildSectionsToLectures(
            Course course,
            Term term,
            RitajCourseDto courseDto,
            SyncCounters counters
    ) {
        List<CourseSection> savedSections = courseSectionRepository.findByCourseAndTerm(course, term);

        for (RitajSectionDto childDto : courseDto.getSections()) {
            SectionType childType = courseSectionSyncMapper.mapSectionType(childDto.getSectionType());

            boolean isChildSection =
                    childType == SectionType.LAB
                            || childType == SectionType.DISCUSSION
                            || childType == SectionType.PRACTICAL;

            if (!isChildSection) {
                continue;
            }

            if (Boolean.TRUE.equals(courseDto.getIsLabCourse()) && childType == SectionType.LAB) {
                continue;
            }

            String childNumber = firstNonBlank(childDto.getSectionNumber(), "1");

            CourseSection child = savedSections.stream()
                    .filter(section -> section.getSectionType() == childType)
                    .filter(section -> childNumber.equals(section.getSectionNumber()))
                    .findFirst()
                    .orElse(null);

            if (child == null) {
                continue;
            }

            String parentNumber = firstNonBlank(childDto.getParentLectureSectionNumber());

            CourseSection parent = null;

            if (parentNumber != null) {
                parent = savedSections.stream()
                        .filter(section -> section.getSectionType() == SectionType.LECTURE)
                        .filter(section -> parentNumber.equals(section.getSectionNumber()))
                        .findFirst()
                        .orElse(null);
            }

            if (parent == null) {
                parent = savedSections.stream()
                        .filter(section -> section.getSectionType() == SectionType.LECTURE)
                        .findFirst()
                        .orElse(null);
            }

            if (parent != null) {
                child.setParentLectureSection(parent);
                courseSectionRepository.save(child);
                counters.labsLinked++;

                log.info(
                        "🔗 [Link Success] course={}, childType={}, childSection={}, parentLecture={}",
                        course.getCode(),
                        childType,
                        child.getSectionNumber(),
                        parent.getSectionNumber()
                );
            } else {
                log.warn(
                        "⚠️ [Link Fail] course={}, childType={}, childSection={} has no lecture parent",
                        course.getCode(),
                        childType,
                        child.getSectionNumber()
                );
            }
        }
    }

    private String storageCourseCode(RitajCourseDto dto) {
        return firstNonBlank(dto.getStorageCode(), dto.getCourseInternalId(), dto.getCode());
    }

    private String normalizeKey(String value) {
        String normalized = firstNonBlank(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return null;
    }

    private RitajSyncResult buildSyncResult(SyncCounters counters) {
        return RitajSyncResult.builder()
                .facultiesCreated(counters.facultiesCreated)
                .departmentsCreated(counters.departmentsCreated)
                .coursesCreated(counters.coursesCreated)
                .coursesUpdated(counters.coursesUpdated)
                .instructorsCreated(counters.instructorsCreated)
                .sectionsCreated(counters.sectionsCreated)
                .sectionsUpdated(counters.sectionsUpdated)
                .sessionsCreated(counters.sessionsCreated)
                .labsLinked(counters.labsLinked)
                .deletedSections(counters.deletedSections)
                .deletedSessions(counters.deletedSessions)
                .build();
    }

    private static class SyncCounters {
        int facultiesCreated;
        int departmentsCreated;
        int coursesCreated;
        int coursesUpdated;
        int instructorsCreated;
        int sectionsCreated;
        int sectionsUpdated;
        int sessionsCreated;
        int labsLinked;
        int deletedSections;
        int deletedSessions;

        @Override
        public String toString() {
            return String.format(
                    "SyncCounters{facultiesCreated=%d, departmentsCreated=%d, coursesCreated=%d, coursesUpdated=%d, instructorsCreated=%d, sectionsCreated=%d, sectionsUpdated=%d, sessionsCreated=%d, labsLinked=%d}",
                    facultiesCreated,
                    departmentsCreated,
                    coursesCreated,
                    coursesUpdated,
                    instructorsCreated,
                    sectionsCreated,
                    sectionsUpdated,
                    sessionsCreated,
                    labsLinked
            );
        }
    }
}
