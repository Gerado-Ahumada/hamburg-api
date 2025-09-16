package com.hamburg.backend.repository;

import com.hamburg.backend.entity.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BenefitRepository extends JpaRepository<Benefit, Long> {

    /**
     * Encuentra todos los beneficios activos ordenados por juegos requeridos
     */
    List<Benefit> findByActiveTrueOrderByRequiredGamesAsc();

    /**
     * Encuentra un beneficio por la cantidad exacta de juegos requeridos
     */
    Optional<Benefit> findByRequiredGamesAndActiveTrue(Integer requiredGames);

    /**
     * Encuentra el próximo beneficio disponible basado en la cantidad de juegos del usuario
     */
    @Query("SELECT b FROM Benefit b WHERE b.requiredGames > :currentGames AND b.active = true ORDER BY b.requiredGames ASC LIMIT 1")
    Optional<Benefit> findNextBenefitByGames(@Param("currentGames") Integer currentGames);

    /**
     * Encuentra todos los beneficios que el usuario debería tener basado en sus juegos
     */
    @Query("SELECT b FROM Benefit b WHERE b.requiredGames <= :totalGames AND b.active = true ORDER BY b.requiredGames ASC")
    List<Benefit> findEligibleBenefits(@Param("totalGames") Integer totalGames);

    /**
     * Verifica si existe un beneficio para una cantidad específica de juegos
     */
    boolean existsByRequiredGamesAndActiveTrue(Integer requiredGames);
}