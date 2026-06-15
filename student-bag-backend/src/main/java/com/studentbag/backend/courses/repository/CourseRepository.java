package com.studentbag.backend.courses.repository;

import com.studentbag.backend.courses.entity.Course;
import com.studentbag.backend.institution.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Course entity
 */
public interface CourseRepository extends JpaRepository<Course, Long>,
        JpaSpecificationExecutor<Course> {

    Optional<Course> findByExternalIdAndInstitution(String externalId, Institution institution);
    Optional<Course> findByCodeAndInstitution(String code, Institution institution);
    @Query("""
    select distinct c
    from Course c
    left join fetch c.institution i
    left join fetch c.department d
    left join fetch c.sections s
    left join fetch s.instructor ins
    left join fetch ins.user u
    left join fetch s.term t
    left join fetch s.parentLectureSection pls
    where c.id = :id
""")
    Optional<Course> findDetailedById(@Param("id") Long id);
    @Query("""
        SELECT c
        FROM Course c
        ORDER BY c.code ASC
    """)
    List<Course> findAllForManualSchedulePicker();


}