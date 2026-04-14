package com.studentbag.backend.schedule.repository;

import com.studentbag.backend.domain.enums.schedule.ScheduleStatus;
import com.studentbag.backend.schedule.entity.StudentSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentScheduleRepository extends JpaRepository<StudentSchedule, Long> {
    /**
     * Finds all schedules belonging to a specific student.
     * Resolves: Cannot resolve method 'findAllByStudentId'
     */
    List<StudentSchedule> findAllByStudentId(Long studentId);
    List<StudentSchedule> findByStudentId(Long studentId);
    /**
     * Finds schedules filtered by student, term, and their status (e.g., ACTIVE).
     * Resolves: Cannot resolve method 'findAllByStudentIdAndTermIdAndStatus'
     */
    List<StudentSchedule> findAllByStudentIdAndTermIdAndStatus(Long studentId, Long termId, ScheduleStatus status);
    List<StudentSchedule> findByStudent_IdOrderByCreatedAtDesc(Long studentId);

    Optional<StudentSchedule> findFirstByStudent_IdAndStatusOrderByActivatedAtDescCreatedAtDesc(
            Long studentId,
            ScheduleStatus status
    );

    List<StudentSchedule> findByStudent_IdAndTerm_Id(Long studentId, Long termId);
    /**
     * Useful for finding the specific active schedule to archive it.
     */
    Optional<StudentSchedule> findByStudentIdAndTermIdAndStatus(Long studentId, Long termId, ScheduleStatus status);
    List<StudentSchedule> findAllByStudentIdAndTermId(Long studentId, Long termId);

    @Modifying
    @Query("UPDATE StudentSchedule s SET s.status = 'ARCHIVED' " +
            "WHERE s.student.id = :studentId AND s.term.id = :termId AND s.id <> :activeScheduleId")
    void archiveOldSchedules(Long studentId, Long termId, Long activeScheduleId);
}