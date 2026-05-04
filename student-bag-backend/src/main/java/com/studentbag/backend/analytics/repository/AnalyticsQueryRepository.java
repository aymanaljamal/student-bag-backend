package com.studentbag.backend.analytics.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnalyticsQueryRepository {

    private final JdbcTemplate jdbc;

    public record ChartRow(String label, Long value) {}
    public record TimeSeriesRow(LocalDate date, Long value) {}
    public record NearestTaskRow(Long id, String title, LocalDateTime dueDateTime) {}
    public record LastEditedNoteRow(Long id, String title, LocalDateTime updatedAt) {}
    public record NearestEventRow(Long id, String title, LocalDateTime startDateTime) {}

    private Long count(String sql, Object... args) {
        Long value = jdbc.queryForObject(sql, Long.class, args);
        return value == null ? 0L : value;
    }

    private Long countSafe(String sql, Object... args) {
        try {
            return count(sql, args);
        } catch (Exception ex) {
            log.warn("Analytics count query failed: {}", ex.getMessage());
            return 0L;
        }
    }

    private List<ChartRow> chartSafe(String sql, Object... args) {
        try {
            return jdbc.query(
                    sql,
                    (rs, rowNum) -> new ChartRow(
                            rs.getString("label"),
                            rs.getLong("value")
                    ),
                    args
            );
        } catch (Exception ex) {
            log.warn("Analytics chart query failed: {}", ex.getMessage());
            return List.of();
        }
    }

    private List<TimeSeriesRow> timeSeriesSafe(String sql, Object... args) {
        try {
            return jdbc.query(
                    sql,
                    (rs, rowNum) -> new TimeSeriesRow(
                            rs.getDate("date").toLocalDate(),
                            rs.getLong("value")
                    ),
                    args
            );
        } catch (Exception ex) {
            log.warn("Analytics time-series query failed: {}", ex.getMessage());
            return List.of();
        }
    }

    // -------------------------------------------------------------------------
    // Identity Resolvers
    // -------------------------------------------------------------------------

    public Long resolveStudentIdByEmail(String email) {
        return countSafe("""
            SELECT s.id
            FROM students s
            JOIN users u ON u.id = s.user_id
            WHERE u.email = ?
        """, email);
    }

    public Long resolveInstructorIdByEmail(String email) {
        return countSafe("""
            SELECT i.id
            FROM instructors i
            JOIN users u ON u.id = i.user_id
            WHERE u.email = ?
        """, email);
    }

    public Long resolveAdministratorIdByEmail(String email) {
        return countSafe("""
            SELECT a.id
            FROM administrators a
            JOIN users u ON u.id = a.user_id
            WHERE u.email = ?
        """, email);
    }

    public Long resolveStudentIdByUserId(UUID userId) {
        return countSafe("""
            SELECT id
            FROM students
            WHERE user_id = ?
        """, userId);
    }

    public Long resolveInstructorIdByUserId(UUID userId) {
        return countSafe("""
            SELECT id
            FROM instructors
            WHERE user_id = ?
        """, userId);
    }

    public Long resolveAdministratorIdByUserId(UUID userId) {
        return countSafe("""
            SELECT id
            FROM administrators
            WHERE user_id = ?
        """, userId);
    }

    // -------------------------------------------------------------------------
    // Student Tasks Analytics
    // -------------------------------------------------------------------------

    public Long countStudentTasks(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentCompletedTasks(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND status = 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentActiveTasks(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND status <> 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """, studentId);
    }

    public Long countStudentOverdueTasks(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND status <> 'COMPLETED'
              AND due_date_time < CURRENT_TIMESTAMP
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """, studentId);
    }

    public Long countStudentTasksDueToday(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND DATE(due_date_time) = CURRENT_DATE
              AND status <> 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """, studentId);
    }

    public Long countStudentTasksAddedThisWeek(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public NearestTaskRow findNearestStudentTask(Long studentId) {
        try {
            List<NearestTaskRow> rows = jdbc.query("""
                SELECT id, title, due_date_time
                FROM tasks
                WHERE student_id = ?
                  AND status <> 'COMPLETED'
                  AND due_date_time >= CURRENT_TIMESTAMP
                  AND COALESCE(is_deleted, false) = false
                  AND COALESCE(archived, false) = false
                ORDER BY due_date_time ASC
                LIMIT 1
            """, (rs, rowNum) -> new NearestTaskRow(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getTimestamp("due_date_time").toLocalDateTime()
            ), studentId);

            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.warn("Nearest task query failed: {}", ex.getMessage());
            return null;
        }
    }

    public List<ChartRow> studentTasksByStatus(Long studentId) {
        return List.of(
                new ChartRow("Active", countStudentActiveTasks(studentId)),
                new ChartRow("Completed", countStudentCompletedTasks(studentId)),
                new ChartRow("Overdue", countStudentOverdueTasks(studentId))
        );
    }

    public List<TimeSeriesRow> studentTasksCreatedThisWeek(Long studentId) {
        return timeSeriesSafe("""
            SELECT DATE(created_at) AS date, COUNT(*) AS value
            FROM tasks
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
        """, studentId);
    }

    // -------------------------------------------------------------------------
    // Student Notes Analytics
    // -------------------------------------------------------------------------

    public Long countStudentNotes(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentPinnedNotes(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_pinned, false) = true
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentImportantNotes(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_important, false) = true
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentArchivedNotes(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_archived, false) = true
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentNotesAddedThisWeek(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public LastEditedNoteRow findLastEditedStudentNote(Long studentId) {
        try {
            List<LastEditedNoteRow> rows = jdbc.query("""
                SELECT id, title, updated_at
                FROM notes
                WHERE student_id = ?
                  AND COALESCE(is_deleted, false) = false
                ORDER BY updated_at DESC NULLS LAST, created_at DESC
                LIMIT 1
            """, (rs, rowNum) -> new LastEditedNoteRow(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getTimestamp("updated_at") == null
                            ? null
                            : rs.getTimestamp("updated_at").toLocalDateTime()
            ), studentId);

            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.warn("Last edited note query failed: {}", ex.getMessage());
            return null;
        }
    }

    public List<ChartRow> studentNotesByStatus(Long studentId) {
        return List.of(
                new ChartRow("Pinned", countStudentPinnedNotes(studentId)),
                new ChartRow("Important", countStudentImportantNotes(studentId)),
                new ChartRow("Archived", countStudentArchivedNotes(studentId))
        );
    }

    public List<TimeSeriesRow> studentNotesCreatedThisWeek(Long studentId) {
        return timeSeriesSafe("""
            SELECT DATE(created_at) AS date, COUNT(*) AS value
            FROM notes
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
        """, studentId);
    }

    // -------------------------------------------------------------------------
    // Student Schedule Analytics
    // -------------------------------------------------------------------------

    public Boolean hasActiveSchedule(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM student_schedules
            WHERE student_id = ?
              AND status = 'ACTIVE'
        """, studentId) > 0;
    }

    public String findActiveScheduleName(Long studentId) {
        try {
            List<String> rows = jdbc.query("""
                SELECT COALESCE(t.name, CONCAT('Active Schedule #', ss.id)) AS name
                FROM student_schedules ss
                LEFT JOIN terms t ON t.id = ss.term_id
                WHERE ss.student_id = ?
                  AND ss.status = 'ACTIVE'
                ORDER BY ss.id DESC
                LIMIT 1
            """, (rs, rowNum) -> rs.getString("name"), studentId);

            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.warn("Active schedule name query failed: {}", ex.getMessage());
            return null;
        }
    }

    public Long countActiveScheduleCourses(Long studentId) {
        return countSafe("""
            SELECT COUNT(DISTINCT cs.course_id)
            FROM schedule_entries se
            JOIN student_schedules ss ON ss.id = se.schedule_id
            JOIN course_sections cs ON cs.id = se.course_section_id
            WHERE ss.student_id = ?
              AND ss.status = 'ACTIVE'
              AND se.source_type = 'COURSE'
        """, studentId);
    }

    public Long countTodayScheduleSessions(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM schedule_entries se
            JOIN student_schedules ss ON ss.id = se.schedule_id
            WHERE ss.student_id = ?
              AND ss.status = 'ACTIVE'
              AND DATE(se.start_date_time) = CURRENT_DATE
        """, studentId);
    }

    public Long countActiveScheduleDaysOff(Long studentId) {
        Long activeDays = countSafe("""
            SELECT COUNT(DISTINCT EXTRACT(DOW FROM se.start_date_time))
            FROM schedule_entries se
            JOIN student_schedules ss ON ss.id = se.schedule_id
            WHERE ss.student_id = ?
              AND ss.status = 'ACTIVE'
        """, studentId);

        return Math.max(0L, 7L - activeDays);
    }

    // -------------------------------------------------------------------------
    // Student Grades + Library + Events
    // -------------------------------------------------------------------------
    public List<TimeSeriesRow> studentGradeTrend(Long studentId) {
        return timeSeriesSafe("""
        SELECT 
            DATE(COALESCE(updated_at, created_at)) AS date,
            ROUND(COALESCE(calculated_percentage, 0)) AS value
        FROM grade_calculations
        WHERE student_id = ?
        ORDER BY COALESCE(updated_at, created_at) DESC
        LIMIT 5
    """, studentId);
    }

    public List<TimeSeriesRow> studentUpcomingEventsTimeline(Long studentId) {
        return timeSeriesSafe("""
        SELECT 
            DATE(e.start_date_time) AS date,
            COUNT(*) AS value
        FROM event_registrations r
        JOIN events e ON e.id = r.event_id
        WHERE r.student_id = ?
          AND COALESCE(r.status, 'REGISTERED') <> 'CANCELLED'
          AND e.start_date_time >= CURRENT_TIMESTAMP
        GROUP BY DATE(e.start_date_time)
        ORDER BY DATE(e.start_date_time) ASC
        LIMIT 5
    """, studentId);
    }
    public Long countStudentGradeCalculations(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM grade_calculations
            WHERE student_id = ?
        """, studentId);
    }

    public Double findStudentLatestGpa(Long studentId) {
        try {
            List<Double> rows = jdbc.query("""
                SELECT calculated_gpa
                FROM grade_calculations
                WHERE student_id = ?
                ORDER BY updated_at DESC NULLS LAST, created_at DESC
                LIMIT 1
            """, (rs, rowNum) -> rs.getDouble("calculated_gpa"), studentId);

            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.warn("Latest GPA query failed: {}", ex.getMessage());
            return null;
        }
    }

    public Double findStudentLatestPercentage(Long studentId) {
        try {
            List<Double> rows = jdbc.query("""
                SELECT calculated_percentage
                FROM grade_calculations
                WHERE student_id = ?
                ORDER BY updated_at DESC NULLS LAST, created_at DESC
                LIMIT 1
            """, (rs, rowNum) -> rs.getDouble("calculated_percentage"), studentId);

            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.warn("Latest percentage query failed: {}", ex.getMessage());
            return null;
        }
    }

    public Long countStudentLibraryFolders(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM personal_resource_folders
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentLibraryItems(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM personal_resource_items
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentCopiedFromPublic(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM resource_share_copy_logs
            WHERE student_id = ?
        """, studentId);
    }

    public Long countStudentUpcomingReminders(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM task_reminders tr
            JOIN tasks t ON t.id = tr.task_id
            WHERE t.student_id = ?
              AND COALESCE(tr.enabled, true) = true
              AND COALESCE(tr.sent, false) = false
              AND tr.remind_at >= CURRENT_TIMESTAMP
              AND tr.remind_at < CURRENT_TIMESTAMP + INTERVAL '7 days'
              AND t.status <> 'COMPLETED'
              AND COALESCE(t.is_deleted, false) = false
              AND COALESCE(t.archived, false) = false
        """, studentId);
    }

    public Long countStudentRegisteredEvents(Long studentId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM event_registrations
            WHERE student_id = ?
              AND COALESCE(status, 'REGISTERED') <> 'CANCELLED'
        """, studentId);
    }

    public NearestEventRow findNearestStudentEvent(Long studentId) {
        try {
            List<NearestEventRow> rows = jdbc.query("""
                SELECT e.id, e.title, e.start_date_time
                FROM event_registrations r
                JOIN events e ON e.id = r.event_id
                WHERE r.student_id = ?
                  AND COALESCE(r.status, 'REGISTERED') <> 'CANCELLED'
                  AND e.start_date_time >= CURRENT_TIMESTAMP
                ORDER BY e.start_date_time ASC
                LIMIT 1
            """, (rs, rowNum) -> new NearestEventRow(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getTimestamp("start_date_time").toLocalDateTime()
            ), studentId);

            return rows.isEmpty() ? null : rows.get(0);
        } catch (Exception ex) {
            log.warn("Nearest event query failed: {}", ex.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Instructor Analytics
    // -------------------------------------------------------------------------

    public Long countInstructorResources(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE uploaded_by_type = 'INSTRUCTOR'
              AND uploaded_by_id = ?
              AND COALESCE(is_deleted, false) = false
        """, instructorId);
    }

    public Long countInstructorResourcesByStatus(Long instructorId, String status) {
        return countSafe("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE uploaded_by_type = 'INSTRUCTOR'
              AND uploaded_by_id = ?
              AND approval_status = ?
              AND COALESCE(is_deleted, false) = false
        """, instructorId, status);
    }

    public Long countInstructorEvents(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
        """, instructorId);
    }

    public Long countInstructorActiveEvents(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND e.start_date_time <= CURRENT_TIMESTAMP
              AND e.end_date_time >= CURRENT_TIMESTAMP
        """, instructorId);
    }

    public Long countInstructorEndedEvents(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND e.end_date_time < CURRENT_TIMESTAMP
        """, instructorId);
    }

    public Long countInstructorUpcomingEvents(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND e.start_date_time > CURRENT_TIMESTAMP
        """, instructorId);
    }

    public Long countInstructorOpportunities(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM opportunities o
            JOIN events e ON e.id = o.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
        """, instructorId);
    }

    public Long countInstructorPaidOpportunities(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM opportunities o
            JOIN events e ON e.id = o.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND COALESCE(o.is_paid, false) = true
        """, instructorId);
    }

    public Long countInstructorEventRegistrations(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM event_registrations er
            JOIN events e ON e.id = er.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND COALESCE(er.status, 'REGISTERED') <> 'CANCELLED'
        """, instructorId);
    }

    public Long countInstructorEventCheckIns(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM event_registrations er
            JOIN events e ON e.id = er.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND er.checked_in_at IS NOT NULL
        """, instructorId);
    }

    public Long countInstructorSections(Long instructorId) {
        return countSafe("""
            SELECT COUNT(*)
            FROM course_sections
            WHERE instructor_id = ?
        """, instructorId);
    }

    public Long sumInstructorSectionsCapacity(Long instructorId) {
        return countSafe("""
            SELECT COALESCE(SUM(capacity), 0)
            FROM course_sections
            WHERE instructor_id = ?
        """, instructorId);
    }

    public Long sumInstructorSectionsEnrolled(Long instructorId) {
        return countSafe("""
            SELECT COALESCE(SUM(enrolled_count), 0)
            FROM course_sections
            WHERE instructor_id = ?
        """, instructorId);
    }

    // -------------------------------------------------------------------------
    // Admin Users Analytics
    // -------------------------------------------------------------------------

    public Long countUsers() {
        return countSafe("SELECT COUNT(*) FROM users");
    }

    public Long countUsersByRole(String role) {
        return countSafe("""
            SELECT COUNT(*)
            FROM users
            WHERE role = ?
        """, role);
    }

    public Long countStudents() {
        return countSafe("SELECT COUNT(*) FROM students");
    }

    public Long countInstructors() {
        return countSafe("SELECT COUNT(*) FROM instructors");
    }

    public Long countAdministrators() {
        return countSafe("SELECT COUNT(*) FROM administrators");
    }

    public Long countActiveStudents() {
        return countSafe("""
            SELECT COUNT(*)
            FROM students s
            JOIN users u ON u.id = s.user_id
            WHERE COALESCE(u.enabled, true) = true
        """);
    }

    public Long countNewUsersThisMonth() {
        return countSafe("""
            SELECT COUNT(*)
            FROM users
            WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE)
        """);
    }

    public Long countUsersCreatedThisWeek() {
        return countSafe("""
            SELECT COUNT(*)
            FROM users
            WHERE created_at >= DATE_TRUNC('week', CURRENT_DATE)
        """);
    }

    public List<ChartRow> usersByRole() {
        return chartSafe("""
            SELECT role AS label, COUNT(*) AS value
            FROM users
            GROUP BY role
            ORDER BY value DESC
        """);
    }

    // -------------------------------------------------------------------------
    // Admin Resources Analytics
    // -------------------------------------------------------------------------

    public Long countResources() {
        return countSafe("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE COALESCE(is_deleted, false) = false
        """);
    }

    public Long countResourcesByStatus(String status) {
        return countSafe("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE approval_status = ?
              AND COALESCE(is_deleted, false) = false
        """, status);
    }

    public Long countApprovedResources() {
        return countResourcesByStatus("APPROVED");
    }

    public Long countPendingResources() {
        return countResourcesByStatus("PENDING");
    }

    public Long countRejectedResources() {
        return countResourcesByStatus("REJECTED");
    }

    public Long countVisibleResources() {
        return countSafe("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE COALESCE(is_visible, false) = true
              AND COALESCE(is_deleted, false) = false
        """);
    }

    public Long countPersonalFolders() {
        return countSafe("""
            SELECT COUNT(*)
            FROM personal_resource_folders
            WHERE COALESCE(is_deleted, false) = false
        """);
    }

    public Long countPersonalItems() {
        return countSafe("""
            SELECT COUNT(*)
            FROM personal_resource_items
            WHERE COALESCE(is_deleted, false) = false
        """);
    }

    public Long countResourcesCreatedThisWeek() {
        return countSafe("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
        """);
    }

    public List<ChartRow> resourcesByStatus() {
        return chartSafe("""
            SELECT approval_status AS label, COUNT(*) AS value
            FROM admin_resources
            WHERE COALESCE(is_deleted, false) = false
            GROUP BY approval_status
            ORDER BY value DESC
        """);
    }

    // -------------------------------------------------------------------------
    // Admin Events Analytics
    // -------------------------------------------------------------------------

    public Long countEvents() {
        return countSafe("SELECT COUNT(*) FROM events");
    }

    public Long countActiveEvents() {
        return countSafe("""
        SELECT COUNT(*)
        FROM events
        WHERE start_date_time <= CURRENT_TIMESTAMP
          AND end_date_time >= CURRENT_TIMESTAMP
          AND start_date_time <= end_date_time
    """);
    }
    public Long countNotificationsSentTotal() {
        return countSafe("""
        SELECT COUNT(*)
        FROM user_notifications
        WHERE status IN ('SENT', 'DELIVERED', 'READ')
    """);
    }
    public Long countEndedEvents() {
        return countSafe("""
            SELECT COUNT(*)
            FROM events
            WHERE end_date_time < CURRENT_TIMESTAMP
        """);
    }

    public Long countUpcomingEvents() {
        return countSafe("""
        SELECT COUNT(*)
        FROM events
        WHERE start_date_time > CURRENT_TIMESTAMP
    """);
    }

    public Long countFinishedEvents() {
        return countSafe("""
        SELECT COUNT(*)
        FROM events
        WHERE end_date_time IS NOT NULL
          AND end_date_time < CURRENT_TIMESTAMP
          AND start_date_time <= end_date_time
    """);
    }

    public Long countEventsRequiresRegistration() {
        return countSafe("""
            SELECT COUNT(*)
            FROM events
            WHERE COALESCE(requires_registration, false) = true
        """);
    }

    public Long countAllEventRegistrations() {
        return countSafe("""
            SELECT COUNT(*)
            FROM event_registrations
            WHERE COALESCE(status, 'REGISTERED') <> 'CANCELLED'
        """);
    }

    public Long countEventRegistrations() {
        return countAllEventRegistrations();
    }

    public Long countAllOpportunities() {
        return countSafe("SELECT COUNT(*) FROM opportunities");
    }

    public Long countOpportunities() {
        return countAllOpportunities();
    }

    public Long countPaidOpportunities() {
        return countSafe("""
            SELECT COUNT(*)
            FROM opportunities
            WHERE COALESCE(is_paid, false) = true
        """);
    }

    public Long countEventsCreatedThisWeek() {
        return countSafe("""
            SELECT COUNT(*)
            FROM events
            WHERE created_at >= DATE_TRUNC('week', CURRENT_DATE)
        """);
    }

    public List<ChartRow> eventsByType() {
        return chartSafe("""
            SELECT type AS label, COUNT(*) AS value
            FROM events
            GROUP BY type
            ORDER BY value DESC
        """);
    }

    // -------------------------------------------------------------------------
    // Admin Notifications Analytics
    // -------------------------------------------------------------------------

    public Long countNotificationCampaigns() {
        return countSafe("SELECT COUNT(*) FROM notifications");
    }

    public Long countNotificationCampaignsToday() {
        return countSafe("""
        SELECT COUNT(*)
        FROM notifications
        WHERE DATE(created_at) = CURRENT_DATE
    """);
    }
    public Long countNotificationsSentToday() {
        return countSafe("""
        SELECT COUNT(*)
        FROM user_notifications
        WHERE DATE(delivered_at) = CURRENT_DATE
          AND status IN ('SENT', 'DELIVERED', 'READ')
    """);
    }

    public Long countNotificationsSentThisMonth() {
        return countSafe("""
        SELECT COUNT(*)
        FROM user_notifications
        WHERE delivered_at >= DATE_TRUNC('month', CURRENT_DATE)
          AND status IN ('SENT', 'DELIVERED', 'READ')
    """);
    }


    public Long countUnreadNotifications() {
        return countSafe("""
        SELECT COUNT(*)
        FROM user_notifications
        WHERE COALESCE(read_flag, false) = false
          AND status IN ('SENT', 'DELIVERED')
    """);
    }

    public Long countReadNotifications() {
        return countSafe("""
        SELECT COUNT(*)
        FROM user_notifications
        WHERE COALESCE(read_flag, false) = true
           OR status = 'READ'
    """);
    }

    public List<ChartRow> notificationsByStatus() {
        return chartSafe("""
        SELECT status AS label, COUNT(*) AS value
        FROM user_notifications
        GROUP BY status
        ORDER BY value DESC
    """);
    }
    // -------------------------------------------------------------------------
    // Admin Academic Structure Analytics
    // -------------------------------------------------------------------------

    public Long countInstitutions() {
        return countSafe("SELECT COUNT(*) FROM institutions");
    }

    public Long countActiveInstitutions() {
        return countSafe("""
            SELECT COUNT(*)
            FROM institutions
            WHERE COALESCE(active, false) = true
        """);
    }

    public Long countCourses() {
        return countSafe("SELECT COUNT(*) FROM courses");
    }

    public Long countActiveCourses() {
        return countSafe("""
            SELECT COUNT(*)
            FROM courses
            WHERE COALESCE(is_active, false) = true
        """);
    }

    public Long countFaculties() {
        return countSafe("SELECT COUNT(*) FROM faculties");
    }

    public Long countActiveFaculties() {
        return countSafe("""
            SELECT COUNT(*)
            FROM faculties
            WHERE COALESCE(is_active, false) = true
        """);
    }

    public Long countDepartments() {
        return countSafe("SELECT COUNT(*) FROM departments");
    }

    public Long countActiveDepartments() {
        return countSafe("""
            SELECT COUNT(*)
            FROM departments
            WHERE COALESCE(is_active, false) = true
        """);
    }

    public Long countTerms() {
        return countSafe("SELECT COUNT(*) FROM terms");
    }

    public Long countCurrentTerms() {
        return countSafe("""
            SELECT COUNT(*)
            FROM terms
            WHERE COALESCE(is_current, false) = true
        """);
    }

    public Long countCourseSections() {
        return countSafe("SELECT COUNT(*) FROM course_sections");
    }

    public Long countClassSessions() {
        return countSafe("SELECT COUNT(*) FROM class_sessions");
    }

    // -------------------------------------------------------------------------
    // Admin Notes + Tasks Analytics
    // -------------------------------------------------------------------------

    public Long countNotes() {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE COALESCE(is_deleted, false) = false
        """);
    }

    public Long countImportantNotes() {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE COALESCE(is_important, false) = true
              AND COALESCE(is_deleted, false) = false
        """);
    }

    public Long countPinnedNotes() {
        return countSafe("""
            SELECT COUNT(*)
            FROM notes
            WHERE COALESCE(is_pinned, false) = true
              AND COALESCE(is_deleted, false) = false
        """);
    }

    public Long countNoteAttachments() {
        return countSafe("SELECT COUNT(*) FROM note_attachments");
    }

    public Long countTasks() {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE COALESCE(is_deleted, false) = false
        """);
    }

    public Long countActiveTasks() {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE status <> 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """);
    }

    public Long countCompletedTasks() {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE status = 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
        """);
    }

    public Long countOverdueTasks() {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE status <> 'COMPLETED'
              AND due_date_time < CURRENT_TIMESTAMP
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """);
    }

    public Long countTasksDueToday() {
        return countSafe("""
            SELECT COUNT(*)
            FROM tasks
            WHERE DATE(due_date_time) = CURRENT_DATE
              AND status <> 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """);
    }

    // -------------------------------------------------------------------------
    // Admin Schedule + Grade Analytics
    // -------------------------------------------------------------------------

    public Long countSchedules() {
        return countSafe("SELECT COUNT(*) FROM student_schedules");
    }

    public Long countActiveSchedules() {
        return countSafe("""
            SELECT COUNT(*)
            FROM student_schedules
            WHERE status = 'ACTIVE'
        """);
    }

    public Long countArchivedSchedules() {
        return countSafe("""
            SELECT COUNT(*)
            FROM student_schedules
            WHERE status = 'ARCHIVED'
        """);
    }

    public Long countScheduleEntries() {
        return countSafe("SELECT COUNT(*) FROM schedule_entries");
    }

    public Long countGradeCalculations() {
        return countSafe("SELECT COUNT(*) FROM grade_calculations");
    }

    public Long countGradeCalculationItems() {
        return countSafe("SELECT COUNT(*) FROM grade_course_items");
    }

    public Long countGradeCalculationsCreatedThisWeek() {
        return countSafe("""
            SELECT COUNT(*)
            FROM grade_calculations
            WHERE created_at >= DATE_TRUNC('week', CURRENT_DATE)
        """);
    }

    // -------------------------------------------------------------------------
    // Admin Combined Charts
    // -------------------------------------------------------------------------

    public List<TimeSeriesRow> weeklyActivity() {
        return timeSeriesSafe("""
            SELECT date, SUM(value) AS value
            FROM (
                SELECT DATE(created_at) AS date, COUNT(*) AS value
                FROM notes
                WHERE created_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY DATE(created_at)

                UNION ALL

                SELECT DATE(created_at) AS date, COUNT(*) AS value
                FROM tasks
                WHERE created_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY DATE(created_at)

                UNION ALL

                SELECT DATE(created_at) AS date, COUNT(*) AS value
                FROM admin_resources
                WHERE created_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY DATE(created_at)

                UNION ALL

                SELECT DATE(created_at) AS date, COUNT(*) AS value
                FROM events
                WHERE created_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY DATE(created_at)

                UNION ALL

                SELECT DATE(created_at) AS date, COUNT(*) AS value
                FROM user_notifications
                WHERE created_at >= CURRENT_DATE - INTERVAL '6 days'
                GROUP BY DATE(created_at)
            ) x
            GROUP BY date
            ORDER BY date ASC
        """);
    }
}