package com.studentbag.backend.notes.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "note_attachments")
@Getter
@Setter
public class NoteAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(nullable = false)
    private String fileName;

    private Integer durationSeconds;

    private Long fileSizeBytes;

    public String getUrlValue() {
        return url;
    }

    public String getTypeValue() {
        return type;
    }
}