package com.studentbag.backend.instructor.repository;


import com.studentbag.backend.institution.entity.Institution;
import com.studentbag.backend.instructor.entity.Instructor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
public interface InstructorRepository extends JpaRepository<Instructor, Long> {



    Optional<Instructor> findByUser_Id(UUID userId);
    Optional<Instructor> findByUserId(UUID userId);
    Optional<Instructor> findByExternalIdAndInstitution(String externalId, Institution institution);
    Optional<Instructor> findByFullNameArabicAndInstitution(String fullNameArabic, Institution institution);
    @Query("""
            select i
            from Instructor i
            left join fetch i.department d
            left join fetch i.user u
            left join fetch i.institution ins
            where i.id = :id
            """)
    Optional<Instructor> findDetailedById(@Param("id") Long id);

    @Query("""
            select i
            from Instructor i
            left join fetch i.department d
            left join fetch i.user u
            left join fetch i.institution ins
            where lower(u.email) = lower(:email)
            """)
    Optional<Instructor> findDetailedByUserEmail(@Param("email") String email);
}

