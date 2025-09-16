package com.hamburg.backend.repository;

import com.hamburg.backend.entity.UserBenefit;
import com.hamburg.backend.entity.UserBenefit.BenefitStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserBenefitRepository extends JpaRepository<UserBenefit, Long> {

    /**
     * Encuentra todos los beneficios de un usuario por estado
     */
    @Query("SELECT ub FROM UserBenefit ub JOIN FETCH ub.benefit WHERE ub.user.uuid = :userUuid AND ub.status = :status ORDER BY ub.createdAt DESC")
    List<UserBenefit> findByUserUuidAndStatus(@Param("userUuid") String userUuid, @Param("status") BenefitStatus status);

    /**
     * Encuentra todos los beneficios activos de un usuario
     */
    @Query("SELECT ub FROM UserBenefit ub JOIN FETCH ub.benefit WHERE ub.user.uuid = :userUuid AND ub.status = 'ACTIVE' ORDER BY ub.benefit.requiredGames ASC")
    List<UserBenefit> findActiveByUserUuid(@Param("userUuid") String userUuid);

    /**
     * Encuentra todos los beneficios de un usuario (cualquier estado)
     */
    @Query("SELECT ub FROM UserBenefit ub JOIN FETCH ub.benefit WHERE ub.user.uuid = :userUuid ORDER BY ub.createdAt DESC")
    List<UserBenefit> findAllByUserUuid(@Param("userUuid") String userUuid);

    /**
     * Verifica si un usuario ya tiene un beneficio específico
     */
    @Query("SELECT ub FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.benefit.id = :benefitId")
    Optional<UserBenefit> findByUserUuidAndBenefitId(@Param("userUuid") String userUuid, @Param("benefitId") Long benefitId);

    /**
     * Verifica si un usuario ya tiene un beneficio para una cantidad específica de juegos
     */
    @Query("SELECT ub FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.benefit.requiredGames = :requiredGames")
    Optional<UserBenefit> findByUserUuidAndRequiredGames(@Param("userUuid") String userUuid, @Param("requiredGames") Integer requiredGames);

    /**
     * Cuenta los beneficios activos de un usuario
     */
    @Query("SELECT COUNT(ub) FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.status = 'ACTIVE'")
    Long countActiveByUserUuid(@Param("userUuid") String userUuid);

    /**
     * Cuenta los beneficios cobrados de un usuario
     */
    @Query("SELECT COUNT(ub) FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.status = 'CLAIMED'")
    Long countClaimedByUserUuid(@Param("userUuid") String userUuid);
    
    /**
     * Encuentra beneficios activos de un usuario creados en el mes actual
     */
    @Query("SELECT ub FROM UserBenefit ub JOIN FETCH ub.benefit WHERE ub.user.uuid = :userUuid AND ub.status = 'ACTIVE' AND ub.createdAt BETWEEN :startOfMonth AND :endOfMonth ORDER BY ub.benefit.requiredGames ASC")
    List<UserBenefit> findActiveByUserUuidAndCurrentMonth(@Param("userUuid") String userUuid, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);
    
    /**
     * Cuenta los beneficios activos de un usuario creados en el mes actual
     */
    @Query("SELECT COUNT(ub) FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.status = 'ACTIVE' AND ub.createdAt BETWEEN :startOfMonth AND :endOfMonth")
    Long countActiveByUserUuidAndCurrentMonth(@Param("userUuid") String userUuid, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);
    
    /**
     * Cuenta los beneficios cobrados de un usuario en el mes actual
     */
    @Query("SELECT COUNT(ub) FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.status = 'CLAIMED' AND ub.claimedAt BETWEEN :startOfMonth AND :endOfMonth")
    Long countClaimedByUserUuidAndCurrentMonth(@Param("userUuid") String userUuid, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);
    
    /**
     * Encuentra un beneficio activo específico de un usuario en el mes actual
     */
    @Query("SELECT ub FROM UserBenefit ub WHERE ub.user.uuid = :userUuid AND ub.benefit.id = :benefitId AND ub.status = 'ACTIVE' AND ub.createdAt BETWEEN :startOfMonth AND :endOfMonth")
    Optional<UserBenefit> findActiveByUserUuidAndBenefitIdAndCurrentMonth(@Param("userUuid") String userUuid, @Param("benefitId") Long benefitId, @Param("startOfMonth") LocalDateTime startOfMonth, @Param("endOfMonth") LocalDateTime endOfMonth);
}