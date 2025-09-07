package com.hamburg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.hamburg.backend.service.AuthService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticarUsuario(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.autenticar(loginRequest);
        return ResponseEntity.ok(loginResponse);
    }
    
    @PostMapping("/signup")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody SignupRequest signUpRequest) {
        String resultado = authService.registrarUsuario(signUpRequest);
        
        if (resultado.startsWith("Error:")) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(resultado));
        }
        
        return ResponseEntity.ok(new MessageResponse(resultado));
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
    
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(new MessageResponse("Endpoint de prueba funcionando"));
    }
}