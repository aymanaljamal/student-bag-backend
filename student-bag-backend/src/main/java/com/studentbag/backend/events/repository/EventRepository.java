package com.studentbag.backend.events.repository;

import com.studentbag.backend.domain.enums.schedule.EventType;
import com.studentbag.backend.events.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("""
    select e
    from Event e
    where e.startDateTime is not null
      and e.startDateTime >= :from
      and e.startDateTime < :to
""")
    List<Event> findEventsStartingBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("SELECT e FROM Event e WHERE " +
            "(:type IS NULL OR e.eventType = :type) AND " +
            "(:dept IS NULL OR e.department = :dept) AND " +
            "(:loc IS NULL OR e.location LIKE %:loc%) AND " +
            "(e.startDateTime >= :startDate) " +
            "ORDER BY e.startDateTime ASC")
    Page<Event> findFilteredEvents(
            @Param("type") EventType type,
            @Param("dept") String dept,
            @Param("loc") String loc,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable);

    // FR-9.2 & FR-9.7: Find by institution for staff management
    List<Event> findAllByInstitutionId(Long institutionId);

    // FR-9.9: Support Uni-Bot search by keyword
    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(concat('%', :query, '%')) " +
            "OR LOWER(e.description) LIKE LOWER(concat('%', :query, '%'))")
    List<Event> searchByKeyword(@Param("query") String query);

    // Find upcoming events for Smart Schedule reminders (FR-9.5)
    List<Event> findAllByStartDateTimeBetween(LocalDateTime start, LocalDateTime end);
}