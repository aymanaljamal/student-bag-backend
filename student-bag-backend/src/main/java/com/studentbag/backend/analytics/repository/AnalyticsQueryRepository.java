package com.studentbag.backend.analytics.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    public Long resolveStudentIdByEmail(String email) {
        return jdbc.queryForObject("""
            SELECT s.id
            FROM students s
            JOIN users u ON u.id = s.user_id
            WHERE u.email = ?
        """, Long.class, email);
    }

    public Long resolveInstructorIdByEmail(String email) {
        return jdbc.queryForObject("""
            SELECT i.id
            FROM instructors i
            JOIN users u ON u.id = i.user_id
            WHERE u.email = ?
        """, Long.class, email);
    }

    public Long resolveAdministratorIdByEmail(String email) {
        return jdbc.queryForObject("""
            SELECT a.id
            FROM administrators a
            JOIN users u ON u.id = a.user_id
            WHERE u.email = ?
        """, Long.class, email);
    }

    public Long resolveStudentIdByUserId(UUID userId) {
        return jdbc.queryForObject("""
            SELECT id
            FROM students
            WHERE user_id = ?
        """, Long.class, userId);
    }

    public Long resolveInstructorIdByUserId(UUID userId) {
        return jdbc.queryForObject("""
            SELECT id
            FROM instructors
            WHERE user_id = ?
        """, Long.class, userId);
    }

    public Long resolveAdministratorIdByUserId(UUID userId) {
        return jdbc.queryForObject("""
            SELECT id
            FROM administrators
            WHERE user_id = ?
        """, Long.class, userId);
    }

    public Long countStudentTasks(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentCompletedTasks(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND status = 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentActiveTasks(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND status <> 'COMPLETED'
              AND COALESCE(is_deleted, false) = false
              AND COALESCE(archived, false) = false
        """, studentId);
    }

    public Long countStudentOverdueTasks(Long studentId) {
        return count("""
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
        return count("""
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
        return count("""
            SELECT COUNT(*)
            FROM tasks
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public NearestTaskRow findNearestStudentTask(Long studentId) {
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
    }

    public Long countStudentNotes(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentPinnedNotes(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_pinned, false) = true
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentImportantNotes(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_important, false) = true
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentArchivedNotes(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND COALESCE(is_archived, false) = true
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentNotesAddedThisWeek(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM notes
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public LastEditedNoteRow findLastEditedStudentNote(Long studentId) {
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
    }

    public Boolean hasActiveSchedule(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM student_schedules
            WHERE student_id = ?
              AND status = 'ACTIVE'
        """, studentId) > 0;
    }

    public String findActiveScheduleName(Long studentId) {
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
    }

    public Long countActiveScheduleCourses(Long studentId) {
        return count("""
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
        return count("""
            SELECT COUNT(*)
            FROM schedule_entries se
            JOIN student_schedules ss ON ss.id = se.schedule_id
            WHERE ss.student_id = ?
              AND ss.status = 'ACTIVE'
              AND DATE(se.start_date_time) = CURRENT_DATE
        """, studentId);
    }

    public Long countActiveScheduleDaysOff(Long studentId) {
        Long activeDays = count("""
            SELECT COUNT(DISTINCT EXTRACT(DOW FROM se.start_date_time))
            FROM schedule_entries se
            JOIN student_schedules ss ON ss.id = se.schedule_id
            WHERE ss.student_id = ?
              AND ss.status = 'ACTIVE'
        """, studentId);

        return Math.max(0L, 7L - activeDays);
    }

    public Long countStudentGradeCalculations(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM grade_calculations
            WHERE student_id = ?
        """, studentId);
    }

    public Double findStudentLatestGpa(Long studentId) {
        List<Double> rows = jdbc.query("""
            SELECT calculated_gpa
            FROM grade_calculations
            WHERE student_id = ?
            ORDER BY updated_at DESC NULLS LAST, created_at DESC
            LIMIT 1
        """, (rs, rowNum) -> rs.getDouble("calculated_gpa"), studentId);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public Double findStudentLatestPercentage(Long studentId) {
        List<Double> rows = jdbc.query("""
            SELECT calculated_percentage
            FROM grade_calculations
            WHERE student_id = ?
            ORDER BY updated_at DESC NULLS LAST, created_at DESC
            LIMIT 1
        """, (rs, rowNum) -> rs.getDouble("calculated_percentage"), studentId);

        return rows.isEmpty() ? null : rows.get(0);
    }

    public Long countStudentLibraryFolders(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM personal_resource_folders
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentLibraryItems(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM personal_resource_items
            WHERE student_id = ?
              AND COALESCE(is_deleted, false) = false
        """, studentId);
    }

    public Long countStudentCopiedFromPublic(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM resource_share_copy_logs
            WHERE student_id = ?
        """, studentId);
    }

    public Long countStudentUpcomingReminders(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM task_reminders tr
            JOIN tasks t ON t.id = tr.task_id
            WHERE t.student_id = ?
              AND tr.enabled = true
              AND tr.sent = false
              AND tr.remind_at >= CURRENT_TIMESTAMP
              AND tr.remind_at < CURRENT_TIMESTAMP + INTERVAL '7 days'
              AND t.status <> 'COMPLETED'
              AND COALESCE(t.is_deleted, false) = false
              AND COALESCE(t.archived, false) = false
        """, studentId);
    }

    public Long countStudentRegisteredEvents(Long studentId) {
        return count("""
            SELECT COUNT(*)
            FROM event_registrations
            WHERE student_id = ?
              AND COALESCE(status, 'REGISTERED') <> 'CANCELLED'
        """, studentId);
    }

    public NearestEventRow findNearestStudentEvent(Long studentId) {
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
    }

    public Long countInstructorResources(Long instructorId) {
        return 0L;
    }

    public Long countInstructorResourcesByStatus(Long instructorId, String status) {
        return 0L;
    }

    public Long countInstructorEvents(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
        """, instructorId);
    }

    public Long countInstructorActiveEvents(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND e.start_date_time <= CURRENT_TIMESTAMP
              AND e.end_date_time >= CURRENT_TIMESTAMP
        """, instructorId);
    }

    public Long countInstructorEndedEvents(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND e.end_date_time < CURRENT_TIMESTAMP
        """, instructorId);
    }

    public Long countInstructorUpcomingEvents(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM events e
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND e.start_date_time > CURRENT_TIMESTAMP
        """, instructorId);
    }

    public Long countInstructorOpportunities(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM opportunities o
            JOIN events e ON e.id = o.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
        """, instructorId);
    }

    public Long countInstructorPaidOpportunities(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM opportunities o
            JOIN events e ON e.id = o.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND COALESCE(o.is_paid, false) = true
        """, instructorId);
    }

    public Long countInstructorEventRegistrations(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM event_registrations er
            JOIN events e ON e.id = er.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND COALESCE(er.status, 'REGISTERED') <> 'CANCELLED'
        """, instructorId);
    }

    public Long countInstructorEventCheckIns(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM event_registrations er
            JOIN events e ON e.id = er.event_id
            JOIN instructors i ON i.user_id = e.created_by_user_id
            WHERE i.id = ?
              AND er.checked_in_at IS NOT NULL
        """, instructorId);
    }

    public Long countInstructorSections(Long instructorId) {
        return count("""
            SELECT COUNT(*)
            FROM course_sections
            WHERE instructor_id = ?
        """, instructorId);
    }

    public Long sumInstructorSectionsCapacity(Long instructorId) {
        return count("""
            SELECT COALESCE(SUM(capacity), 0)
            FROM course_sections
            WHERE instructor_id = ?
        """, instructorId);
    }

    public Long sumInstructorSectionsEnrolled(Long instructorId) {
        return 0L;
    }

    public Long countUsers() {
        return count("SELECT COUNT(*) FROM users");
    }

    public Long countUsersByRole(String role) {
        return count("""
            SELECT COUNT(*)
            FROM users
            WHERE role = ?
        """, role);
    }

    public Long countNewUsersThisMonth() {
        return count("""
            SELECT COUNT(*)
            FROM users
            WHERE created_at >= DATE_TRUNC('month', CURRENT_DATE)
        """);
    }

    public Long countResources() {
        return count("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE COALESCE(is_deleted, false) = false
        """);
    }

    public Long countResourcesByStatus(String status) {
        return count("""
            SELECT COUNT(*)
            FROM admin_resources
            WHERE approval_status = ?
              AND COALESCE(is_deleted, false) = false
        """, status);
    }

    public Long countEvents() {
        return count("SELECT COUNT(*) FROM events");
    }

    public Long countActiveEvents() {
        return count("""
            SELECT COUNT(*)
            FROM events
            WHERE start_date_time <= CURRENT_TIMESTAMP
              AND end_date_time >= CURRENT_TIMESTAMP
        """);
    }

    public Long countEndedEvents() {
        return count("""
            SELECT COUNT(*)
            FROM events
            WHERE end_date_time < CURRENT_TIMESTAMP
        """);
    }

    public Long countUpcomingEvents() {
        return count("""
            SELECT COUNT(*)
            FROM events
            WHERE start_date_time > CURRENT_TIMESTAMP
        """);
    }

    public Long countEventsRequiresRegistration() {
        return count("""
            SELECT COUNT(*)
            FROM events
            WHERE COALESCE(requires_registration, false) = true
        """);
    }

    public Long countAllEventRegistrations() {
        return count("""
            SELECT COUNT(*)
            FROM event_registrations
            WHERE COALESCE(status, 'REGISTERED') <> 'CANCELLED'
        """);
    }

    public Long countAllOpportunities() {
        return count("SELECT COUNT(*) FROM opportunities");
    }

    public Long countPaidOpportunities() {
        return count("""
            SELECT COUNT(*)
            FROM opportunities
            WHERE COALESCE(is_paid, false) = true
        """);
    }

    public Long countNotificationsSentToday() {
        return count("""
            SELECT COUNT(*)
            FROM user_notifications
            WHERE DATE(delivered_at) = CURRENT_DATE
        """);
    }

    public Long countNotificationsSentThisMonth() {
        return count("""
            SELECT COUNT(*)
            FROM user_notifications
            WHERE delivered_at >= DATE_TRUNC('month', CURRENT_DATE)
        """);
    }

    public Long countInstitutions() {
        return count("SELECT COUNT(*) FROM institutions");
    }

    public Long countCourses() {
        return count("SELECT COUNT(*) FROM courses");
    }

    public Long countFaculties() {
        return count("SELECT COUNT(*) FROM faculties");
    }

    public Long countDepartments() {
        return count("SELECT COUNT(*) FROM departments");
    }

    public Long countTerms() {
        return count("SELECT COUNT(*) FROM terms");
    }

    public List<ChartRow> studentTasksByStatus(Long studentId) {
        return List.of(
                new ChartRow("Active", countStudentActiveTasks(studentId)),
                new ChartRow("Completed", countStudentCompletedTasks(studentId)),
                new ChartRow("Overdue", countStudentOverdueTasks(studentId))
        );
    }

    public List<ChartRow> studentNotesByStatus(Long studentId) {
        return List.of(
                new ChartRow("Pinned", countStudentPinnedNotes(studentId)),
                new ChartRow("Important", countStudentImportantNotes(studentId)),
                new ChartRow("Archived", countStudentArchivedNotes(studentId))
        );
    }

    public List<TimeSeriesRow> studentTasksCreatedThisWeek(Long studentId) {
        return jdbc.query("""
            SELECT DATE(created_at) AS date, COUNT(*) AS value
            FROM tasks
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
        """, (rs, rowNum) -> new TimeSeriesRow(
                rs.getDate("date").toLocalDate(),
                rs.getLong("value")
        ), studentId);
    }

    public List<TimeSeriesRow> studentNotesCreatedThisWeek(Long studentId) {
        return jdbc.query("""
            SELECT DATE(created_at) AS date, COUNT(*) AS value
            FROM notes
            WHERE student_id = ?
              AND created_at >= DATE_TRUNC('week', CURRENT_DATE)
              AND COALESCE(is_deleted, false) = false
            GROUP BY DATE(created_at)
            ORDER BY DATE(created_at)
        """, (rs, rowNum) -> new TimeSeriesRow(
                rs.getDate("date").toLocalDate(),
                rs.getLong("value")
        ), studentId);
    }
}