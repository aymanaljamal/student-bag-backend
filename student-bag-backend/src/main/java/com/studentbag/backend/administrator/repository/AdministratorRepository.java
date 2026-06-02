package com.studentbag.backend.administrator.repository;


import com.studentbag.backend.administrator.entity.Administrator;
import com.studentbag.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByUser_Id(UUID userId);
    Optional<Administrator> findByUserId(UUID userId);
    @Query("""
        select a.user
        from Administrator a
        join a.user u
        where a.institution.id = :institutionId
          and u.active = true
    """)
    List<User> findActiveAdminUsersByInstitutionId(@Param("institutionId") Long institutionId);
}