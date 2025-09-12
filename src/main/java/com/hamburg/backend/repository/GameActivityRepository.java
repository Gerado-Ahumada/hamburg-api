package com.hamburg.backend.repository;

import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameActivityRepository extends JpaRepository<GameActivity, Long> {
    
    List<GameActivity> findByUser(User user);
    
    List<GameActivity> findByUserOrderByGameDateDesc(User user);
    
    @Query("SELECT ga FROM GameActivity ga WHERE ga.user.uuid = :userUuid ORDER BY ga.gameDate DESC")
    List<GameActivity> findByUserUuidOrderByGameDateDesc(@Param("userUuid") String userUuid);
    
    @Query("SELECT ga FROM GameActivity ga WHERE ga.user.uuid = :userUuid AND ga.gameDate BETWEEN :startDate AND :endDate")
    List<GameActivity> findByUserUuidAndGameDateBetween(
        @Param("userUuid") String userUuid, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT ga FROM GameActivity ga WHERE ga.user.uuid = :userUuid AND YEAR(ga.gameDate) = :year AND MONTH(ga.gameDate) = :month ORDER BY ga.gameDate DESC")
    List<GameActivity> findByUserUuidAndYearAndMonthOrderByGameDateDesc(@Param("userUuid") String userUuid, @Param("year") int year, @Param("month") int month);
}