package com.hamburg.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamburg.backend.dto.LoginRequest;
import com.hamburg.backend.dto.LoginResponse;
import com.hamburg.backend.dto.SignupRequest;
import com.hamburg.backend.exception.UnauthorizedRoleException;
import com.hamburg.backend.service.AuthService;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequest loginRequest;
    private LoginResponse loginResponse;
    private SignupRequest signupRequest;

    @BeforeEach
    public void setup() {
        loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");

        loginResponse = LoginResponse.builder()
                .token("jwt-token")
                .userUuid(UUID.randomUUID().toString())
                .username("admin")
                .email("admin@hamburg.com")
                .role("ROLE_ADMIN")
                .build();

        signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@hamburg.com");
        signupRequest.setPassword("password123");
        signupRequest.setFirstName("New");
        signupRequest.setLastName("User");
        signupRequest.setPhone("123456789");
        signupRequest.setPlayerCategory("Amateur");
    }

    @Test
    public void testLoginSuccess() throws Exception {
        when(authService.autenticar(any(LoginRequest.class))).thenReturn(loginResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    public void testLoginFailure() throws Exception {
        when(authService.autenticar(any(LoginRequest.class)))
            .thenThrow(new RuntimeException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.username").value("Error en autenticación: Credenciales inválidas"));
    }

    @Test
    public void testGetTestEndpoint() throws Exception {
        mockMvc.perform(get("/api/auth/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("API funcionando correctamente"));
    }

    @Test
    public void testSignupSuccess() throws Exception {
        doNothing().when(authService).validateAdminRole("valid-token");
        when(authService.registrarUsuario(any(SignupRequest.class)))
            .thenReturn("Usuario registrado exitosamente");

        mockMvc.perform(post("/api/auth/signup")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Usuario registrado exitosamente"));
    }

    @Test
    public void testSignupWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de autorización requerido"));
    }

    @Test
    public void testSignupWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                .header("Authorization", "InvalidToken")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token de autorización requerido"));
    }

    @Test
    public void testSignupUnauthorizedRole() throws Exception {
        doThrow(new UnauthorizedRoleException("No tienes permisos de administrador"))
            .when(authService).validateAdminRole("player-token");

        mockMvc.perform(post("/api/auth/signup")
                .header("Authorization", "Bearer player-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No tienes permisos de administrador"));
    }

    @Test
    public void testSignupServiceError() throws Exception {
        doNothing().when(authService).validateAdminRole("valid-token");
        when(authService.registrarUsuario(any(SignupRequest.class)))
            .thenReturn("Error: Usuario ya existe");

        mockMvc.perform(post("/api/auth/signup")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Usuario ya existe"));
    }

    @Test
    public void testSignupWithInvalidData() throws Exception {
        SignupRequest invalidRequest = new SignupRequest();
        // Missing required fields
        
        mockMvc.perform(post("/api/auth/signup")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLogoutSuccess() throws Exception {
        doNothing().when(authService).cerrarSesion("valid-token");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cerrada exitosamente"));
    }

    @Test
    public void testLogoutWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token no proporcionado"));
    }

    @Test
    public void testLogoutWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "InvalidToken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token no proporcionado"));
    }

    @Test
    public void testLogoutServiceError() throws Exception {
        doThrow(new RuntimeException("Error al invalidar token"))
            .when(authService).cerrarSesion("valid-token");

        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error al cerrar sesión: Error al invalidar token"));
    }
}