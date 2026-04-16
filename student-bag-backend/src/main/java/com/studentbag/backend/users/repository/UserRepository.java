package com.studentbag.backend.users.repository;

import com.studentbag.backend.domain.enums.UserRole;
import com.studentbag.backend.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAllByRole(UserRole role);
    List<User> findAllByIdIn(Collection<UUID> ids);


}
