package com.hamburg.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hamburg.backend.model.*;
import com.hamburg.backend.repository.*;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RolRepository rolRepository;
    
    @Autowired
    private EstadoRepository estadoRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        if (rolRepository.findByNombre(ERol.ROLE_ADMIN).isEmpty()) {
            rolRepository.save(new Rol(ERol.ROLE_ADMIN));
        }
        if (rolRepository.findByNombre(ERol.ROLE_PLAYER).isEmpty()) {
            rolRepository.save(new Rol(ERol.ROLE_PLAYER));
        }
        
        // Crear estados si no existen
        if (estadoRepository.findByNombre(EEstado.ACTIVE).isEmpty()) {
            estadoRepository.save(new Estado(EEstado.ACTIVE));
        }
        if (estadoRepository.findByNombre(EEstado.DESACTIVE).isEmpty()) {
            estadoRepository.save(new Estado(EEstado.DESACTIVE));
        }
        
        // Verificar si ya existe el usuario admin
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@hamburg.com");
            admin.setTelefono("123456789");
            admin.setCategoriaJugador("N/A");
            
            // Asignar rol de administrador
            Set<Rol> roles = new HashSet<>();
            Rol adminRole = rolRepository.findByNombre(ERol.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Rol ADMIN no encontrado."));
            roles.add(adminRole);
            admin.setRoles(roles);
            
            // Asignar estado activo
            Estado estadoActivo = estadoRepository.findByNombre(EEstado.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Error: Estado ACTIVE no encontrado."));
            admin.setEstado(estadoActivo);
            
            usuarioRepository.save(admin);
            
            System.out.println("Usuario admin creado con éxito");
        }
        
        // Verificar si ya existe el usuario player
        if (!usuarioRepository.existsByUsername("testplayer")) {
            Usuario player = new Usuario();
            player.setUsername("testplayer");
            player.setPassword(passwordEncoder.encode("player123"));
            player.setNombre("Test");
            player.setApellido("Player");
            player.setEmail("testplayer@hamburg.com");
            player.setTelefono("987654321");
            player.setCategoriaJugador("Principiante");
            
            // Asignar rol de player
            Set<Rol> playerRoles = new HashSet<>();
            Rol playerRole = rolRepository.findByNombre(ERol.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException("Error: Rol PLAYER no encontrado."));
            playerRoles.add(playerRole);
            player.setRoles(playerRoles);
            
            // Asignar estado activo
            Estado estadoActivo = estadoRepository.findByNombre(EEstado.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Error: Estado ACTIVE no encontrado."));
            player.setEstado(estadoActivo);
            
            usuarioRepository.save(player);
            
            System.out.println("Usuario testplayer creado con éxito");
        }
    }
}