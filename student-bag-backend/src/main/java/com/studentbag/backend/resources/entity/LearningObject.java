package com.studentbag.backend.resources.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.AcademicLevel;
import com.studentbag.backend.domain.enums.ContentFormat;
import com.studentbag.backend.domain.enums.LanguageCode;
import com.studentbag.backend.domain.enums.ResourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "learning_objects")
@Getter
@Setter
public class LearningObject extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 1000)
    private String keywords;

    @Enumerated(EnumType.STRING)
    private LanguageCode language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentFormat format;

    private String difficulty;

    private String intendedEndUserRole;

    @Enumerated(EnumType.STRING)
    private AcademicLevel educationalLevel;

    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    private Integer typicalLearningTimeMinutes;

    @Column(length = 1000)
    private String url;

    @Column(length = 1000)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    public boolean search(String text) {
        String query = text == null ? "" : text.toLowerCase();

        return (title != null && title.toLowerCase().contains(query))
                || (description != null && description.toLowerCase().contains(query))
                || (keywords != null && keywords.toLowerCase().contains(query));
    }
}