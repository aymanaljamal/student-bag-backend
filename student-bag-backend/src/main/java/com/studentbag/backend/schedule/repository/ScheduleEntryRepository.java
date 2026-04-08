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
    @Query("SELECT se FROM ScheduleEntry se WHERE se.student.id = :studentId " +
            "AND se.startDateTime < :end AND se.endDateTime > :start")
    List<ScheduleEntry> findConflictingEntries(Long studentId, LocalDateTime start, LocalDateTime end);
}