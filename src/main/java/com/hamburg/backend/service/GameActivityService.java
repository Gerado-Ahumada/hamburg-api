package com.hamburg.backend.service;

import com.hamburg.backend.dto.GameActivityDTO;
import com.hamburg.backend.dto.GameActivityRequest;
import com.hamburg.backend.dto.GameActivityResponse;
import com.hamburg.backend.dto.GameActivityRangeResponse;
import com.hamburg.backend.dto.UserDashboardDTO;
import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.GameActivityRepository;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.service.BenefitService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameActivityService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameActivityService.class);
    
    @Autowired
    private GameActivityRepository gameActivityRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Inyección lazy para evitar dependencia circular
    private BenefitService benefitService;
    
    @Autowired
    public void setBenefitService(@Lazy BenefitService benefitService) {
        this.benefitService = benefitService;
    }
    
    public GameActivity registerGameActivity(GameActivityRequest request) {
        logger.info("Registrando actividad de juego para usuario UUID: {}", request.getUserUuid());
        
        // Buscar usuario por UUID
        User user = userRepository.findByUuid(request.getUserUuid())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con UUID: " + request.getUserUuid()));
        
        // Crear nueva actividad de juego
        GameActivity gameActivity = new GameActivity();
        gameActivity.setUser(user);
        gameActivity.setGameDate(request.getGameDate());
        
        // Guardar en base de datos
        GameActivity savedActivity = gameActivityRepository.save(gameActivity);
        
        logger.info("Actividad de juego registrada exitosamente con ID: {}", savedActivity.getId());
        
        // Verificar y activar beneficios automáticamente
        try {
            benefitService.checkAndActivateBenefits(request.getUserUuid());
            logger.info("Verificación de beneficios completada para usuario: {}", request.getUserUuid());
        } catch (Exception e) {
            logger.error("Error al verificar beneficios para usuario: {}", request.getUserUuid(), e);
            // No fallar el registro del juego por errores en beneficios
        }
        
        return savedActivity;
    }
    
    public List<GameActivityResponse> getGameActivitiesByUserUuid(String userUuid) {
        logger.info("Obteniendo actividades de juego para usuario UUID: {}", userUuid);
        
        // Buscar usuario por UUID
        User user = userRepository.findByUuid(userUuid)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con UUID: " + userUuid));
        
        // Obtener actividades del usuario y convertir a DTO
        List<GameActivity> activities = gameActivityRepository.findByUserOrderByGameDateDesc(user);
        return activities.stream()
            .map(activity -> new GameActivityResponse(
                activity.getId(),
                activity.getGameDate(),
                activity.getUser().getUuid(),
                activity.getUser().getUsername(),
                activity.getUser().getFirstName(),
                activity.getUser().getLastName()
            ))
            .collect(Collectors.toList());
    }
    
    public GameActivityRangeResponse getGameActivitiesByUserUuidAndRange(String userUuid, int year, int month) {
        List<GameActivity> activities = gameActivityRepository.findByUserUuidAndYearAndMonthOrderByGameDateDesc(userUuid, year, month);
        
        List<GameActivityResponse> activityResponses = activities.stream()
                .map(activity -> new GameActivityResponse(
                        activity.getId(),
                        activity.getGameDate(),
                        activity.getUser().getUuid(),
                        activity.getUser().getUsername(),
                        activity.getUser().getFirstName(),
                        activity.getUser().getLastName()
                ))
                .collect(Collectors.toList());
        
        return new GameActivityRangeResponse(activities.size(), activityResponses);
    }
    
    public List<GameActivity> getGameActivitiesByUserUuidAndDateRange(String userUuid, LocalDateTime startDate, LocalDateTime endDate) {
        logger.info("Obteniendo actividades de juego para usuario UUID: {} entre {} y {}", userUuid, startDate, endDate);
        return gameActivityRepository.findByUserUuidAndGameDateBetween(userUuid, startDate, endDate);
    }

    /**
     * Obtiene estadísticas de juegos para el dashboard
     */
    public UserDashboardDTO.GameStats getGameStats(String userUuid) {
        logger.info("Obteniendo estadísticas de juegos para usuario UUID: {}", userUuid);
        
        // Total de juegos
        Long totalGames = gameActivityRepository.countByUserUuid(userUuid);
        
        // Juegos este mes
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        List<GameActivity> thisMonthActivities = gameActivityRepository
            .findByUserUuidAndGameDateBetween(userUuid, startOfMonth, endOfMonth);
        Long gamesThisMonth = (long) thisMonthActivities.size();
        
        // Juegos este año
        LocalDateTime startOfYear = LocalDateTime.of(LocalDateTime.now().getYear(), 1, 1, 0, 0);
        LocalDateTime endOfYear = LocalDateTime.of(LocalDateTime.now().getYear(), 12, 31, 23, 59, 59);
        
        List<GameActivity> thisYearActivities = gameActivityRepository
            .findByUserUuidAndGameDateBetween(userUuid, startOfYear, endOfYear);
        Long gamesThisYear = (long) thisYearActivities.size();
        
        // Última fecha de juego
        LocalDateTime lastGameDate = null;
        List<GameActivity> recentActivities = gameActivityRepository
            .findByUserUuidOrderByGameDateDesc(userUuid);
        if (!recentActivities.isEmpty()) {
            lastGameDate = recentActivities.get(0).getGameDate();
        }
        
        return new UserDashboardDTO.GameStats(
            totalGames,
            gamesThisMonth,
            gamesThisYear,
            lastGameDate
        );
    }

    /**
     * Obtiene actividades de juego para el dashboard con paginación opcional
     * Filtra solo las actividades del mes actual
     */
    public List<GameActivityDTO> getGameActivitiesForDashboard(String userUuid, Integer limit) {
        logger.info("Obteniendo actividades para dashboard - Usuario UUID: {}, Límite: {}", userUuid, limit);
        
        // Filtrar por mes actual
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        List<GameActivity> activities = gameActivityRepository
            .findByUserUuidAndGameDateBetween(userUuid, startOfMonth, endOfMonth);
        
        // Ordenar por fecha descendente
        activities = activities.stream()
            .sorted((a, b) -> b.getGameDate().compareTo(a.getGameDate()))
            .collect(Collectors.toList());
        
        // Aplicar límite si se especifica
        if (limit != null && limit > 0 && activities.size() > limit) {
            activities = activities.subList(0, limit);
        }
        
        return activities.stream()
            .map(this::convertToGameActivityDTO)
            .collect(Collectors.toList());
    }

    /**
     * Convierte GameActivity a GameActivityDTO
     */
    private GameActivityDTO convertToGameActivityDTO(GameActivity activity) {
        return new GameActivityDTO(
            activity.getId(),
            activity.getGameDate(),
            activity.getCreatedAt(),
            activity.getUser().getUuid(),
            activity.getUser().getUsername()
        );
    }
}