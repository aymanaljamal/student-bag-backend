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
        SELECT e
        FROM Event e
        WHERE e.startDateTime IS NOT NULL
          AND e.startDateTime >= :from
          AND e.startDateTime < :to
    """)
    List<Event> findEventsStartingBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
        SELECT e
        FROM Event e
        WHERE (:type IS NULL OR e.eventType = :type)
          AND (:dept IS NULL OR e.department = :dept)
          AND (:loc IS NULL OR e.location LIKE %:loc%)
          AND e.startDateTime >= :startDate
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> findFilteredEvents(
            @Param("type") EventType type,
            @Param("dept") String dept,
            @Param("loc") String loc,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );

    List<Event> findAllByInstitutionId(Long institutionId);

    @Query("""
        SELECT e
        FROM Event e
        WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    List<Event> searchByKeyword(@Param("query") String query);

    List<Event> findAllByStartDateTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    /*
     * Used by AI:
     * Returns all upcoming system events/opportunities, not only registered events.
     *
     * If your Event entity has isDeleted / cancelled / visible fields,
     * add them inside this query.
     */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        LEFT JOIN FETCH e.opportunity o
        WHERE e.startDateTime IS NOT NULL
          AND e.startDateTime >= :now
        ORDER BY e.startDateTime ASC
    """)
    List<Event> findUpcomingVisibleEventsForAi(
            @Param("now") LocalDateTime now
    );
}