package com.hamburg.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.hamburg.backend.dto.LoginRequest;
import com.hamburg.backend.dto.LoginResponse;
import com.hamburg.backend.model.User;
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
    }

    @Test
    public void testAutenticar() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("admin");
        
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        LoginResponse response = authService.autenticar(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());
    }
}