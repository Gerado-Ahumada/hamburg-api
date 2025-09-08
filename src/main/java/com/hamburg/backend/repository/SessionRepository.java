package com.hamburg.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hamburg.backend.model.Session;
import com.hamburg.backend.model.User;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    
    Optional<Session> findByTokenAndActiveTrue(String token);
    
    List<Session> findByUserAndActiveTrue(User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE Session s SET s.active = false WHERE s.user = :user")
    void deactivateSessionsByUser(@Param("user") User user);
    
    @Modifying
    @Transactional
    @Query("UPDATE Session s SET s.active = false WHERE s.expirationDate < :currentDate")
    void deactivateExpiredSessions(@Param("currentDate") LocalDateTime currentDate);
    
    @Query("SELECT COUNT(s) FROM Session s WHERE s.user = :user AND s.active = true")
    Long countActiveSessionsByUser(@Param("user") User user);
    
    Boolean existsByTokenAndActiveTrue(String token);
}