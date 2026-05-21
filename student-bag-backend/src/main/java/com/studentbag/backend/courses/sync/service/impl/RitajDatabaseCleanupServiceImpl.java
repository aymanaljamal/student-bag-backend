package com.studentbag.backend.courses.sync.service.impl;

import com.studentbag.backend.courses.sync.service.RitajDatabaseCleanupService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RitajDatabaseCleanupServiceImpl implements RitajDatabaseCleanupService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void clearInstitutionCourseData(Long institutionId) {
        if (institutionId == null) {
            throw new IllegalArgumentException("institutionId is required");
        }

        log.warn("🧹 [Cleanup] بدء حذف بيانات الكورسات القديمة للمؤسسة رقم: {}", institutionId);

        int totalDeleted = 0;

        // ─────────────────────────────────────────────
        // 1) Schedule related by course / section
        // ─────────────────────────────────────────────
        totalDeleted += deleteBySectionId("schedule_entries", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("schedule_entries", "course_id", institutionId);

        totalDeleted += deleteBySectionId("student_schedule_entries", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("student_schedule_entries", "course_id", institutionId);

        totalDeleted += deleteBySectionId("schedule_options_entries", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("schedule_options_entries", "course_id", institutionId);

        totalDeleted += deleteBySectionId("generated_schedule_entries", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("generated_schedule_entries", "course_id", institutionId);

        // ─────────────────────────────────────────────
        // 2) Grades related
        // ─────────────────────────────────────────────
        totalDeleted += deleteBySectionId("grade_course_items", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("grade_course_items", "course_id", institutionId);

        totalDeleted += deleteBySectionId("grade_items", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("grade_items", "course_id", institutionId);

        totalDeleted += deleteBySectionId("grade_calculation_items", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("grade_calculation_items", "course_id", institutionId);

        totalDeleted += deleteBySectionId("course_grade_items", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("course_grade_items", "course_id", institutionId);

        // ─────────────────────────────────────────────
        // 3) Tasks related
        // ─────────────────────────────────────────────
        totalDeleted += deleteTaskChildrenBySectionId("task_reminders", "task_id", "tasks", "course_section_id", institutionId);
        totalDeleted += deleteTaskChildrenByCourseId("task_reminders", "task_id", "tasks", "course_id", institutionId);

        totalDeleted += deleteTaskChildrenBySectionId("task_attachments", "task_id", "tasks", "course_section_id", institutionId);
        totalDeleted += deleteTaskChildrenByCourseId("task_attachments", "task_id", "tasks", "course_id", institutionId);

        totalDeleted += deleteTaskChildrenBySectionId("subtasks", "task_id", "tasks", "course_section_id", institutionId);
        totalDeleted += deleteTaskChildrenByCourseId("subtasks", "task_id", "tasks", "course_id", institutionId);

        totalDeleted += deleteTaskChildrenBySectionId("task_label_links", "task_id", "tasks", "course_section_id", institutionId);
        totalDeleted += deleteTaskChildrenByCourseId("task_label_links", "task_id", "tasks", "course_id", institutionId);

        totalDeleted += deleteBySectionId("tasks", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("tasks", "course_id", institutionId);

        totalDeleted += deleteBySectionId("student_tasks", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("student_tasks", "course_id", institutionId);

        // ─────────────────────────────────────────────
        // 4) Notes related
        // ─────────────────────────────────────────────
        totalDeleted += deleteNoteChildrenBySectionId("note_attachments", "note_id", "notes", "course_section_id", institutionId);
        totalDeleted += deleteNoteChildrenByCourseId("note_attachments", "note_id", "notes", "course_id", institutionId);

        totalDeleted += deleteNoteChildrenBySectionId("note_tags", "note_id", "notes", "course_section_id", institutionId);
        totalDeleted += deleteNoteChildrenByCourseId("note_tags", "note_id", "notes", "course_id", institutionId);

        totalDeleted += deleteNoteChildrenBySectionId("note_checklist_items", "note_id", "notes", "course_section_id", institutionId);
        totalDeleted += deleteNoteChildrenByCourseId("note_checklist_items", "note_id", "notes", "course_id", institutionId);

        totalDeleted += deleteBySectionId("notes", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("notes", "course_id", institutionId);

        totalDeleted += deleteBySectionId("student_notes", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("student_notes", "course_id", institutionId);

        // ─────────────────────────────────────────────
        // 5) Resource Hub related
        // ─────────────────────────────────────────────
        totalDeleted += deleteAdminResourceChildren(institutionId);

        totalDeleted += deleteResourceAttachmentsBySectionId(institutionId);
        totalDeleted += deleteResourceAttachmentsByCourseId(institutionId);

        totalDeleted += deleteResourceFolderChildrenBySectionId(institutionId);
        totalDeleted += deleteResourceFolderChildrenByCourseId(institutionId);

        totalDeleted += deleteResourceItemsBySectionId(institutionId);
        totalDeleted += deleteResourceItemsByCourseId(institutionId);

        totalDeleted += deleteBySectionId("resource_items", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("resource_items", "course_id", institutionId);

        totalDeleted += deleteBySectionId("personal_resource_items", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("personal_resource_items", "course_id", institutionId);

        totalDeleted += deleteBySectionId("public_resources", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("public_resources", "course_id", institutionId);

        totalDeleted += deleteBySectionId("admin_resources", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("admin_resources", "course_id", institutionId);

        detachResourceFolderChildren(institutionId);
        totalDeleted += deleteBySectionId("resource_folders", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("resource_folders", "course_id", institutionId);

        detachPersonalResourceFolderChildren(institutionId);
        totalDeleted += deleteBySectionId("personal_resource_folders", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("personal_resource_folders", "course_id", institutionId);

        // ─────────────────────────────────────────────
        // 6) Other course-section / course dependents
        // ─────────────────────────────────────────────
        totalDeleted += deleteBySectionId("attendance_records", "course_section_id", institutionId);

        totalDeleted += deleteBySectionId("course_registrations", "course_section_id", institutionId);
        totalDeleted += deleteByCourseId("course_registrations", "course_id", institutionId);

        totalDeleted += deleteBySectionId("student_course_sections", "course_section_id", institutionId);
        totalDeleted += deleteBySectionId("section_students", "course_section_id", institutionId);

        totalDeleted += deleteByCourseId("student_courses", "course_id", institutionId);

        totalDeleted += deleteByCourseId("course_ratings", "course_id", institutionId);

        // حذف المدرسين + users اللي role تبعهم INSTRUCTOR قبل حذف departments
        totalDeleted += deleteInstructorsAndInstructorUsers(institutionId);

        // مهم جدًا: حذف student_schedules قبل حذف terms
        totalDeleted += deleteStudentSchedulesByTerm(institutionId);

        // ─────────────────────────────────────────────
        // 7) Core course data
        // ─────────────────────────────────────────────
        int sessionsDeleted = executeSafe("""
                DELETE FROM class_sessions
                WHERE course_section_id IN (
                    SELECT cs.id
                    FROM course_sections cs
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """, institutionId, "class_sessions");

        int sectionsDeleted = executeSafe("""
                DELETE FROM course_sections
                WHERE course_id IN (
                    SELECT id
                    FROM courses
                    WHERE institution_id = ?1
                )
                """, institutionId, "course_sections");

        int coursesDeleted = executeSafe("""
                DELETE FROM courses
                WHERE institution_id = ?1
                """, institutionId, "courses");

        int departmentsDeleted = executeSafe("""
                DELETE FROM departments
                WHERE faculty_id IN (
                    SELECT id
                    FROM faculties
                    WHERE institution_id = ?1
                )
                """, institutionId, "departments");

        int facultiesDeleted = executeSafe("""
                DELETE FROM faculties
                WHERE institution_id = ?1
                """, institutionId, "faculties");

        int termsDeleted = executeSafe("""
                DELETE FROM terms
                WHERE institution_id = ?1
                """, institutionId, "terms");

        totalDeleted += sessionsDeleted;
        totalDeleted += sectionsDeleted;
        totalDeleted += coursesDeleted;
        totalDeleted += departmentsDeleted;
        totalDeleted += facultiesDeleted;
        totalDeleted += termsDeleted;

        log.warn("""
                ✅ [Cleanup] تم حذف بيانات المؤسسة رقم: {}
                - sessions: {}
                - sections: {}
                - courses: {}
                - departments: {}
                - faculties: {}
                - terms: {}
                - totalDeleted: {}
                """,
                institutionId,
                sessionsDeleted,
                sectionsDeleted,
                coursesDeleted,
                departmentsDeleted,
                facultiesDeleted,
                termsDeleted,
                totalDeleted
        );
    }

    private int deleteBySectionId(String tableName, String columnName, Long institutionId) {
        if (!hasColumn(tableName, columnName)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT cs.id
                    FROM course_sections cs
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(tableName, columnName), institutionId, tableName);
    }

    private int deleteByCourseId(String tableName, String columnName, Long institutionId) {
        if (!hasColumn(tableName, columnName)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT id
                    FROM courses
                    WHERE institution_id = ?1
                )
                """.formatted(tableName, columnName), institutionId, tableName);
    }

    private int executeSafe(String sql, Long institutionId, String tableName) {
        int deleted = entityManager.createNativeQuery(sql)
                .setParameter(1, institutionId)
                .executeUpdate();

        if (deleted > 0) {
            log.warn("🧹 [Cleanup] {} rows deleted from {}", deleted, tableName);
        }

        return deleted;
    }

    private boolean hasColumn(String tableName, String columnName) {
        try {
            Number count = (Number) entityManager.createNativeQuery("""
                    SELECT COUNT(*)
                    FROM information_schema.columns
                    WHERE table_schema = 'public'
                      AND table_name = ?1
                      AND column_name = ?2
                    """)
                    .setParameter(1, tableName)
                    .setParameter(2, columnName)
                    .getSingleResult();

            return count != null && count.longValue() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private int deleteTaskChildrenBySectionId(
            String childTable,
            String childTaskColumn,
            String taskTable,
            String taskSectionColumn,
            Long institutionId
    ) {
        if (!hasColumn(childTable, childTaskColumn) || !hasColumn(taskTable, taskSectionColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT t.id
                    FROM %s t
                    JOIN course_sections cs ON cs.id = t.%s
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(childTable, childTaskColumn, taskTable, taskSectionColumn), institutionId, childTable);
    }

    private int deleteTaskChildrenByCourseId(
            String childTable,
            String childTaskColumn,
            String taskTable,
            String taskCourseColumn,
            Long institutionId
    ) {
        if (!hasColumn(childTable, childTaskColumn) || !hasColumn(taskTable, taskCourseColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT t.id
                    FROM %s t
                    JOIN courses c ON c.id = t.%s
                    WHERE c.institution_id = ?1
                )
                """.formatted(childTable, childTaskColumn, taskTable, taskCourseColumn), institutionId, childTable);
    }

    private int deleteNoteChildrenBySectionId(
            String childTable,
            String childNoteColumn,
            String noteTable,
            String noteSectionColumn,
            Long institutionId
    ) {
        if (!hasColumn(childTable, childNoteColumn) || !hasColumn(noteTable, noteSectionColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT n.id
                    FROM %s n
                    JOIN course_sections cs ON cs.id = n.%s
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(childTable, childNoteColumn, noteTable, noteSectionColumn), institutionId, childTable);
    }

    private int deleteNoteChildrenByCourseId(
            String childTable,
            String childNoteColumn,
            String noteTable,
            String noteCourseColumn,
            Long institutionId
    ) {
        if (!hasColumn(childTable, childNoteColumn) || !hasColumn(noteTable, noteCourseColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT n.id
                    FROM %s n
                    JOIN courses c ON c.id = n.%s
                    WHERE c.institution_id = ?1
                )
                """.formatted(childTable, childNoteColumn, noteTable, noteCourseColumn), institutionId, childTable);
    }

    private int deleteAdminResourceChildren(Long institutionId) {
        int deleted = 0;

        deleted += deleteChildByAdminResourceSectionId("resource_approval_actions", "resource_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("resource_approval_actions", "resource_id", institutionId);

        deleted += deleteChildByAdminResourceSectionId("resource_approval_actions", "admin_resource_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("resource_approval_actions", "admin_resource_id", institutionId);

        deleted += deleteChildByAdminResourceSectionId("resource_approval_actions", "admin_resources_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("resource_approval_actions", "admin_resources_id", institutionId);

        deleted += deleteChildByAdminResourceSectionId("resource_attachments", "resource_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("resource_attachments", "resource_id", institutionId);

        deleted += deleteChildByAdminResourceSectionId("resource_attachments", "admin_resource_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("resource_attachments", "admin_resource_id", institutionId);

        deleted += deleteChildByAdminResourceSectionId("admin_resource_attachments", "resource_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("admin_resource_attachments", "resource_id", institutionId);

        deleted += deleteChildByAdminResourceSectionId("admin_resource_attachments", "admin_resource_id", institutionId);
        deleted += deleteChildByAdminResourceCourseId("admin_resource_attachments", "admin_resource_id", institutionId);

        return deleted;
    }

    private int deleteChildByAdminResourceSectionId(
            String childTable,
            String adminResourceFkColumn,
            Long institutionId
    ) {
        if (!hasColumn(childTable, adminResourceFkColumn)
                || !hasColumn("admin_resources", "course_section_id")) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT ar.id
                    FROM admin_resources ar
                    JOIN course_sections cs ON cs.id = ar.course_section_id
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(childTable, adminResourceFkColumn), institutionId, childTable);
    }

    private int deleteChildByAdminResourceCourseId(
            String childTable,
            String adminResourceFkColumn,
            Long institutionId
    ) {
        if (!hasColumn(childTable, adminResourceFkColumn)
                || !hasColumn("admin_resources", "course_id")) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT ar.id
                    FROM admin_resources ar
                    JOIN courses c ON c.id = ar.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(childTable, adminResourceFkColumn), institutionId, childTable);
    }

    private int deleteResourceAttachmentsBySectionId(Long institutionId) {
        int deleted = 0;

        deleted += deleteResourceAttachmentByItemSectionId(
                "resource_attachments",
                "resource_item_id",
                "resource_items",
                "course_section_id",
                institutionId
        );

        deleted += deleteResourceAttachmentByItemSectionId(
                "resource_attachments",
                "resource_item_id",
                "personal_resource_items",
                "course_section_id",
                institutionId
        );

        deleted += deleteResourceAttachmentByItemSectionId(
                "personal_resource_attachments",
                "resource_item_id",
                "personal_resource_items",
                "course_section_id",
                institutionId
        );

        return deleted;
    }

    private int deleteResourceAttachmentsByCourseId(Long institutionId) {
        int deleted = 0;

        deleted += deleteResourceAttachmentByItemCourseId(
                "resource_attachments",
                "resource_item_id",
                "resource_items",
                "course_id",
                institutionId
        );

        deleted += deleteResourceAttachmentByItemCourseId(
                "resource_attachments",
                "resource_item_id",
                "personal_resource_items",
                "course_id",
                institutionId
        );

        deleted += deleteResourceAttachmentByItemCourseId(
                "personal_resource_attachments",
                "resource_item_id",
                "personal_resource_items",
                "course_id",
                institutionId
        );

        return deleted;
    }

    private int deleteResourceAttachmentByItemSectionId(
            String attachmentTable,
            String attachmentItemColumn,
            String itemTable,
            String itemSectionColumn,
            Long institutionId
    ) {
        if (!hasColumn(attachmentTable, attachmentItemColumn) || !hasColumn(itemTable, itemSectionColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT ri.id
                    FROM %s ri
                    JOIN course_sections cs ON cs.id = ri.%s
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(attachmentTable, attachmentItemColumn, itemTable, itemSectionColumn), institutionId, attachmentTable);
    }

    private int deleteResourceAttachmentByItemCourseId(
            String attachmentTable,
            String attachmentItemColumn,
            String itemTable,
            String itemCourseColumn,
            Long institutionId
    ) {
        if (!hasColumn(attachmentTable, attachmentItemColumn) || !hasColumn(itemTable, itemCourseColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT ri.id
                    FROM %s ri
                    JOIN courses c ON c.id = ri.%s
                    WHERE c.institution_id = ?1
                )
                """.formatted(attachmentTable, attachmentItemColumn, itemTable, itemCourseColumn), institutionId, attachmentTable);
    }

    private int deleteResourceItemsBySectionId(Long institutionId) {
        int deleted = 0;

        deleted += deleteBySectionId("resource_items", "course_section_id", institutionId);
        deleted += deleteBySectionId("personal_resource_items", "course_section_id", institutionId);
        deleted += deleteBySectionId("admin_resources", "course_section_id", institutionId);

        return deleted;
    }

    private int deleteResourceItemsByCourseId(Long institutionId) {
        int deleted = 0;

        deleted += deleteByCourseId("resource_items", "course_id", institutionId);
        deleted += deleteByCourseId("personal_resource_items", "course_id", institutionId);
        deleted += deleteByCourseId("admin_resources", "course_id", institutionId);

        return deleted;
    }

    private int deleteResourceFolderChildrenBySectionId(Long institutionId) {
        int deleted = 0;

        deleted += deleteResourceItemByFolderSectionId(
                "resource_items",
                "folder_id",
                "resource_folders",
                "course_section_id",
                institutionId
        );

        deleted += deleteResourceItemByFolderSectionId(
                "personal_resource_items",
                "folder_id",
                "personal_resource_folders",
                "course_section_id",
                institutionId
        );

        return deleted;
    }

    private int deleteResourceFolderChildrenByCourseId(Long institutionId) {
        int deleted = 0;

        deleted += deleteResourceItemByFolderCourseId(
                "resource_items",
                "folder_id",
                "resource_folders",
                "course_id",
                institutionId
        );

        deleted += deleteResourceItemByFolderCourseId(
                "personal_resource_items",
                "folder_id",
                "personal_resource_folders",
                "course_id",
                institutionId
        );

        return deleted;
    }

    private int deleteResourceItemByFolderSectionId(
            String itemTable,
            String itemFolderColumn,
            String folderTable,
            String folderSectionColumn,
            Long institutionId
    ) {
        if (!hasColumn(itemTable, itemFolderColumn) || !hasColumn(folderTable, folderSectionColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT f.id
                    FROM %s f
                    JOIN course_sections cs ON cs.id = f.%s
                    JOIN courses c ON c.id = cs.course_id
                    WHERE c.institution_id = ?1
                )
                """.formatted(itemTable, itemFolderColumn, folderTable, folderSectionColumn), institutionId, itemTable);
    }

    private int deleteResourceItemByFolderCourseId(
            String itemTable,
            String itemFolderColumn,
            String folderTable,
            String folderCourseColumn,
            Long institutionId
    ) {
        if (!hasColumn(itemTable, itemFolderColumn) || !hasColumn(folderTable, folderCourseColumn)) {
            return 0;
        }

        return executeSafe("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT f.id
                    FROM %s f
                    JOIN courses c ON c.id = f.%s
                    WHERE c.institution_id = ?1
                )
                """.formatted(itemTable, itemFolderColumn, folderTable, folderCourseColumn), institutionId, itemTable);
    }

    private void detachResourceFolderChildren(Long institutionId) {
        if (!hasColumn("resource_folders", "parent_folder_id")) {
            return;
        }

        if (hasColumn("resource_folders", "course_id")) {
            int updated = entityManager.createNativeQuery("""
                    UPDATE resource_folders
                    SET parent_folder_id = NULL
                    WHERE parent_folder_id IN (
                        SELECT id
                        FROM resource_folders
                        WHERE course_id IN (
                            SELECT id
                            FROM courses
                            WHERE institution_id = ?1
                        )
                    )
                    """)
                    .setParameter(1, institutionId)
                    .executeUpdate();

            if (updated > 0) {
                log.warn("🧹 [Cleanup] detached {} resource_folders children by course", updated);
            }
        }

        if (hasColumn("resource_folders", "course_section_id")) {
            int updated = entityManager.createNativeQuery("""
                    UPDATE resource_folders
                    SET parent_folder_id = NULL
                    WHERE parent_folder_id IN (
                        SELECT rf.id
                        FROM resource_folders rf
                        JOIN course_sections cs ON cs.id = rf.course_section_id
                        JOIN courses c ON c.id = cs.course_id
                        WHERE c.institution_id = ?1
                    )
                    """)
                    .setParameter(1, institutionId)
                    .executeUpdate();

            if (updated > 0) {
                log.warn("🧹 [Cleanup] detached {} resource_folders children by section", updated);
            }
        }
    }

    private void detachPersonalResourceFolderChildren(Long institutionId) {
        if (!hasColumn("personal_resource_folders", "parent_folder_id")) {
            return;
        }

        if (hasColumn("personal_resource_folders", "course_id")) {
            int updated = entityManager.createNativeQuery("""
                    UPDATE personal_resource_folders
                    SET parent_folder_id = NULL
                    WHERE parent_folder_id IN (
                        SELECT id
                        FROM personal_resource_folders
                        WHERE course_id IN (
                            SELECT id
                            FROM courses
                            WHERE institution_id = ?1
                        )
                    )
                    """)
                    .setParameter(1, institutionId)
                    .executeUpdate();

            if (updated > 0) {
                log.warn("🧹 [Cleanup] detached {} personal_resource_folders children by course", updated);
            }
        }

        if (hasColumn("personal_resource_folders", "course_section_id")) {
            int updated = entityManager.createNativeQuery("""
                    UPDATE personal_resource_folders
                    SET parent_folder_id = NULL
                    WHERE parent_folder_id IN (
                        SELECT prf.id
                        FROM personal_resource_folders prf
                        JOIN course_sections cs ON cs.id = prf.course_section_id
                        JOIN courses c ON c.id = cs.course_id
                        WHERE c.institution_id = ?1
                    )
                    """)
                    .setParameter(1, institutionId)
                    .executeUpdate();

            if (updated > 0) {
                log.warn("🧹 [Cleanup] detached {} personal_resource_folders children by section", updated);
            }
        }
    }
    private int deleteStudentSchedulesByTerm(Long institutionId) {
        if (!hasColumn("student_schedules", "term_id")) {
            return 0;
        }

        int total = 0;

        if (hasColumn("schedule_entries", "schedule_id")) {
            total += executeSafe("""
                DELETE FROM schedule_entries
                WHERE schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "schedule_entries");
        }

        if (hasColumn("student_schedule_entries", "schedule_id")) {
            total += executeSafe("""
                DELETE FROM student_schedule_entries
                WHERE schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "student_schedule_entries");
        }

        if (hasColumn("generated_schedule_entries", "schedule_id")) {
            total += executeSafe("""
                DELETE FROM generated_schedule_entries
                WHERE schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "generated_schedule_entries");
        }

        if (hasColumn("schedule_options_entries", "schedule_id")) {
            total += executeSafe("""
                DELETE FROM schedule_options_entries
                WHERE schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "schedule_options_entries");
        }

        // مهم: grade_calculations ماسكة student_schedules من خلال source_schedule_id
        if (hasColumn("grade_calculations", "source_schedule_id")) {
            total += executeSafe("""
                DELETE FROM grade_calculations
                WHERE source_schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "grade_calculations");
        }

        // احتياط لو عندك أعمدة ثانية في بيئات مختلفة
        if (hasColumn("grade_calculations", "schedule_id")) {
            total += executeSafe("""
                DELETE FROM grade_calculations
                WHERE schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "grade_calculations");
        }

        if (hasColumn("grade_calculations", "student_schedule_id")) {
            total += executeSafe("""
                DELETE FROM grade_calculations
                WHERE student_schedule_id IN (
                    SELECT ss.id
                    FROM student_schedules ss
                    JOIN terms t ON t.id = ss.term_id
                    WHERE t.institution_id = ?1
                )
                """, institutionId, "grade_calculations");
        }

        total += executeSafe("""
            DELETE FROM student_schedules
            WHERE term_id IN (
                SELECT id
                FROM terms
                WHERE institution_id = ?1
            )
            """, institutionId, "student_schedules");

        return total;
    }
    private int deleteInstructorsAndInstructorUsers(Long institutionId) {
        if (!hasColumn("instructors", "institution_id")) {
            return 0;
        }

        int total = 0;

        if (hasColumn("course_sections", "instructor_id")) {
            total += executeSafe("""
                    UPDATE course_sections
                    SET instructor_id = NULL
                    WHERE instructor_id IN (
                        SELECT id
                        FROM instructors
                        WHERE institution_id = ?1
                    )
                    """, institutionId, "course_sections.instructor_id");
        }

        entityManager.createNativeQuery("""
                DROP TABLE IF EXISTS tmp_ritaj_instructor_users
                """).executeUpdate();

        entityManager.createNativeQuery("""
                CREATE TEMP TABLE tmp_ritaj_instructor_users AS
                SELECT user_id
                FROM instructors
                WHERE institution_id = ?1
                  AND user_id IS NOT NULL
                """)
                .setParameter(1, institutionId)
                .executeUpdate();

        int instructorsDeleted = executeSafe("""
                DELETE FROM instructors
                WHERE institution_id = ?1
                """, institutionId, "instructors");

        total += instructorsDeleted;

        total += deleteInstructorUserDependents();

        if (hasColumn("users", "role")) {
            int usersDeleted = entityManager.createNativeQuery("""
                    DELETE FROM users
                    WHERE id IN (
                        SELECT user_id
                        FROM tmp_ritaj_instructor_users
                    )
                    AND role = 'INSTRUCTOR'
                    """)
                    .executeUpdate();

            total += usersDeleted;

            if (usersDeleted > 0) {
                log.warn("🧹 [Cleanup] {} instructor users deleted", usersDeleted);
            }
        }

        entityManager.createNativeQuery("""
                DROP TABLE IF EXISTS tmp_ritaj_instructor_users
                """).executeUpdate();

        log.warn("🧹 [Cleanup] {} instructors deleted", instructorsDeleted);

        return total;
    }

    private int deleteInstructorUserDependents() {
        int total = 0;

        total += detachEventsCreatedByInstructorUsers();

        total += deleteByTempUserIds("user_notifications", "user_id");
        total += deleteByTempUserIds("notification_recipients", "user_id");
        total += deleteByTempUserIds("notification_attachments", "user_id");

        total += deleteByTempUserIds("fcm_tokens", "user_id");
        total += deleteByTempUserIds("user_devices", "user_id");
        total += deleteByTempUserIds("refresh_tokens", "user_id");

        total += deleteByTempUserIds("password_reset_tokens", "user_id");
        total += deleteByTempUserIds("email_verification_tokens", "user_id");

        return total;
    }

    private int detachEventsCreatedByInstructorUsers() {
        if (!hasColumn("events", "created_by_user_id")) {
            return 0;
        }

        int updated = entityManager.createNativeQuery("""
                UPDATE events
                SET created_by_user_id = NULL
                WHERE created_by_user_id IN (
                    SELECT user_id
                    FROM tmp_ritaj_instructor_users
                )
                """)
                .executeUpdate();

        if (updated > 0) {
            log.warn("🧹 [Cleanup] detached {} events from instructor users", updated);
        }

        return updated;
    }

    private int deleteByTempUserIds(String tableName, String userColumnName) {
        if (!hasColumn(tableName, userColumnName)) {
            return 0;
        }

        int deleted = entityManager.createNativeQuery("""
                DELETE FROM %s
                WHERE %s IN (
                    SELECT user_id
                    FROM tmp_ritaj_instructor_users
                )
                """.formatted(tableName, userColumnName))
                .executeUpdate();

        if (deleted > 0) {
            log.warn("🧹 [Cleanup] {} rows deleted from {}", deleted, tableName);
        }

        return deleted;
    }
}