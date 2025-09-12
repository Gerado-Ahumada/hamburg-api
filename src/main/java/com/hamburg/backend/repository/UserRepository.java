package com.hamburg.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hamburg.backend.model.ERole;
import com.hamburg.backend.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByUuid(String uuid);
    
    Boolean existsByUsername(String username);
    
    Boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role")
    Page<User> findByRole(@Param("role") ERole role, Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<User> findByRoleAndNameContaining(@Param("role") ERole role, 
                                          @Param("name") String name, 
                                          Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role " +
           "AND LOWER(u.playerCategory) LIKE LOWER(CONCAT('%', :category, '%'))")
    Page<User> findByRoleAndPlayerCategory(@Param("role") ERole role, 
                                          @Param("category") String category, 
                                          Pageable pageable);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :role " +
           "AND (LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND LOWER(u.playerCategory) LIKE LOWER(CONCAT('%', :category, '%'))")
    Page<User> findByRoleAndNameContainingAndPlayerCategory(@Param("role") ERole role, 
                                                           @Param("name") String name, 
                                                           @Param("category") String category, 
                                                           Pageable pageable);
}