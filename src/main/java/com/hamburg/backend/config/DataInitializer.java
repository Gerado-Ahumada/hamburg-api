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
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Crear roles si no existen
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
        if (roleRepository.findByName(ERole.ROLE_PLAYER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_PLAYER));
        }
        
        // Crear estados si no existen
        if (statusRepository.findByName(EStatus.ACTIVE).isEmpty()) {
            statusRepository.save(new Status(EStatus.ACTIVE));
        }
        if (statusRepository.findByName(EStatus.INACTIVE).isEmpty()) {
            statusRepository.save(new Status(EStatus.INACTIVE));
        }
        
        // Verificar si ya existe el usuario admin
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setFirstName("Administrator");
            admin.setLastName("System");
            admin.setEmail("admin@hamburg.com");
            admin.setPhone("123456789");
            admin.setPlayerCategory("N/A");
            
            // Asignar rol de administrador
            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Error: Role ADMIN not found."));
            roles.add(adminRole);
            admin.setRoles(roles);
            
            // Asignar estado activo
            Status activeStatus = statusRepository.findByName(EStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Error: Status ACTIVE not found."));
            admin.setStatus(activeStatus);
            
            userRepository.save(admin);
            
            System.out.println("Usuario admin creado con éxito");
        }
        
        // Verificar si ya existe el usuario player
        if (!userRepository.existsByUsername("testplayer")) {
            User player = new User();
            player.setUsername("testplayer");
            player.setPassword(passwordEncoder.encode("player123"));
            player.setFirstName("Test");
            player.setLastName("Player");
            player.setEmail("testplayer@hamburg.com");
            player.setPhone("987654321");
            player.setPlayerCategory("Beginner");
            
            // Asignar rol de player
            Set<Role> playerRoles = new HashSet<>();
            Role playerRole = roleRepository.findByName(ERole.ROLE_PLAYER)
                    .orElseThrow(() -> new RuntimeException("Error: Role PLAYER not found."));
            playerRoles.add(playerRole);
            player.setRoles(playerRoles);
            
            // Asignar estado activo
            Status activeStatus = statusRepository.findByName(EStatus.ACTIVE)
                    .orElseThrow(() -> new RuntimeException("Error: Status ACTIVE not found."));
            player.setStatus(activeStatus);
            
            userRepository.save(player);
            
            System.out.println("Usuario testplayer creado con éxito");
        }
    }
}