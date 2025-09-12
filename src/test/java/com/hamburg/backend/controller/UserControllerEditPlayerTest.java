package com.hamburg.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamburg.backend.dto.EditPlayerRequest;
import com.hamburg.backend.dto.MessageResponse;
import com.hamburg.backend.model.Session;
import com.hamburg.backend.model.User;
import com.hamburg.backend.model.Role;
import com.hamburg.backend.model.ERole;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDateTime;
import com.hamburg.backend.repository.SessionRepository;
import com.hamburg.backend.service.UserService;
import com.hamburg.backend.security.jwt.JwtUtils;
import com.hamburg.backend.security.UserDetailsImpl;
import com.hamburg.backend.security.UserDetailsServiceImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerEditPlayerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    private EditPlayerRequest editPlayerRequest;
    private Session adminSession;
    private String validJwtToken;

    @BeforeEach
    void setUp() {
        editPlayerRequest = new EditPlayerRequest();
        editPlayerRequest.setFirstName("Juan");
        editPlayerRequest.setLastName("Pérez");
        editPlayerRequest.setPassword("newPassword123");
        editPlayerRequest.setPlayerCategory("Professional");
        editPlayerRequest.setStatus("ACTIVE");

        // Generate a valid JWT token
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "admin", null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));
        validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        
        adminSession = new Session();
        adminSession.setId(1L);
        adminSession.setToken(validJwtToken);
        adminSession.setActive(true);
        adminSession.setCreationDate(LocalDateTime.now());
        adminSession.setExpirationDate(LocalDateTime.now().plusHours(24));
        adminSession.setUser(createMockUser());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditPlayer_Success() throws Exception {
        // Arrange
        String playerUuid = "test-player-uuid";
        String authHeader = "Bearer " + validJwtToken;
        
        when(sessionRepository.findByTokenAndActiveTrue(validJwtToken))
            .thenReturn(Optional.of(adminSession));
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
            UserDetailsImpl.build(createMockUser()));
        when(userService.editPlayer(anyString(), any(EditPlayerRequest.class)))
            .thenReturn("Jugador actualizado exitosamente");

        // Act & Assert
        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editPlayerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Jugador actualizado exitosamente"));
    }

    private User createMockUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@test.com");
        user.setPassword("password");
        
        Role adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);
        
        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        user.setRoles(roles);
        
        return user;
    }

    private User createMockPlayerUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername("player");
        user.setEmail("player@test.com");
        user.setPassword("password");
        
        Role playerRole = new Role();
        playerRole.setName(ERole.ROLE_PLAYER);
        
        Set<Role> roles = new HashSet<>();
        roles.add(playerRole);
        user.setRoles(roles);
        
        return user;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditPlayer_InvalidSession() throws Exception {
        // Arrange
        String playerUuid = "test-player-uuid";
        String authHeader = "Bearer " + validJwtToken;
        
        when(sessionRepository.findByTokenAndActiveTrue(validJwtToken))
            .thenReturn(Optional.empty());
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
            UserDetailsImpl.build(createMockUser()));

        // Act & Assert
        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer " + validJwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editPlayerRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sesión inválida o expirada"));
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    void testEditPlayer_InsufficientPermissions() throws Exception {
        // Arrange
        String playerUuid = "test-player-uuid";
        String authHeader = "Bearer player-token";
        
        Session playerSession = new Session();
        playerSession.setToken("player-token");
        playerSession.setActive(true);
        playerSession.setCreationDate(LocalDateTime.now());
        playerSession.setExpirationDate(LocalDateTime.now().plusHours(24));
        playerSession.setUser(createMockPlayerUser());
        
        when(sessionRepository.findByTokenAndActiveTrue("player-token"))
            .thenReturn(Optional.of(playerSession));
        when(jwtUtils.validateJwtToken("player-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("player-token")).thenReturn("player");
        when(userDetailsService.loadUserByUsername("player")).thenReturn(
            UserDetailsImpl.build(createMockPlayerUser()));

        // Act & Assert
        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editPlayerRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditPlayer_PlayerNotFound() throws Exception {
        // Arrange
        String playerUuid = "non-existent-uuid";
        String authHeader = "Bearer " + validJwtToken;
        
        when(sessionRepository.findByTokenAndActiveTrue(validJwtToken))
            .thenReturn(Optional.of(adminSession));
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("admin");
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(
            UserDetailsImpl.build(createMockUser()));
        when(userService.editPlayer(anyString(), any(EditPlayerRequest.class)))
            .thenThrow(new RuntimeException("Jugador no encontrado"));

        // Act & Assert
        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(editPlayerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Jugador no encontrado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEditPlayer_InvalidRequestData() throws Exception {
        // Arrange
        String playerUuid = "test-player-uuid";
        String authHeader = "Bearer " + validJwtToken;
        
        EditPlayerRequest invalidRequest = new EditPlayerRequest();
        // Dejar campos vacíos para probar validación
        
        when(sessionRepository.findByTokenAndActiveTrue(validJwtToken))
            .thenReturn(Optional.of(adminSession));

        // Act & Assert
        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}