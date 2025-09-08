package com.hamburg.backend.controller;

import java.io.BufferedReader;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamburg.backend.dto.LoginRequest;
import com.hamburg.backend.dto.LoginResponse;
import com.hamburg.backend.dto.MessageResponse;
import com.hamburg.backend.dto.SignupRequest;
import com.hamburg.backend.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        System.out.println("=== AuthController.test llamado ===");
        return ResponseEntity.ok("API funcionando correctamente");
    }
    
    @PostMapping("/test-login")
    public ResponseEntity<String> testLogin(@RequestBody String rawBody) {
        System.out.println("=== AuthController.testLogin llamado ===");
        System.out.println("Raw body recibido: " + rawBody);
        return ResponseEntity.ok("Login test recibido: " + rawBody);
    }
    
    @PostMapping("/simple-test")
    public ResponseEntity<String> simpleTest() {
        System.out.println("=== AuthController.simpleTest llamado ===");
        return ResponseEntity.ok("Simple test funcionando");
    }
    
    @PostMapping("/debug-request")
    public ResponseEntity<String> debugRequest(HttpServletRequest request) {
        try {
            StringBuilder debug = new StringBuilder();
            debug.append("Method: ").append(request.getMethod()).append("\n");
            debug.append("Content-Type: ").append(request.getContentType()).append("\n");
            debug.append("Content-Length: ").append(request.getContentLength()).append("\n");
            
            // Leer el body raw
            BufferedReader reader = request.getReader();
            String line;
            StringBuilder body = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            debug.append("Body: ").append(body.toString()).append("\n");
            
            return ResponseEntity.ok(debug.toString());
        } catch (Exception e) {
            return ResponseEntity.ok("Error reading request: " + e.getMessage());
        }
    }
    
    @PostMapping("/login-no-validation")
    public ResponseEntity<String> loginNoValidation(@RequestBody LoginRequest loginRequest) {
        System.out.println("=== AuthController.loginNoValidation llamado ===");
        System.out.println("LoginRequest recibido: " + loginRequest);
        return ResponseEntity.ok("Login sin validación recibido: " + loginRequest.getUsername());
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticarUsuario(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("=== AuthController.autenticarUsuario llamado ===");
            System.out.println("LoginRequest recibido: " + loginRequest);
            
            LoginResponse loginResponse = authService.autenticar(loginRequest);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            System.err.println("Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(LoginResponse.builder()
                    .token(null)
                    .username("Error en autenticación: " + e.getMessage())
                    .build());
        }
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
    
}