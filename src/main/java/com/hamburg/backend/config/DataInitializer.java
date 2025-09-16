package com.hamburg.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hamburg.backend.model.*;
import com.hamburg.backend.entity.Benefit;
import com.hamburg.backend.entity.GameActivity;
import com.hamburg.backend.repository.*;
import com.hamburg.backend.repository.BenefitRepository;
import com.hamburg.backend.repository.GameActivityRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private BenefitRepository benefitRepository;
    
    @Autowired
    private GameActivityRepository gameActivityRepository;
    
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
            
            logger.info("Usuario admin creado con éxito");
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
            
            logger.info("Usuario testplayer creado con éxito");
        }
        
        // Crear beneficios iniciales si no existen
        initializeBenefits();
        
        // Crear registros de juego iniciales para testplayer
        initializeGameActivities();
    }
    
    private void initializeBenefits() {
        // Verificar si ya existen beneficios
        if (benefitRepository.count() == 0) {
            logger.info("Inicializando beneficios del sistema...");
            
            // Beneficio por 3 partidos
            Benefit benefit3 = new Benefit();
            benefit3.setName("Descuento Principiante");
            benefit3.setRequiredGames(3);
            benefit3.setCostLevel(1);
            benefit3.setDescription("¡Felicidades por tus primeros 3 partidos! Disfruta de un 10% de descuento en tu próxima reserva.");
            benefit3.setActive(true);
            benefitRepository.save(benefit3);
            
            // Beneficio por 6 partidos
            Benefit benefit6 = new Benefit();
            benefit6.setName("Bebida Gratis");
            benefit6.setRequiredGames(6);
            benefit6.setCostLevel(2);
            benefit6.setDescription("¡6 partidos completados! Reclama una bebida gratis en tu próxima visita al club.");
            benefit6.setActive(true);
            benefitRepository.save(benefit6);
            
            // Beneficio por 9 partidos
            Benefit benefit9 = new Benefit();
            benefit9.setName("Descuento Intermedio");
            benefit9.setRequiredGames(9);
            benefit9.setCostLevel(2);
            benefit9.setDescription("¡Excelente progreso! Obtén un 15% de descuento en cualquier reserva de cancha.");
            benefit9.setActive(true);
            benefitRepository.save(benefit9);
            
            // Beneficio por 12 partidos
            Benefit benefit12 = new Benefit();
            benefit12.setName("Clase Gratuita");
            benefit12.setRequiredGames(12);
            benefit12.setCostLevel(3);
            benefit12.setDescription("¡Una docena de partidos! Disfruta de una clase gratuita con nuestros instructores profesionales.");
            benefit12.setActive(true);
            benefitRepository.save(benefit12);
            
            // Beneficio por 15 partidos
            Benefit benefit15 = new Benefit();
            benefit15.setName("Descuento Avanzado");
            benefit15.setRequiredGames(15);
            benefit15.setCostLevel(3);
            benefit15.setDescription("¡Jugador dedicado! Recibe un 20% de descuento en reservas y productos del pro shop.");
            benefit15.setActive(true);
            benefitRepository.save(benefit15);
            
            // Beneficio por 18 partidos
            Benefit benefit18 = new Benefit();
            benefit18.setName("Membresía VIP Mensual");
            benefit18.setRequiredGames(18);
            benefit18.setCostLevel(4);
            benefit18.setDescription("¡Jugador élite! Disfruta de una membresía VIP gratuita por un mes con acceso prioritario a canchas.");
            benefit18.setActive(true);
            benefitRepository.save(benefit18);
            
            logger.info("Beneficios inicializados exitosamente: 6 beneficios creados");
        } else {
            logger.info("Los beneficios ya existen en la base de datos");
        }
    }
    
    private void initializeGameActivities() {
        // Buscar el usuario testplayer
        User testPlayer = userRepository.findByUsername("testplayer").orElse(null);
        
        if (testPlayer != null) {
            // Verificar si ya existen registros de juego para testplayer
            long existingGames = gameActivityRepository.countByUser(testPlayer);
            
            if (existingGames == 0) {
                logger.info("Inicializando registros de juego para testplayer...");
                
                Random random = new Random();
                int gamesToCreate = 10; // Crear 10 registros iniciales
                
                for (int i = 0; i < gamesToCreate; i++) {
                    GameActivity gameActivity = new GameActivity();
                    gameActivity.setUser(testPlayer);
                    
                    // Generar fechas aleatorias en los últimos 30 días
                    int daysAgo = random.nextInt(30);
                    int hoursAgo = random.nextInt(12) + 8; // Entre 8 y 20 horas
                    int minutesAgo = random.nextInt(60);
                    
                    LocalDateTime gameDate = LocalDateTime.now()
                            .minusDays(daysAgo)
                            .withHour(hoursAgo)
                            .withMinute(minutesAgo)
                            .withSecond(0)
                            .withNano(0);
                    
                    gameActivity.setGameDate(gameDate);
                    
                    gameActivityRepository.save(gameActivity);
                }
                
                logger.info("Registros de juego inicializados exitosamente: {} registros creados para testplayer", gamesToCreate);
            } else {
                logger.info("Los registros de juego para testplayer ya existen en la base de datos: {} registros", existingGames);
            }
        } else {
            logger.warn("Usuario testplayer no encontrado, no se pueden crear registros de juego");
        }
    }
}