package com.studentbag.backend.administrator.repository;


import com.studentbag.backend.administrator.entity.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {
}