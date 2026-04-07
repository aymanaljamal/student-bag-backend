package com.studentbag.backend.institution.entity;

import com.studentbag.backend.common.entity.BaseEntity;
import com.studentbag.backend.domain.enums.InstitutionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "institutions")
@Getter
@Setter
public class Institution extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstitutionType type;

    private String country;
    private String city;
    private String website;

    @Column(nullable = false)
    private Boolean active = true;
}