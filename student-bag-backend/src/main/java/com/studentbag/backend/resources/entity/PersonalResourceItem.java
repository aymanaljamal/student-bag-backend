package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.ContentFormat;
import com.studentbag.backend.notes.entity.Note;
import com.studentbag.backend.student.entity.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "personal_resource_items")
@Getter
@Setter
public class PersonalResourceItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_student_id", nullable = false)
    private Student ownerStudent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private PersonalResourceFolder folder;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentFormat format;

    @Column(nullable = false)
    private Boolean isExamPreparation = false;

    @Column(nullable = false)
    private Boolean isImportant = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_admin_resource_id")
    private AdminResource linkedAdminResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id")
    private Note note;

    @Column(length = 1000)
    private String fileUrl;

    public void markImportant() {
        this.isImportant = true;
    }

    public void moveToFolder(PersonalResourceFolder folder) {
        this.folder = folder;
    }
}