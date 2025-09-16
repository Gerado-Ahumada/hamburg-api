package com.hamburg.backend.service;

import com.hamburg.backend.dto.BenefitDTO;
import com.hamburg.backend.dto.UserDashboardDTO;
import com.hamburg.backend.entity.Benefit;
import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import com.hamburg.backend.entity.UserBenefit;
import com.hamburg.backend.entity.UserBenefit.BenefitStatus;
import com.hamburg.backend.repository.BenefitRepository;
import com.hamburg.backend.repository.GameActivityRepository;
import com.hamburg.backend.repository.UserBenefitRepository;
import com.hamburg.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BenefitService {

    private final BenefitRepository benefitRepository;
    private final UserBenefitRepository userBenefitRepository;
    private final GameActivityRepository gameActivityRepository;
    private final UserRepository userRepository;

    /**
     * Verifica y activa beneficios automáticamente después de registrar un juego
     * Incluye tanto beneficios basados en total de juegos como en juegos del mes actual
     */
    @Transactional
    public void checkAndActivateBenefits(String userUuid) {
        log.info("Verificando beneficios para usuario: {}", userUuid);
        
        // 1. Verificar beneficios basados en total de juegos (comportamiento original)
        Long totalGames = gameActivityRepository.countByUserUuid(userUuid);
        log.info("Usuario {} tiene {} juegos registrados en total", userUuid, totalGames);
        
        List<Benefit> eligibleBenefits = benefitRepository.findEligibleBenefits(totalGames.intValue());
        
        User user = userRepository.findByUuid(userUuid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userUuid));
        
        for (Benefit benefit : eligibleBenefits) {
            // Verificar si el usuario ya tiene este beneficio (sin filtro de fecha)
            Optional<UserBenefit> existingBenefit = userBenefitRepository
                .findByUserUuidAndBenefitId(userUuid, benefit.getId());
            
            if (existingBenefit.isEmpty()) {
                // Crear nuevo beneficio para el usuario
                UserBenefit userBenefit = new UserBenefit();
                userBenefit.setUser(user);
                userBenefit.setBenefit(benefit);
                userBenefit.setStatus(BenefitStatus.ACTIVE);
                
                userBenefitRepository.save(userBenefit);
                log.info("Beneficio total activado: {} para usuario: {} (total juegos: {})", 
                    benefit.getName(), userUuid, totalGames);
            }
        }
        
        // 2. Verificar beneficios basados en juegos del mes actual (nueva funcionalidad)
        try {
            checkAndActivateMonthlyBenefits(userUuid);
        } catch (Exception e) {
            log.error("Error al verificar beneficios mensuales para usuario: {}", userUuid, e);
            // No fallar el proceso completo por errores en beneficios mensuales
        }
    }

    /**
     * Obtiene todos los beneficios disponibles (activos) para un usuario del mes actual
     */
    public List<BenefitDTO> getAvailableBenefits(String userUuid) {
        // Filtrar por mes actual
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        List<UserBenefit> activeBenefits = userBenefitRepository
            .findActiveByUserUuidAndCurrentMonth(userUuid, startOfMonth, endOfMonth);
        
        return activeBenefits.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Procesa el cobro de un beneficio
     */
    @Transactional
    public boolean claimBenefit(String userUuid, Long benefitId) {
        Optional<UserBenefit> userBenefitOpt = userBenefitRepository
            .findByUserUuidAndBenefitId(userUuid, benefitId);
        
        if (userBenefitOpt.isEmpty()) {
            log.warn("Beneficio no encontrado para usuario: {} y beneficio: {}", userUuid, benefitId);
            return false;
        }
        
        UserBenefit userBenefit = userBenefitOpt.get();
        
        if (userBenefit.getStatus() != BenefitStatus.ACTIVE) {
            log.warn("Beneficio no está activo para cobrar: {}", benefitId);
            return false;
        }
        
        // Marcar como cobrado
        userBenefit.setStatus(BenefitStatus.CLAIMED);
        userBenefit.setClaimedAt(LocalDateTime.now());
        userBenefitRepository.save(userBenefit);
        
        log.info("Beneficio cobrado exitosamente: {} por usuario: {}", benefitId, userUuid);
        return true;
    }

    /**
     * Verifica y activa beneficios basados en los juegos del mes actual
     */
    @Transactional
    public void checkAndActivateMonthlyBenefits(String userUuid) {
        log.info("Verificando beneficios mensuales para usuario: {}", userUuid);
        
        // Filtrar por mes actual
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // Contar juegos del mes actual
        List<GameActivity> currentMonthGames = gameActivityRepository
            .findByUserUuidAndGameDateBetween(userUuid, startOfMonth, endOfMonth);
        int monthlyGames = currentMonthGames.size();
        
        log.info("Usuario {} tiene {} juegos este mes", userUuid, monthlyGames);
        
        // Obtener beneficios elegibles basados en juegos del mes
        List<Benefit> eligibleBenefits = benefitRepository.findEligibleBenefits(monthlyGames);
        
        User user = userRepository.findByUuid(userUuid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + userUuid));
        
        for (Benefit benefit : eligibleBenefits) {
            // Verificar si el usuario ya tiene este beneficio ACTIVO en el mes actual
            Optional<UserBenefit> existingBenefit = userBenefitRepository
                .findActiveByUserUuidAndBenefitIdAndCurrentMonth(userUuid, benefit.getId(), startOfMonth, endOfMonth);
            
            if (existingBenefit.isEmpty()) {
                // Crear nuevo beneficio para el usuario
                UserBenefit userBenefit = new UserBenefit();
                userBenefit.setUser(user);
                userBenefit.setBenefit(benefit);
                userBenefit.setStatus(BenefitStatus.ACTIVE);
                
                userBenefitRepository.save(userBenefit);
                log.info("Beneficio mensual activado: {} para usuario: {} (juegos del mes: {})", 
                    benefit.getName(), userUuid, monthlyGames);
            }
        }
    }

    /**
     * Obtiene información de beneficios para el dashboard filtrada por mes actual
     */
    public UserDashboardDTO.BenefitInfo getBenefitInfo(String userUuid) {
        // Filtrar por mes actual
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        // VERIFICAR Y ACTIVAR BENEFICIOS BASADOS EN JUEGOS DEL MES ACTUAL
        try {
            checkAndActivateMonthlyBenefits(userUuid);
        } catch (Exception e) {
            log.error("Error al verificar beneficios mensuales para usuario: {}", userUuid, e);
        }
        
        // Beneficios disponibles del mes actual
        List<BenefitDTO> availableBenefits = getAvailableBenefits(userUuid);
        
        // Próximo beneficio basado en juegos del mes actual
        List<GameActivity> currentMonthGames = gameActivityRepository
            .findByUserUuidAndGameDateBetween(userUuid, startOfMonth, endOfMonth);
        UserDashboardDTO.BenefitInfo.NextBenefit nextBenefit = getNextBenefit(currentMonthGames.size());
        
        // Estadísticas del mes actual
        Long totalClaimed = userBenefitRepository
            .countClaimedByUserUuidAndCurrentMonth(userUuid, startOfMonth, endOfMonth);
        Long totalActive = userBenefitRepository
            .countActiveByUserUuidAndCurrentMonth(userUuid, startOfMonth, endOfMonth);
        
        return new UserDashboardDTO.BenefitInfo(
            availableBenefits,
            nextBenefit,
            totalClaimed,
            totalActive
        );
    }

    /**
     * Obtiene el próximo beneficio disponible
     */
    private UserDashboardDTO.BenefitInfo.NextBenefit getNextBenefit(Integer currentGames) {
        Optional<Benefit> nextBenefitOpt = benefitRepository.findNextBenefitByGames(currentGames);
        
        if (nextBenefitOpt.isEmpty()) {
            return null; // No hay más beneficios disponibles
        }
        
        Benefit nextBenefit = nextBenefitOpt.get();
        int gamesRemaining = nextBenefit.getRequiredGames() - currentGames;
        
        return new UserDashboardDTO.BenefitInfo.NextBenefit(
            nextBenefit.getName(),
            nextBenefit.getDescription(),
            nextBenefit.getRequiredGames(),
            gamesRemaining,
            nextBenefit.getCostLevel()
        );
    }

    /**
     * Convierte UserBenefit a BenefitDTO
     */
    private BenefitDTO convertToDTO(UserBenefit userBenefit) {
        Benefit benefit = userBenefit.getBenefit();
        
        return new BenefitDTO(
            benefit.getId(),
            benefit.getName(),
            benefit.getDescription(),
            benefit.getRequiredGames(),
            benefit.getCostLevel(),
            userBenefit.getStatus(),
            userBenefit.getStatus() == BenefitStatus.ACTIVE,
            userBenefit.getCreatedAt(),
            userBenefit.getClaimedAt()
        );
    }

    /**
     * Obtiene todos los beneficios base disponibles
     */
    public List<Benefit> getAllActiveBenefits() {
        return benefitRepository.findByActiveTrueOrderByRequiredGamesAsc();
    }
}