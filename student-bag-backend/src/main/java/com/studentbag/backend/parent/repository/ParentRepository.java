package com.studentbag.backend.parent.repository;

import com.studentbag.backend.parent.entity.Parent;
import com.studentbag.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByUser(User user);
}