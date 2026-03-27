package com.studentbag.backend.courses.service;

import com.studentbag.backend.common.exception.ResourceNotFoundException;
import com.studentbag.backend.courses.entity.CourseSection;
import com.studentbag.backend.courses.repository.CourseSectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseSectionService {

    private final CourseSectionRepository courseSectionRepository;

    public CourseSection create(CourseSection section) {
        return courseSectionRepository.save(section);
    }

    public CourseSection update(Long id, CourseSection updatedSection) {
        CourseSection existing = getById(id);

        existing.setCourse(updatedSection.getCourse());
        existing.setTerm(updatedSection.getTerm());
        existing.setSectionNumber(updatedSection.getSectionNumber());
        existing.setInstructor(updatedSection.getInstructor());
        existing.setCapacity(updatedSection.getCapacity());
        existing.setEnrolled(updatedSection.getEnrolled());
        existing.setIsOfficial(updatedSection.getIsOfficial());

        return courseSectionRepository.save(existing);
    }

    public CourseSection getById(Long id) {
        return courseSectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course section not found with id: " + id));
    }

    public List<CourseSection> getAll() {
        return courseSectionRepository.findAll();
    }

    public List<CourseSection> getByCourse(Long courseId) {
        return courseSectionRepository.findByCourseId(courseId);
    }

    public List<CourseSection> getByTerm(Long termId) {
        return courseSectionRepository.findByTermId(termId);
    }

    public List<CourseSection> getByInstructor(Long instructorId) {
        return courseSectionRepository.findByInstructorId(instructorId);
    }

    public List<CourseSection> getByCourseAndTerm(Long courseId, Long termId) {
        return courseSectionRepository.findByCourseIdAndTermId(courseId, termId);
    }

    public CourseSection enrollStudent(Long sectionId) {
        CourseSection section = getById(sectionId);
        section.enroll();
        return courseSectionRepository.save(section);
    }

    public CourseSection dropStudent(Long sectionId) {
        CourseSection section = getById(sectionId);
        section.drop();
        return courseSectionRepository.save(section);
    }

    public boolean hasCapacity(Long sectionId) {
        return getById(sectionId).hasCapacity();
    }

    public void delete(Long id) {
        CourseSection section = getById(id);
        courseSectionRepository.delete(section);
    }
}