package com.hamburg.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.hamburg.backend.model.Usuario;
import com.hamburg.backend.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Verificar si ya existe el usuario admin
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario admin = new Usuario();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setNombre("Administrador");
            admin.setApellido("Sistema");
            admin.setEmail("admin@hamburg.com");
            admin.setActivo(true);
            admin.setRol("ROLE_ADMIN");
            
            usuarioRepository.save(admin);
            
            System.out.println("Usuario admin creado con éxito");
        }
    }
}