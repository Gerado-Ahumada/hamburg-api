package com.hamburg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hamburg.backend.dto.LoginRequest;
import com.hamburg.backend.dto.LoginResponse;
import com.hamburg.backend.dto.SignupRequest;
import com.hamburg.backend.model.*;
import com.hamburg.backend.repository.StatusRepository;
import com.hamburg.backend.repository.RoleRepository;
import com.hamburg.backend.repository.SessionRepository;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.security.UserDetailsImpl;
import com.hamburg.backend.security.jwt.JwtUtils;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private PasswordEncoder encoder;

    public LoginResponse autenticar(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Buscar el usuario para crear la sesión
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Desactivar sesiones anteriores del usuario
        sessionRepository.deactivateSessionsByUser(user);
        
        // Crear nueva sesión
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plus(Duration.ofMillis(jwtUtils.getJwtExpirationMs()));
        
        Session newSession = new Session(jwt, ahora, expiracion, user);
        sessionRepository.save(newSession);
        
        return LoginResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .role(userDetails.getAuthorities().stream().findFirst().get().getAuthority())
                .build();
    }
    
    public String registrarUsuario(SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return "Error: Username is already taken!";
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return "Error: Email is already in use!";
        }

        // Crear nueva cuenta de usuario
        User user = new User(signUpRequest.getUsername(),
                                    signUpRequest.getEmail(),
                                    encoder.encode(signUpRequest.getPassword()),
                                    signUpRequest.getFirstName(),
                                    signUpRequest.getLastName());
        
        user.setPhone(signUpRequest.getPhone());
        user.setPlayerCategory(signUpRequest.getPlayerCategory());

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role playerRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException("Error: Role not found."));
            roles.add(playerRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(adminRole);
                        break;
                    default:
                        Role playerRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                                .orElseThrow(() -> new RuntimeException("Error: Role not found."));
                        roles.add(playerRole);
                }
            });
        }

        user.setRoles(roles);
        
        // Asignar estado ACTIVE por defecto
        Status activeStatus = statusRepository.findByName(EStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Error: Status not found."));
        user.setStatus(activeStatus);

        userRepository.save(user);

        return "User registered successfully!";
    }
    
    public void cerrarSesion(String token) {
        sessionRepository.findByTokenAndActiveTrue(token)
                .ifPresent(session -> {
                    session.deactivate();
                    sessionRepository.save(session);
                });
    }
    
    public boolean validarSesion(String token) {
        return sessionRepository.findByTokenAndActiveTrue(token)
                .map(session -> !session.isExpired())
                .orElse(false);
    }
    
    public void limpiarSesionesExpiradas() {
        sessionRepository.deactivateExpiredSessions(LocalDateTime.now());
    }
}