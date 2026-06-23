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

    /*
     * Used for reminders / scheduled jobs / calendar checks.
     * Only ACTIVE events should be returned here.
     * Deleted, cancelled, and finished events should not trigger reminders.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE e.startDateTime IS NOT NULL
          AND e.startDateTime >= :from
          AND e.startDateTime < :to
          AND (e.status IS NULL OR e.status = com.studentbag.backend.domain.enums.EventStatus.ACTIVE)
    """)
    List<Event> findEventsStartingBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /*
     * General visible filter.
     * This should not return soft-deleted events.
     * It also excludes cancelled/finished because the query is startDate >= startDate,
     * so it is usually used for upcoming visible events.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE (:type IS NULL OR e.eventType = :type)
          AND (:dept IS NULL OR e.department = :dept)
          AND (:loc IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :loc, '%')))
          AND e.startDateTime >= :startDate
          AND (e.status IS NULL OR e.status = com.studentbag.backend.domain.enums.EventStatus.ACTIVE)
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> findFilteredEvents(
            @Param("type") EventType type,
            @Param("dept") String dept,
            @Param("loc") String loc,
            @Param("startDate") LocalDateTime startDate,
            Pageable pageable
    );

    /*
     * Keep this method as-is for backward compatibility.
     * Important:
     * This returns all events in the institution, including CANCELLED and DELETED.
     * If you need visible-only events, use findVisibleByInstitutionId.
     */
    List<Event> findAllByInstitutionId(Long institutionId);

    /*
     * Visible institution events only.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE e.institution.id = :institutionId
          AND (e.status IS NULL OR e.status = com.studentbag.backend.domain.enums.EventStatus.ACTIVE)
        ORDER BY e.startDateTime ASC
    """)
    List<Event> findVisibleByInstitutionId(
            @Param("institutionId") Long institutionId
    );

    /*
     * Keyword search for visible active events only.
     * This avoids showing deleted/cancelled items in general public search.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE (
            LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(e.description) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(e.location) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(e.department) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(e.host) LIKE LOWER(CONCAT('%', :query, '%'))
          )
          AND (e.status IS NULL OR e.status = com.studentbag.backend.domain.enums.EventStatus.ACTIVE)
        ORDER BY e.startDateTime ASC
    """)
    List<Event> searchByKeyword(@Param("query") String query);

    /*
     * Date range for visible active events.
     */
    @Query("""
        SELECT e
        FROM Event e
        WHERE e.startDateTime IS NOT NULL
          AND e.startDateTime >= :start
          AND e.startDateTime <= :end
          AND (e.status IS NULL OR e.status = com.studentbag.backend.domain.enums.EventStatus.ACTIVE)
        ORDER BY e.startDateTime ASC
    """)
    List<Event> findAllByStartDateTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    /*
     * Used by AI:
     * Returns only upcoming active system events/opportunities.
     * Deleted/cancelled/finished events should not be suggested by AI.
     */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        LEFT JOIN FETCH e.opportunity o
        WHERE e.startDateTime IS NOT NULL
          AND e.startDateTime >= :now
          AND (e.status IS NULL OR e.status = com.studentbag.backend.domain.enums.EventStatus.ACTIVE)
        ORDER BY e.startDateTime ASC
    """)
    List<Event> findUpcomingVisibleEventsForAi(
            @Param("now") LocalDateTime now
    );
}
