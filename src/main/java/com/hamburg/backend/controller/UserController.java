package com.hamburg.backend.controller;

import com.hamburg.backend.dto.EditPlayerRequest;
import com.hamburg.backend.dto.MessageResponse;
import com.hamburg.backend.dto.PlayersPageResponse;
import com.hamburg.backend.service.AuthService;
import com.hamburg.backend.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/players")
    public ResponseEntity<?> getPlayers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category) {
        
        try {
            logger.info("Obteniendo lista de jugadores - Página: {}, Tamaño: {}, Nombre: {}, Categoría: {}", 
                       page, size, name, category);
            
            // Obtener lista paginada de jugadores
            PlayersPageResponse response = userService.getPlayers(page, size, name, category);
            
            logger.info("Se encontraron {} jugadores en total", response.getTotalElements());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error al obtener lista de jugadores: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error interno del servidor");
        }
    }
    
    @PutMapping("/players/{uuid}")
    public ResponseEntity<?> editPlayer(
            @PathVariable String uuid,
            @Valid @RequestBody EditPlayerRequest editRequest,
            @RequestHeader("Authorization") String token) {
        
        try {
            logger.info("Editando jugador con UUID: {}", uuid);
            
            // Extraer token sin el prefijo "Bearer "
            String jwtToken = token.replace("Bearer ", "");
            
            // Validar que el usuario tenga rol de administrador
            authService.validateAdminRole(jwtToken);
            
            // Validar que la sesión esté activa
            if (!authService.validarSesion(jwtToken)) {
                logger.warn("Intento de edición con sesión inválida o expirada");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new MessageResponse("Sesión inválida o expirada"));
            }
            
            // Editar el jugador
            String result = userService.editPlayer(uuid, editRequest);
            
            logger.info("Jugador editado exitosamente: {}", uuid);
            return ResponseEntity.ok(new MessageResponse(result));
            
        } catch (com.hamburg.backend.exception.UnauthorizedRoleException e) {
            logger.warn("Acceso denegado para editar jugador: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            logger.error("Error al editar jugador: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error interno al editar jugador: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error interno del servidor"));
        }
    }
}