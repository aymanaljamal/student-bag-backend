package com.studentbag.backend.schedule.repository;

import com.studentbag.backend.schedule.entity.ScheduleEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleEntryRepository extends JpaRepository<ScheduleEntry, Long> {

    List<ScheduleEntry> findAllByScheduleIdOrderByStartDateTimeAsc(Long scheduleId);

    @Query("""
        SELECT se
        FROM ScheduleEntry se
        WHERE se.student.id = :studentId
          AND se.startDateTime < :end
          AND se.endDateTime > :start
    """)
    List<ScheduleEntry> findConflictingEntries(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    /*
     * Used by AI:
     * Returns the full active academic schedule for the student.
     * This is needed for questions like:
     * - "شو عندي يوم الاثنين؟"
     * - "مين دكتور محاضرة السبت؟"
     * - "اعطيني جدولي كامل"
     */
    @Query("""
        SELECT se
        FROM ScheduleEntry se
        JOIN se.schedule s
        LEFT JOIN FETCH se.courseSection cs
        LEFT JOIN FETCH cs.course c
        LEFT JOIN FETCH cs.instructor i
        LEFT JOIN FETCH i.user
        LEFT JOIN FETCH se.event e
        WHERE se.student.id = :studentId
          AND s.status = com.studentbag.backend.domain.enums.schedule.ScheduleStatus.ACTIVE
          AND se.isAllDay = false
        ORDER BY se.startDateTime ASC
    """)
    List<ScheduleEntry> findAllActiveScheduleEntriesForAi(Long studentId);

    @Query("""
        SELECT se
        FROM ScheduleEntry se
        JOIN se.schedule s
        LEFT JOIN FETCH se.courseSection cs
        LEFT JOIN FETCH cs.course c
        LEFT JOIN FETCH cs.instructor i
        LEFT JOIN FETCH i.user
        LEFT JOIN FETCH se.event e
        WHERE se.student.id = :studentId
          AND s.status = com.studentbag.backend.domain.enums.schedule.ScheduleStatus.ACTIVE
          AND se.startDateTime BETWEEN :start AND :end
          AND se.isAllDay = false
        ORDER BY se.startDateTime ASC
    """)
    List<ScheduleEntry> findActiveScheduleEntriesBetween(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
        SELECT se
        FROM ScheduleEntry se
        JOIN se.schedule s
        LEFT JOIN FETCH se.courseSection cs
        LEFT JOIN FETCH cs.course c
        LEFT JOIN FETCH cs.instructor i
        LEFT JOIN FETCH i.user
        LEFT JOIN FETCH se.event e
        WHERE se.student.id = :studentId
          AND s.status = com.studentbag.backend.domain.enums.schedule.ScheduleStatus.ACTIVE
          AND se.startDateTime >= :start
          AND se.startDateTime <= :end
          AND se.isAllDay = false
        ORDER BY se.startDateTime ASC
    """)
    List<ScheduleEntry> findActiveUpcomingEntries(
            Long studentId,
            LocalDateTime start,
            LocalDateTime end
    );
}