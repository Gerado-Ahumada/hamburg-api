package com.hamburg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hamburg.backend.dto.LoginRequest;
import com.hamburg.backend.dto.LoginResponse;
import com.hamburg.backend.dto.MessageResponse;
import com.hamburg.backend.dto.SignupRequest;
import com.hamburg.backend.exception.UnauthorizedRoleException;
import com.hamburg.backend.service.AuthService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("API funcionando correctamente");
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticarUsuario(@RequestBody LoginRequest loginRequest) {
        try {
            logger.info("Solicitud de login recibida - Usuario: {}", loginRequest.getUsername());
            LoginResponse loginResponse = authService.autenticar(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .token(null)
                    .username("Error en autenticación: " + e.getMessage())
                    .build());
        }
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registrarUsuario(
            @Valid @RequestBody SignupRequest signUpRequest,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            logger.info("Solicitud de registro recibida - Usuario: {}, Email: {}, Rol: {}", 
                       signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getRole());
            // Extraer el token del header Authorization
            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            
            if (token == null) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Token de autorización requerido"));
            }
            
            // Validar que el usuario tenga rol de administrador
            authService.validateAdminRole(token);
            
            String resultado = authService.registrarUsuario(signUpRequest);
            
            if (resultado.startsWith("Error:")) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse(resultado));
            }
            
            return ResponseEntity.ok(new MessageResponse(resultado));
            
        } catch (UnauthorizedRoleException e) {
            return ResponseEntity.status(403)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error al registrar usuario: " + e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> cerrarSesion(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extraer el token del header Authorization
            String token = null;
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7);
            }
            
            if (token != null) {
                authService.cerrarSesion(token);
                return ResponseEntity.ok(new MessageResponse("Sesión cerrada exitosamente"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Token no proporcionado"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error al cerrar sesión: " + e.getMessage()));
        }
    }
    
}