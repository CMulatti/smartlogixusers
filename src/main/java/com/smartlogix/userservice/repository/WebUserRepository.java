package com.smartlogix.userservice.repository;

import com.smartlogix.userservice.entity.WebUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WebUserRepository extends JpaRepository<WebUser, Long> {
    Optional<WebUser> findByUsername(String username);
}