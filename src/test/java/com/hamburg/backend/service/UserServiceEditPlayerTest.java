package com.hamburg.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hamburg.backend.dto.EditPlayerRequest;
import com.hamburg.backend.dto.MessageResponse;
import com.hamburg.backend.model.ERole;
import com.hamburg.backend.model.EStatus;
import com.hamburg.backend.model.Role;
import com.hamburg.backend.model.Status;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.StatusRepository;
import com.hamburg.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceEditPlayerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testPlayer;
    private EditPlayerRequest editPlayerRequest;
    private Status activeStatus;
    private Status inactiveStatus;
    private Role playerRole;

    @BeforeEach
    void setUp() {
        // Setup test player
        testPlayer = new User();
        testPlayer.setId(1L);
        testPlayer.setUuid("test-player-uuid");
        testPlayer.setUsername("testplayer");
        testPlayer.setFirstName("Test");
        testPlayer.setLastName("Player");
        testPlayer.setEmail("test@example.com");
        testPlayer.setPassword("encodedPassword");
        testPlayer.setPlayerCategory("Beginner");

        // Setup roles and status
        playerRole = new Role(ERole.ROLE_PLAYER);
        playerRole.setId(2L);
        
        activeStatus = new Status(EStatus.ACTIVE);
        activeStatus.setId(1L);
        
        inactiveStatus = new Status(EStatus.INACTIVE);
        inactiveStatus.setId(2L);
        
        testPlayer.setStatus(activeStatus);
        testPlayer.setRoles(Set.of(playerRole));

        // Setup edit request
        editPlayerRequest = new EditPlayerRequest();
        editPlayerRequest.setFirstName("Updated");
        editPlayerRequest.setLastName("Player");
        editPlayerRequest.setPassword("newpassword123");
        editPlayerRequest.setPlayerCategory("Advanced");
        editPlayerRequest.setStatus("ACTIVE");
    }

    @Test
    void testEditPlayer_Success() {
        // Arrange
        String playerUuid = "test-player-uuid";
        
        when(userRepository.findByUuid(playerUuid))
            .thenReturn(Optional.of(testPlayer));
        when(statusRepository.findByName(EStatus.ACTIVE))
            .thenReturn(Optional.of(activeStatus));
        when(passwordEncoder.encode(anyString()))
            .thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(testPlayer);

        // Act
        String response = userService.editPlayer(playerUuid, editPlayerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Jugador actualizado exitosamente", response);
        
        verify(userRepository).findByUuid(playerUuid);
        verify(statusRepository).findByName(EStatus.ACTIVE);
        verify(passwordEncoder).encode("newpassword123");
        verify(userRepository).save(testPlayer);
    }

    @Test
    void testEditPlayer_PlayerNotFound() {
        // Arrange
        String playerUuid = "non-existent-uuid";
        
        when(userRepository.findByUuid(playerUuid))
            .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.editPlayer("non-existent-uuid", editPlayerRequest);
        });
        assertEquals("Usuario no encontrado con UUID: non-existent-uuid", exception.getMessage());
        verify(userRepository).findByUuid("non-existent-uuid");
    }

    @Test
    void testEditPlayer_StatusNotFound() {
        // Arrange
        String playerUuid = "test-player-uuid";
        editPlayerRequest.setStatus("INVALID_STATUS");
        
        when(userRepository.findByUuid(playerUuid))
            .thenReturn(Optional.of(testPlayer));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.editPlayer(playerUuid, editPlayerRequest);
        });
        assertEquals("Status inválido: INVALID_STATUS. Valores permitidos: ACTIVE, INACTIVE", exception.getMessage());
        verify(userRepository).findByUuid(playerUuid);
    }

    @Test
    void testEditPlayer_WithoutPassword() {
        // Arrange
        String playerUuid = "test-player-uuid";
        editPlayerRequest.setPassword(null); // No password update
        
        when(userRepository.findByUuid(playerUuid))
            .thenReturn(Optional.of(testPlayer));
        when(statusRepository.findByName(EStatus.ACTIVE))
            .thenReturn(Optional.of(activeStatus));
        when(userRepository.save(any(User.class)))
            .thenReturn(testPlayer);

        // Act
        String response = userService.editPlayer(playerUuid, editPlayerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Jugador actualizado exitosamente", response);
        
        verify(userRepository).findByUuid(playerUuid);
        verify(statusRepository).findByName(EStatus.ACTIVE);
        verify(userRepository).save(testPlayer);
        // Verify password encoder was not called
        verify(passwordEncoder, org.mockito.Mockito.never()).encode(anyString());
    }

    @Test
    void testEditPlayer_ChangeStatusToInactive() {
        // Arrange
        String playerUuid = "test-player-uuid";
        editPlayerRequest.setStatus("INACTIVE");
        
        when(userRepository.findByUuid(playerUuid))
            .thenReturn(Optional.of(testPlayer));
        when(statusRepository.findByName(EStatus.INACTIVE))
            .thenReturn(Optional.of(inactiveStatus));
        when(passwordEncoder.encode(anyString()))
            .thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class)))
            .thenReturn(testPlayer);

        // Act
        String response = userService.editPlayer(playerUuid, editPlayerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Jugador actualizado exitosamente", response);
        
        verify(userRepository).findByUuid(playerUuid);
        verify(statusRepository).findByName(EStatus.INACTIVE);
        verify(userRepository).save(testPlayer);
    }

    @Test
    void testEditPlayer_PartialUpdate() {
        // Arrange
        String playerUuid = "test-player-uuid";
        EditPlayerRequest partialRequest = new EditPlayerRequest();
        partialRequest.setFirstName("NewFirstName");
        // Solo actualizar firstName, dejar otros campos null
        
        when(userRepository.findByUuid(playerUuid))
            .thenReturn(Optional.of(testPlayer));
        when(userRepository.save(any(User.class)))
            .thenReturn(testPlayer);

        // Act
        String response = userService.editPlayer(playerUuid, partialRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Jugador actualizado exitosamente", response);
        
        verify(userRepository).findByUuid(playerUuid);
        verify(userRepository).save(testPlayer);
    }
}