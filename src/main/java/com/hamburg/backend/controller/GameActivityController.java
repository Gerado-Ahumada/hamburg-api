package com.hamburg.backend.controller;

import com.hamburg.backend.dto.GameActivityRequest;
import com.hamburg.backend.dto.GameActivityResponse;
import com.hamburg.backend.dto.GameActivityRangeResponse;
import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.service.GameActivityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/game-activity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class GameActivityController {
    
    private static final Logger logger = LoggerFactory.getLogger(GameActivityController.class);
    
    @Autowired
    private GameActivityService gameActivityService;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerGameActivity(@Valid @RequestBody GameActivityRequest request) {
        try {
            logger.info("Recibida solicitud para registrar actividad de juego - UUID: {}, GameDate: {}", 
                       request.getUserUuid(), request.getGameDate());
            
            GameActivity gameActivity = gameActivityService.registerGameActivity(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Actividad de juego registrada exitosamente");
            response.put("activityId", gameActivity.getId());
            response.put("gameDate", gameActivity.getGameDate());
            response.put("userUuid", request.getUserUuid());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            logger.error("Error al registrar actividad de juego: {}", e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            logger.error("Error interno del servidor: {}", e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error interno del servidor");
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/user/{userUuid}")
    public ResponseEntity<List<GameActivityResponse>> getUserActivities(@PathVariable String userUuid) {
        logger.info("Obteniendo actividades para usuario UUID: {}", userUuid);
        
        try {
            List<GameActivityResponse> activities = gameActivityService.getGameActivitiesByUserUuid(userUuid);
            return ResponseEntity.ok(activities);
        } catch (Exception e) {
            logger.error("Error al obtener actividades para usuario UUID: {}", userUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userUuid}/range")
    public ResponseEntity<GameActivityRangeResponse> getGameActivitiesByUserAndRange(
            @PathVariable String userUuid,
            @RequestParam int year,
            @RequestParam int month) {
        try {
            logger.info("Obteniendo actividades de juego para usuario UUID: {} para {}/{}", userUuid, month, year);
            
            GameActivityRangeResponse response = gameActivityService.getGameActivitiesByUserUuidAndRange(userUuid, year, month);
            
            logger.info("Se encontraron {} actividades para el usuario UUID: {} en {}/{}", response.getTotal_game_activity(), userUuid, month, year);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al obtener actividades por mes/año para usuario UUID: {}", userUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/test/user/{username}/uuid")
    public ResponseEntity<Map<String, String>> getUserUuid(@PathVariable String username) {
        logger.info("Obteniendo UUID para usuario: {}", username);
        
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + username));
            
            Map<String, String> response = new HashMap<>();
            response.put("username", username);
            response.put("uuid", user.getUuid());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error obteniendo UUID para usuario {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Usuario no encontrado"));
        }
    }
}