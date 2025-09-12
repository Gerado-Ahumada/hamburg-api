package com.hamburg.backend.controller;

import com.hamburg.backend.dto.PlayersPageResponse;
import com.hamburg.backend.service.UserService;
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
}