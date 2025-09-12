package com.hamburg.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hamburg.backend.dto.LoginRequest;
import com.hamburg.backend.dto.LoginResponse;
import com.hamburg.backend.dto.SignupRequest;
import com.hamburg.backend.exception.UnauthorizedRoleException;
import com.hamburg.backend.model.ERole;
import com.hamburg.backend.model.EStatus;
import com.hamburg.backend.model.Role;
import com.hamburg.backend.model.Session;
import com.hamburg.backend.model.Status;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.RoleRepository;
import com.hamburg.backend.repository.StatusRepository;
import com.hamburg.backend.security.UserDetailsImpl;
import com.hamburg.backend.security.jwt.JwtUtils;

public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private com.hamburg.backend.repository.UserRepository userRepository;

    @Mock
    private com.hamburg.backend.repository.SessionRepository sessionRepository;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private UserDetailsImpl userDetails;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");

        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("admin")
                .email("admin@hamburg.com")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();
        
        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@hamburg.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");
        signupRequest.setPhone("123456789");
        signupRequest.setPlayerCategory("Amateur");
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@hamburg.com");
        testUser.setUuid("test-uuid-123");
        
        playerRole = new Role();
        playerRole.setId(1);
        playerRole.setName(ERole.ROLE_PLAYER);
        
        adminRole = new Role();
        adminRole.setId(2);
        adminRole.setName(ERole.ROLE_ADMIN);
        
        activeStatus = new Status();
        activeStatus.setId(1);
        activeStatus.setName(EStatus.ACTIVE);
        
        testSession = new Session();
        testSession.setId(1L);
        testSession.setToken("test-token");
        testSession.setCreatedAt(LocalDateTime.now());
        testSession.setExpiresAt(LocalDateTime.now().plusHours(1));
        testSession.setActive(true);
        testSession.setUser(testUser);
    }

    @Test
    public void testAutenticar() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(jwtUtils.getJwtExpirationMs()).thenReturn(86400000L); // 24 hours
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(sessionRepository).deactivateSessionsByUser(testUser);
        when(sessionRepository.save(any(Session.class))).thenReturn(testSession);

        LoginResponse response = authService.autenticar(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());
        
        verify(sessionRepository).deactivateSessionsByUser(testUser);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    public void testRegistrarUsuarioSuccess() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@hamburg.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(roleRepository.findByName(ERole.ROLE_PLAYER)).thenReturn(Optional.of(playerRole));
        when(statusRepository.findByName(EStatus.ACTIVE)).thenReturn(Optional.of(activeStatus));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.registrarUsuario(signupRequest);

        assertEquals("User registered successfully!", result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    public void testRegistrarUsuarioUsernameExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        String result = authService.registrarUsuario(signupRequest);

        assertEquals("Error: Username is already taken!", result);
    }

    @Test
    public void testRegistrarUsuarioEmailExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@hamburg.com")).thenReturn(true);

        String result = authService.registrarUsuario(signupRequest);

        assertEquals("Error: Email is already in use!", result);
    }

    @Test
    public void testRegistrarUsuarioWithAdminRole() {
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        signupRequest.setRole(roles);
        
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@hamburg.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(statusRepository.findByName(EStatus.ACTIVE)).thenReturn(Optional.of(activeStatus));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.registrarUsuario(signupRequest);

        assertEquals("User registered successfully!", result);
        verify(roleRepository).findByName(ERole.ROLE_ADMIN);
    }

    @Test
    public void testCerrarSesionSuccess() {
        when(sessionRepository.findByTokenAndActiveTrue("test-token"))
            .thenReturn(Optional.of(testSession));
        when(sessionRepository.save(testSession)).thenReturn(testSession);

        authService.cerrarSesion("test-token");

        verify(sessionRepository).findByTokenAndActiveTrue("test-token");
        verify(sessionRepository).save(testSession);
    }

    @Test
    public void testCerrarSesionTokenNotFound() {
        when(sessionRepository.findByTokenAndActiveTrue("invalid-token"))
            .thenReturn(Optional.empty());

        authService.cerrarSesion("invalid-token");

        verify(sessionRepository).findByTokenAndActiveTrue("invalid-token");
    }

    @Test
    public void testValidarSesionValid() {
        when(sessionRepository.findByTokenAndActiveTrue("test-token"))
            .thenReturn(Optional.of(testSession));

        boolean result = authService.validarSesion("test-token");

        assertTrue(result);
        verify(sessionRepository).findByTokenAndActiveTrue("test-token");
    }

    @Test
    public void testValidarSesionInvalid() {
        when(sessionRepository.findByTokenAndActiveTrue("invalid-token"))
            .thenReturn(Optional.empty());

        boolean result = authService.validarSesion("invalid-token");

        assertFalse(result);
    }

    @Test
    public void testValidarSesionExpired() {
        Session expiredSession = new Session();
        expiredSession.setToken("expired-token");
        expiredSession.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        expiredSession.setActive(true);
        
        when(sessionRepository.findByTokenAndActiveTrue("expired-token"))
            .thenReturn(Optional.of(expiredSession));

        boolean result = authService.validarSesion("expired-token");

        assertFalse(result);
    }

    @Test
    public void testGetUserFromToken() {
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        User result = authService.getUserFromToken("test-token");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        verify(jwtUtils).getUserNameFromJwtToken("test-token");
        verify(userRepository).findByUsername("admin");
    }

    @Test
    public void testGetUserFromTokenUserNotFound() {
        when(jwtUtils.getUserNameFromJwtToken("test-token")).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.getUserFromToken("test-token");
        });

        assertEquals("Usuario no encontrado: nonexistent", exception.getMessage());
    }

    @Test
    public void testValidateAdminRoleSuccess() {
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        testUser.setRoles(roles);
        
        when(jwtUtils.getUserNameFromJwtToken("admin-token")).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        // Should not throw exception
        authService.validateAdminRole("admin-token");
        
        verify(jwtUtils).getUserNameFromJwtToken("admin-token");
        verify(userRepository).findByUsername("admin");
    }

    @Test
    public void testValidateAdminRoleUnauthorized() {
        Set<Role> roles = new HashSet<>();
        roles.add(playerRole); // Only player role
        testUser.setRoles(roles);
        
        when(jwtUtils.getUserNameFromJwtToken("player-token")).thenReturn("player");
        when(userRepository.findByUsername("player")).thenReturn(Optional.of(testUser));

        UnauthorizedRoleException exception = assertThrows(UnauthorizedRoleException.class, () -> {
            authService.validateAdminRole("player-token");
        });

        assertEquals("Acceso denegado: Se requiere rol de administrador para realizar esta acción", 
                     exception.getMessage());
    }

    @Test
    public void testLimpiarSesionesExpiradas() {
        doNothing().when(sessionRepository).deactivateExpiredSessions(any(LocalDateTime.class));

        authService.limpiarSesionesExpiradas();

        verify(sessionRepository).deactivateExpiredSessions(any(LocalDateTime.class));
    }
}