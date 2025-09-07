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
import com.hamburg.backend.repository.EstadoRepository;
import com.hamburg.backend.repository.RolRepository;
import com.hamburg.backend.repository.SesionRepository;
import com.hamburg.backend.repository.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    
    @Autowired
    private EstadoRepository estadoRepository;
    
    @Autowired
    private SesionRepository sesionRepository;
    
    @Autowired
    private PasswordEncoder encoder;

    public LoginResponse autenticar(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        // Buscar el usuario para crear la sesión
        Usuario usuario = usuarioRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        // Desactivar sesiones anteriores del usuario
        sesionRepository.desactivarSesionesPorUsuario(usuario);
        
        // Crear nueva sesión
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime expiracion = ahora.plus(Duration.ofMillis(jwtUtils.getJwtExpirationMs()));
        
        Sesion nuevaSesion = new Sesion(jwt, ahora, expiracion, usuario);
        sesionRepository.save(nuevaSesion);
        
        return LoginResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .rol(userDetails.getAuthorities().stream().findFirst().get().getAuthority())
                .build();
    }
    
    public String registrarUsuario(SignupRequest signUpRequest) {
        if (usuarioRepository.existsByUsername(signUpRequest.getUsername())) {
            return "Error: El nombre de usuario ya está en uso!";
        }

        if (usuarioRepository.existsByEmail(signUpRequest.getEmail())) {
            return "Error: El email ya está en uso!";
        }

        // Crear nueva cuenta de usuario
        Usuario usuario = new Usuario(signUpRequest.getUsername(),
                                    signUpRequest.getEmail(),
                                    encoder.encode(signUpRequest.getPassword()),
                                    signUpRequest.getNombre(),
                                    signUpRequest.getApellido());
        
        usuario.setTelefono(signUpRequest.getTelefono());
        usuario.setCategoriaJugador(signUpRequest.getCategoriaJugador());

        Set<String> strRoles = signUpRequest.getRol();
        Set<Rol> roles = new HashSet<>();

        if (strRoles == null) {
            Rol playerRole = rolRepository.findByNombre(ERol.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
            roles.add(playerRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Rol adminRole = rolRepository.findByNombre(ERol.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(adminRole);
                        break;
                    default:
                        Rol playerRole = rolRepository.findByNombre(ERol.ROLE_PLAYER)
                                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));
                        roles.add(playerRole);
                }
            });
        }

        usuario.setRoles(roles);
        
        // Asignar estado ACTIVE por defecto
        Estado estadoActivo = estadoRepository.findByNombre(EEstado.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Error: Estado no encontrado."));
        usuario.setEstado(estadoActivo);

        usuarioRepository.save(usuario);

        return "Usuario registrado exitosamente!";
    }
    
    public void cerrarSesion(String token) {
        sesionRepository.findByTokenAndActivaTrue(token)
                .ifPresent(sesion -> {
                    sesion.desactivar();
                    sesionRepository.save(sesion);
                });
    }
    
    public boolean validarSesion(String token) {
        return sesionRepository.findByTokenAndActivaTrue(token)
                .map(sesion -> !sesion.isExpirada())
                .orElse(false);
    }
    
    public void limpiarSesionesExpiradas() {
        sesionRepository.desactivarSesionesExpiradas(LocalDateTime.now());
    }
}