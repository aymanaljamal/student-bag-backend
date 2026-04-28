package com.studentbag.backend.schedule.repository;

import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentScheduleRepository extends JpaRepository<StudentSchedule, Long> {
    @Query("""
    SELECT DISTINCT s
    FROM StudentSchedule s
    LEFT JOIN FETCH s.entries e
    LEFT JOIN FETCH e.courseSection cs
    LEFT JOIN FETCH cs.instructor
    LEFT JOIN FETCH e.event ev
    WHERE s.student.id = :studentId
    ORDER BY s.id DESC
""")
    List<StudentSchedule> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("""
        SELECT DISTINCT s
        FROM StudentSchedule s
        LEFT JOIN FETCH s.entries e
        LEFT JOIN FETCH e.courseSection cs
        LEFT JOIN FETCH cs.instructor
        LEFT JOIN FETCH e.event ev
        WHERE s.id = :scheduleId
    """)
    Optional<StudentSchedule> findByIdWithEntries(@Param("scheduleId") Long scheduleId);

    Optional<StudentSchedule> findFirstByStudentIdAndStatusOrderByIdDesc(
            Long studentId,
            ScheduleStatus status
    );

    List<StudentSchedule> findByStudentId(Long studentId);

    List<StudentSchedule> findAllByStudentIdAndTermIdAndStatus(
            Long studentId,
            Long termId,
            ScheduleStatus status
    );

    List<StudentSchedule> findByStudent_IdOrderByCreatedAtDesc(Long studentId);

    Optional<StudentSchedule> findFirstByStudent_IdAndStatusOrderByActivatedAtDescCreatedAtDesc(
            Long studentId,
            ScheduleStatus status
    );

    List<StudentSchedule> findByStudent_IdAndTerm_Id(Long studentId, Long termId);

    Optional<StudentSchedule> findByStudentIdAndTermIdAndStatus(
            Long studentId,
            Long termId,
            ScheduleStatus status
    );

    List<StudentSchedule> findAllByStudentIdAndTermId(Long studentId, Long termId);

    @Modifying
    @Query("""
        UPDATE StudentSchedule s
        SET s.status = 'ARCHIVED'
        WHERE s.student.id = :studentId
          AND s.term.id = :termId
          AND s.id <> :activeScheduleId
    """)
    void archiveOldSchedules(
            @Param("studentId") Long studentId,
            @Param("termId") Long termId,
            @Param("activeScheduleId") Long activeScheduleId
    );
}