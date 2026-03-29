package com.studentbag.backend.administrator.repository;


import com.studentbag.backend.administrator.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
    Optional<Administrator> findByUser_Id(UUID userId);
}