package com.hamburg.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hamburg.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUuid(String uuid);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
}