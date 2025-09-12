package com.hamburg.backend.service;

import com.hamburg.backend.dto.GameActivityRequest;
import com.hamburg.backend.dto.GameActivityResponse;
import com.hamburg.backend.dto.GameActivityRangeResponse;
import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.GameActivityRepository;
import com.hamburg.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GameActivityService {
    
    private static final Logger logger = LoggerFactory.getLogger(GameActivityService.class);
    
    @Autowired
    private GameActivityRepository gameActivityRepository;
    
    @Autowired
    private UserRepository userRepository;
    
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
}