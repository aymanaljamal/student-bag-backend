package com.studentbag.backend.schedule.service.impl;

import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import com.studentbag.backend.domain.enums.courses.SectionType;
import com.studentbag.backend.schedule.dto.response.ScheduleOptionResponseDTO;
import com.studentbag.backend.schedule.service.ConflictService;
import com.studentbag.backend.schedule.service.PreferenceRankingService;
import com.studentbag.backend.schedule.service.TimetableGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableGeneratorServiceImpl implements TimetableGeneratorService {

    private final CourseSectionRepository sectionRepository;
    private final ConflictService conflictService;
    private final PreferenceRankingService rankingService;

    @Override
    public List<ScheduleOptionResponseDTO> generateValidOptions(
            Long termId,
            List<Long> courseIds,
            List<Long> lockedSectionIds,
            Map<Long, Integer> courseRatings,
            Long studentId
    ) {
        log.info(
                "Generating timetable options for student={}, term={}, courseIds={}, lockedSectionIds={}, courseRatings={}",
                studentId, termId, courseIds, lockedSectionIds, courseRatings
        );

        if (termId == null || CollectionUtils.isEmpty(courseIds)) {
            log.warn("Cannot generate timetable options because termId or courseIds are missing.");
            return Collections.emptyList();
        }

        List<Long> normalizedLockedIds = lockedSectionIds != null
                ? lockedSectionIds
                : Collections.emptyList();

        Map<Long, Integer> normalizedRatings = courseRatings != null
                ? courseRatings
                : Collections.emptyMap();

        List<CourseSection> allSections =
                sectionRepository.findByCourseIdInAndTermId(courseIds, termId);

        log.info("Loaded {} raw sections from repository for term={}", allSections.size(), termId);

        Map<Long, List<SectionBundle>> bundlesByCourse = buildBundlesByCourse(allSections);

        if (bundlesByCourse.size() < courseIds.size()) {
            List<Long> foundCourseIds = new ArrayList<>(bundlesByCourse.keySet());
            List<Long> missingCourseIds = courseIds.stream()
                    .filter(id -> !foundCourseIds.contains(id))
                    .toList();

            log.warn(
                    "Some requested courses have no available bundles in term={}. Missing courseIds={}",
                    termId,
                    missingCourseIds
            );
        }

        for (Map.Entry<Long, List<SectionBundle>> entry : bundlesByCourse.entrySet()) {
            log.info(
                    "Course {} has {} generator bundles: {}",
                    entry.getKey(),
                    entry.getValue().size(),
                    entry.getValue().stream()
                            .map(this::describeBundle)
                            .toList()
            );
        }

        List<Long> orderedCourseIds = new ArrayList<>(courseIds);
        orderedCourseIds.sort(Comparator.comparingInt(courseId -> {
            List<SectionBundle> bundles =
                    bundlesByCourse.getOrDefault(courseId, Collections.emptyList());

            return bundles.size();
        }));

        List<List<CourseSection>> validCombinations = new ArrayList<>();

        generateCombinations(
                orderedCourseIds,
                0,
                new ArrayList<>(),
                bundlesByCourse,
                normalizedLockedIds,
                validCombinations
        );

        log.info("Found {} valid non-conflicting combinations", validCombinations.size());

        return rankingService.rankAndScore(
                validCombinations,
                studentId,
                normalizedRatings
        );
    }

    private Map<Long, List<SectionBundle>> buildBundlesByCourse(
            List<CourseSection> allSections
    ) {
        if (CollectionUtils.isEmpty(allSections)) {
            return Collections.emptyMap();
        }

        Map<Long, List<CourseSection>> sectionsByCourse = allSections.stream()
                .filter(section -> section != null)
                .filter(section -> section.getCourse() != null)
                .filter(section -> section.getCourse().getId() != null)
                .collect(Collectors.groupingBy(
                        section -> section.getCourse().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        Map<Long, List<SectionBundle>> result = new LinkedHashMap<>();

        for (Map.Entry<Long, List<CourseSection>> entry : sectionsByCourse.entrySet()) {
            Long courseId = entry.getKey();
            List<CourseSection> courseSections = entry.getValue();

            Map<Long, List<CourseSection>> childrenByParentId = courseSections.stream()
                    .filter(section -> parentLectureSectionId(section) != null)
                    .collect(Collectors.groupingBy(
                            this::parentLectureSectionId,
                            LinkedHashMap::new,
                            Collectors.toList()
                    ));

            List<CourseSection> parentlessNonLabMainSections = courseSections.stream()
                    .filter(this::isParentlessSection)
                    .filter(section -> section.getSectionType() != SectionType.DISCUSSION)
                    .filter(section -> section.getSectionType() != SectionType.LAB)
                    .sorted(this::compareSections)
                    .toList();

            /*
             * If the course has normal parentless lecture/seminar/practical sections,
             * use them as main sections and attach child LAB/DISCUSSION sections.
             *
             * If the course itself is a standalone LAB course and has no lecture parent,
             * allow parentless LAB sections as main options.
             */
            List<CourseSection> mainSections = !parentlessNonLabMainSections.isEmpty()
                    ? parentlessNonLabMainSections
                    : courseSections.stream()
                    .filter(this::isParentlessSection)
                    .filter(section -> section.getSectionType() != SectionType.DISCUSSION)
                    .sorted(this::compareSections)
                    .toList();

            List<SectionBundle> bundles = new ArrayList<>();

            for (CourseSection mainSection : mainSections) {
                List<CourseSection> children = childrenByParentId.getOrDefault(
                        mainSection.getId(),
                        Collections.emptyList()
                );

                bundles.addAll(buildBundlesForMainSection(courseId, mainSection, children));
            }

            if (!bundles.isEmpty()) {
                result.put(courseId, bundles);
            }
        }

        return result;
    }

    private List<SectionBundle> buildBundlesForMainSection(
            Long courseId,
            CourseSection mainSection,
            List<CourseSection> children
    ) {
        if (CollectionUtils.isEmpty(children)) {
            return List.of(new SectionBundle(courseId, List.of(mainSection)));
        }

        Map<SectionType, List<CourseSection>> childrenByType = children.stream()
                .filter(child -> child != null)
                .filter(child -> child.getSectionType() != null)
                .sorted(this::compareSections)
                .collect(Collectors.groupingBy(
                        CourseSection::getSectionType,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        if (childrenByType.isEmpty()) {
            return List.of(new SectionBundle(courseId, List.of(mainSection)));
        }

        List<List<CourseSection>> childChoiceGroups = new ArrayList<>(childrenByType.values());
        List<List<CourseSection>> childChoiceCombinations = new ArrayList<>();

        buildChildChoiceCombinations(
                childChoiceGroups,
                0,
                new ArrayList<>(),
                childChoiceCombinations
        );

        List<SectionBundle> bundles = new ArrayList<>();

        for (List<CourseSection> childChoice : childChoiceCombinations) {
            List<CourseSection> bundleSections = new ArrayList<>();
            bundleSections.add(mainSection);
            bundleSections.addAll(childChoice);

            bundles.add(new SectionBundle(courseId, bundleSections));
        }

        return bundles;
    }

    private void buildChildChoiceCombinations(
            List<List<CourseSection>> groups,
            int index,
            List<CourseSection> current,
            List<List<CourseSection>> results
    ) {
        if (index == groups.size()) {
            results.add(new ArrayList<>(current));
            return;
        }

        List<CourseSection> group = groups.get(index);

        if (CollectionUtils.isEmpty(group)) {
            buildChildChoiceCombinations(groups, index + 1, current, results);
            return;
        }

        for (CourseSection section : group) {
            current.add(section);

            buildChildChoiceCombinations(
                    groups,
                    index + 1,
                    current,
                    results
            );

            current.remove(current.size() - 1);
        }
    }

    private void generateCombinations(
            List<Long> courseIds,
            int index,
            List<CourseSection> currentPath,
            Map<Long, List<SectionBundle>> bundlesByCourse,
            List<Long> lockedIds,
            List<List<CourseSection>> results
    ) {
        if (index == courseIds.size()) {
            results.add(new ArrayList<>(currentPath));
            return;
        }

        Long currentCourseId = courseIds.get(index);
        List<SectionBundle> bundles =
                bundlesByCourse.getOrDefault(currentCourseId, Collections.emptyList());

        if (bundles.isEmpty()) {
            log.debug("No selectable bundles found for course {}", currentCourseId);
            return;
        }

        Set<Long> lockedIdsForCurrentCourse = findLockedIdsForCourse(bundles, lockedIds);

        for (SectionBundle bundle : bundles) {
            if (!isBundleAllowedByLock(bundle, lockedIdsForCurrentCourse)) {
                log.debug(
                        "Skipping bundle {} because course {} is restricted by lock ids {}",
                        describeBundle(bundle),
                        currentCourseId,
                        lockedIdsForCurrentCourse
                );
                continue;
            }

            if (hasBundleConflict(bundle, currentPath)) {
                log.debug(
                        "Skipping bundle {} because it conflicts with current path {}",
                        describeBundle(bundle),
                        currentPath.stream().map(CourseSection::getId).toList()
                );
                continue;
            }

            currentPath.addAll(bundle.sections());

            generateCombinations(
                    courseIds,
                    index + 1,
                    currentPath,
                    bundlesByCourse,
                    lockedIds,
                    results
            );

            for (int i = 0; i < bundle.sections().size(); i++) {
                currentPath.remove(currentPath.size() - 1);
            }
        }
    }

    private boolean hasBundleConflict(
            SectionBundle bundle,
            List<CourseSection> currentPath
    ) {
        if (bundle == null || CollectionUtils.isEmpty(bundle.sections())) {
            return false;
        }

        List<CourseSection> temporaryPath = new ArrayList<>(currentPath);

        for (CourseSection section : bundle.sections()) {
            if (conflictService.hasConflict(section, temporaryPath)) {
                return true;
            }

            temporaryPath.add(section);
        }

        return false;
    }

    private Set<Long> findLockedIdsForCourse(
            List<SectionBundle> bundles,
            List<Long> lockedIds
    ) {
        if (CollectionUtils.isEmpty(bundles) || CollectionUtils.isEmpty(lockedIds)) {
            return Collections.emptySet();
        }

        Set<Long> courseSectionIds = bundles.stream()
                .flatMap(bundle -> bundle.sections().stream())
                .map(CourseSection::getId)
                .collect(Collectors.toSet());

        return lockedIds.stream()
                .filter(courseSectionIds::contains)
                .collect(Collectors.toSet());
    }

    private boolean isBundleAllowedByLock(
            SectionBundle bundle,
            Set<Long> lockedIdsForCourse
    ) {
        if (bundle == null || CollectionUtils.isEmpty(lockedIdsForCourse)) {
            return true;
        }

        Set<Long> bundleIds = bundle.sectionIds();

        return bundleIds.containsAll(lockedIdsForCourse);
    }

    private boolean isParentlessSection(CourseSection section) {
        return parentLectureSectionId(section) == null;
    }

    private int compareSections(CourseSection first, CourseSection second) {
        String firstNumber = first != null && first.getSectionNumber() != null
                ? first.getSectionNumber()
                : "";

        String secondNumber = second != null && second.getSectionNumber() != null
                ? second.getSectionNumber()
                : "";

        Integer firstInt = parseIntOrNull(firstNumber);
        Integer secondInt = parseIntOrNull(secondNumber);

        if (firstInt != null && secondInt != null) {
            return firstInt.compareTo(secondInt);
        }

        return firstNumber.compareToIgnoreCase(secondNumber);
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String describeBundle(SectionBundle bundle) {
        if (bundle == null || CollectionUtils.isEmpty(bundle.sections())) {
            return "[]";
        }

        return bundle.sections()
                .stream()
                .map(section -> section.getId()
                        + ":"
                        + section.getSectionType()
                        + ":"
                        + section.getSectionNumber()
                        + ":sessions="
                        + (section.getClassSessions() == null
                        ? 0
                        : section.getClassSessions().size()))
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private static final class SectionBundle {
        private final Long courseId;
        private final List<CourseSection> sections;

        private SectionBundle(
                Long courseId,
                List<CourseSection> sections
        ) {
            this.courseId = courseId;
            this.sections = sections == null
                    ? Collections.emptyList()
                    : List.copyOf(sections);
        }

        private Long courseId() {
            return courseId;
        }

        private List<CourseSection> sections() {
            return sections;
        }

        private Set<Long> sectionIds() {
            if (CollectionUtils.isEmpty(sections)) {
                return Collections.emptySet();
            }

            return sections.stream()
                    .map(CourseSection::getId)
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }
    private Long parentLectureSectionId(CourseSection section) {
        if (section == null || section.getParentLectureSection() == null) {
            return null;
        }

        return section.getParentLectureSection().getId();
    }

}