package com.hamburg.backend.controller;

import com.hamburg.backend.dto.GameActivityDTO;
import com.hamburg.backend.dto.UserDashboardDTO;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.service.BenefitService;
import com.hamburg.backend.service.GameActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final GameActivityService gameActivityService;
    private final BenefitService benefitService;
    private final UserRepository userRepository;

    /**
     * Endpoint unificado para obtener toda la información del dashboard del usuario
     * Incluye: información del usuario, estadísticas de juegos, actividades y beneficios
     * IMPORTANTE: Todos los datos están filtrados por el mes actual de cuando se consulta
     * 
     * @param userUuid UUID del usuario
     * @param limit Límite opcional para paginación de actividades (si no se envía, muestra todas del mes actual)
     * @return UserDashboardDTO con toda la información del dashboard filtrada por mes actual
     */
    @GetMapping("/dashboard")
    public ResponseEntity<UserDashboardDTO> getUserDashboard(
            @RequestParam String userUuid,
            @RequestParam(required = false) Integer limit) {
        
        log.info("Obteniendo dashboard para usuario UUID: {}, límite: {}", userUuid, limit);
        
        try {
            // Verificar que el usuario existe
            Optional<User> userOpt = userRepository.findByUuid(userUuid);
            if (userOpt.isEmpty()) {
                log.warn("Usuario no encontrado con UUID: {}", userUuid);
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            // 1. Información del usuario
            UserDashboardDTO.UserInfo userInfo = new UserDashboardDTO.UserInfo(
                user.getUuid(),
                user.getUsername(),
                user.getEmail()
            );
            
            // 2. Estadísticas de juegos
            UserDashboardDTO.GameStats gameStats = gameActivityService.getGameStats(userUuid);
            
            // 3. Actividades de juego (con paginación opcional)
            List<GameActivityDTO> gameActivities = gameActivityService
                .getGameActivitiesForDashboard(userUuid, limit);
            
            // 4. Información de beneficios
            UserDashboardDTO.BenefitInfo benefitInfo = benefitService.getBenefitInfo(userUuid);
            
            // Construir response completo
            UserDashboardDTO dashboard = new UserDashboardDTO(
                userInfo,
                gameStats,
                gameActivities,
                benefitInfo
            );
            
            log.info("Dashboard generado exitosamente para usuario: {} - {} juegos, {} beneficios activos", 
                userUuid, gameStats.getTotalGames(), benefitInfo.getTotalActive());
            
            return ResponseEntity.ok(dashboard);
            
        } catch (Exception e) {
            log.error("Error al generar dashboard para usuario UUID: {}", userUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint para cobrar un beneficio específico
     * 
     * @param userUuid UUID del usuario
     * @param benefitId ID del beneficio a cobrar
     * @return ResponseEntity con el resultado de la operación
     */
    @PostMapping("/benefits/{benefitId}/claim")
    public ResponseEntity<String> claimBenefit(
            @RequestParam String userUuid,
            @PathVariable Long benefitId) {
        
        log.info("Procesando cobro de beneficio {} para usuario: {}", benefitId, userUuid);
        
        try {
            boolean success = benefitService.claimBenefit(userUuid, benefitId);
            
            if (success) {
                log.info("Beneficio {} cobrado exitosamente por usuario: {}", benefitId, userUuid);
                return ResponseEntity.ok("Beneficio cobrado exitosamente");
            } else {
                log.warn("No se pudo cobrar el beneficio {} para usuario: {}", benefitId, userUuid);
                return ResponseEntity.badRequest().body("No se pudo cobrar el beneficio");
            }
            
        } catch (Exception e) {
            log.error("Error al cobrar beneficio {} para usuario: {}", benefitId, userUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error interno del servidor");
        }
    }

    /**
     * Endpoint para obtener solo los beneficios disponibles de un usuario
     * (Endpoint alternativo si se necesita solo esta información)
     * 
     * @param userUuid UUID del usuario
     * @return Lista de beneficios disponibles
     */
    @GetMapping("/benefits")
    public ResponseEntity<UserDashboardDTO.BenefitInfo> getUserBenefits(
            @RequestParam String userUuid) {
        
        log.info("Obteniendo beneficios para usuario UUID: {}", userUuid);
        
        try {
            UserDashboardDTO.BenefitInfo benefitInfo = benefitService.getBenefitInfo(userUuid);
            return ResponseEntity.ok(benefitInfo);
            
        } catch (Exception e) {
            log.error("Error al obtener beneficios para usuario UUID: {}", userUuid, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}