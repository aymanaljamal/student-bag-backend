package com.studentbag.backend.courses.service.impl;

import com.studentbag.backend.courses.dto.CourseSectionDetailedDTO;
import com.studentbag.backend.courses.dto.request.CourseRequestDTO;
import com.studentbag.backend.courses.dto.response.ClassSessionResponseDTO;
import com.studentbag.backend.courses.dto.response.CourseDetailedResponseDTO;
import com.studentbag.backend.courses.dto.response.CourseResponseDTO;
import com.studentbag.backend.courses.entity.ClassSession;
import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.entity.Department;
import com.studentbag.backend.courses.mapper.CourseMapper;
import com.studentbag.backend.courses.repository.CourseRepository;
import com.studentbag.backend.courses.repository.DepartmentRepository;
import com.studentbag.backend.courses.service.CourseService;
import com.studentbag.backend.courses.specification.CourseSpecification;
import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.institution.repository.InstitutionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final InstitutionRepository institutionRepository;
    private final DepartmentRepository departmentRepository;
    private final CourseMapper courseMapper;

    @Override
    public CourseResponseDTO create(CourseRequestDTO request) {
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institution not found"));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        }

        Course course = new Course();
        courseMapper.toEntity(request, course, institution, department);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO update(Long id, CourseRequestDTO request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new EntityNotFoundException("Institution not found"));

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Department not found"));
        }

        courseMapper.toEntity(request, course, institution, department);

        return courseMapper.toResponse(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO getById(Long id, boolean includeSections) {
        Course course = includeSections
                ? courseRepository.findDetailedById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"))
                : courseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));

        return courseMapper.toResponse(course, includeSections);
    }

    @Override
    public List<CourseResponseDTO> getAll(boolean includeSections) {
        return courseRepository.findAll()
                .stream()
                .map(course -> courseMapper.toResponse(course, includeSections))
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    @Override
    public Page<CourseResponseDTO> search(
            String keyword,
            Long institutionId,
            String level,
            Boolean isActive,
            boolean includeSections,
            Pageable pageable
    ) {
        return courseRepository.findAll(
                        CourseSpecification.search(keyword, institutionId, level, isActive),
                        pageable
                )
                .map(course -> courseMapper.toResponse(course, includeSections));
    }

    @Override
    public List<CourseDetailedResponseDTO> getAllCoursesDetailed() {
        return courseRepository.findAll()
                .stream()
                .map(this::mapCourseDetailed)
                .toList();
    }

    private CourseDetailedResponseDTO mapCourseDetailed(Course course) {
        List<CourseSectionDetailedDTO> detailedSections = buildDisplayedSections(course);

        return CourseDetailedResponseDTO.builder()
                .id(course.getId())
                .externalId(course.getExternalId())
                .code(course.getCode())
                .nameArabic(course.getNameArabic())
                .nameEnglish(course.getNameEnglish())
                .description(course.getDescription())
                .creditHours(course.getCreditHours())
                .level(course.getLevel())
                .programNameArabic(course.getProgramNameArabic())
                .programNameEnglish(course.getProgramNameEnglish())
                .institutionId(course.getInstitution() != null ? course.getInstitution().getId() : null)
                .departmentId(course.getDepartment() != null ? course.getDepartment().getId() : null)
                .isActive(course.getIsActive())
                .sections(detailedSections)
                .build();
    }

    /**
     * Display only main/lecture sections.
     * Any child section (lab) that has parentLectureSection is merged into
     * the parent as additional class sessions.
     */
    private List<CourseSectionDetailedDTO> buildDisplayedSections(Course course) {
        if (course.getSections() == null || course.getSections().isEmpty()) {
            return List.of();
        }

        List<CourseSection> allSections = course.getSections();

        Map<Long, List<CourseSection>> childSectionsByParentId = allSections.stream()
                .filter(Objects::nonNull)
                .filter(section -> section.getParentLectureSection() != null)
                .collect(Collectors.groupingBy(section -> section.getParentLectureSection().getId()));

        return allSections.stream()
                .filter(Objects::nonNull)
                .filter(section -> section.getParentLectureSection() == null) // only main sections
                .map(section -> mapSectionDetailed(section, childSectionsByParentId.get(section.getId())))
                .toList();
    }

    private CourseSectionDetailedDTO mapSectionDetailed(
            CourseSection section,
            List<CourseSection> childSections
    ) {
        List<ClassSessionResponseDTO> mergedSessions = new ArrayList<>();

        // Main section sessions
        if (section.getClassSessions() != null) {
            mergedSessions.addAll(
                    section.getClassSessions().stream()
                            .filter(Objects::nonNull)
                            .map(session -> mapSession(session, false, null))
                            .toList()
            );
        }

        // Child/Lab section sessions -> expose them as sessions under parent
        if (childSections != null && !childSections.isEmpty()) {
            for (CourseSection childSection : childSections) {
                if (childSection.getClassSessions() == null) {
                    continue;
                }

                mergedSessions.addAll(
                        childSection.getClassSessions().stream()
                                .filter(Objects::nonNull)
                                .map(session -> mapSession(session, true, childSection))
                                .toList()
                );
            }
        }

        return CourseSectionDetailedDTO.builder()
                .id(section.getId())
                .externalId(section.getExternalId())
                .courseId(section.getCourse() != null ? section.getCourse().getId() : null)
                .termId(section.getTerm() != null ? section.getTerm().getId() : null)
                .sectionNumber(section.getSectionNumber())
                .sectionType(section.getSectionType() != null ? section.getSectionType().name() : null)
                .instructorId(section.getInstructor() != null ? section.getInstructor().getId() : null)
                .instructorName(
                        section.getInstructor() != null && section.getInstructor().getUser() != null
                                ? section.getInstructor().getUser().getFullName()
                                : null
                )
                .instructorNameArabic(
                        section.getInstructor() != null ? section.getInstructor().getFullNameArabic() : null
                )
                .instructorNameEnglish(
                        section.getInstructor() != null ? section.getInstructor().getFullNameEnglish() : null
                )
                .parentLectureSectionId(
                        section.getParentLectureSection() != null
                                ? section.getParentLectureSection().getId()
                                : null
                )
                .capacity(section.getCapacity())
                .enrolled(section.getEnrolled())
                .availableSeats(section.getAvailableSeats())
                .isOfficial(section.getIsOfficial())
                .classSessions(mergedSessions)
                .build();
    }

    /**
     * If the session comes from a child/lab section, mark it as lab session.
     * You can later add extra DTO fields like:
     * - sourceSectionId
     * - sourceSectionNumber
     * - sourceSectionType
     * - isLab
     */
    private ClassSessionResponseDTO mapSession(
            ClassSession session,
            boolean fromChildSection,
            CourseSection sourceSection
    ) {
        return ClassSessionResponseDTO.builder()
                .id(session.getId())
                .courseSectionId(sourceSection != null
                        ? sourceSection.getId()
                        : (session.getCourseSection() != null ? session.getCourseSection().getId() : null))
                .dayOfWeek(session.getDayOfWeek())
                .startTime(session.getStartTime())
                .endTime(session.getEndTime())
                .room(session.getRoom())
                .building(session.getBuilding())
                .campus(session.getCampus())
                .isOnline(session.getIsOnline())
                .build();
    }


}