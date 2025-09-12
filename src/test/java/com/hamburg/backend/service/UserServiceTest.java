package com.hamburg.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.hamburg.backend.dto.EditPlayerRequest;
import com.hamburg.backend.dto.PlayersPageResponse;
import com.hamburg.backend.model.ERole;
import com.hamburg.backend.model.EStatus;
import com.hamburg.backend.model.Role;
import com.hamburg.backend.model.Status;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.StatusRepository;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.security.UserDetailsImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.mockito.Mock;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private StatusRepository statusRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User playerUser1;
    private User playerUser2;
    private Role playerRole;
    private Status activeStatus;
    private Pageable pageable;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // Setup roles
        playerRole = new Role(ERole.ROLE_PLAYER);
        
        // Setup status
        activeStatus = new Status(EStatus.ACTIVE);
        
        // Setup test user for authentication
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        Set<Role> roles = new HashSet<>();
        roles.add(playerRole);
        testUser.setRoles(roles);
        testUser.setStatus(activeStatus);
        
        // Setup player users
        playerUser1 = new User();
        playerUser1.setId(2L);
        playerUser1.setUsername("player1");
        playerUser1.setEmail("player1@test.com");
        playerUser1.setFirstName("John");
        playerUser1.setLastName("Doe");
        playerUser1.setPhone("123456789");
        playerUser1.setPlayerCategory("SENIOR");
        playerUser1.setUuid("uuid-1");
        playerUser1.setRoles(Set.of(playerRole));
        playerUser1.setStatus(activeStatus);
        
        playerUser2 = new User();
        playerUser2.setId(3L);
        playerUser2.setUsername("player2");
        playerUser2.setEmail("player2@test.com");
        playerUser2.setFirstName("Jane");
        playerUser2.setLastName("Smith");
        playerUser2.setPhone("987654321");
        playerUser2.setPlayerCategory("JUNIOR");
        playerUser2.setUuid("uuid-2");
        playerUser2.setRoles(Set.of(playerRole));
        playerUser2.setStatus(activeStatus);
        
        // Setup pageable
        pageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
    }

    @Test
    public void testLoadUserByUsernameSuccess() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        
        UserDetails userDetails = userService.loadUserByUsername("testuser");
        
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
    }

    @Test
    public void testLoadUserByUsernameNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername("nonexistent");
        });
    }

    @Test
    public void testGetPlayersWithoutFilters() {
        Page<User> usersPage = new PageImpl<>(Arrays.asList(playerUser1, playerUser2), pageable, 2);
        when(userRepository.findByRole(eq(ERole.ROLE_PLAYER), any(Pageable.class)))
            .thenReturn(usersPage);
        
        PlayersPageResponse response = userService.getPlayers(0, 10, null, null);
        
        assertNotNull(response);
        assertEquals(2, response.getPlayers().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(1, response.getTotalPages());
        assertEquals(2L, response.getTotalElements());
        assertEquals(10, response.getSize());
        assertEquals(false, response.isHasNext());
        assertEquals(false, response.isHasPrevious());
        
        // Verify player data
        assertEquals("player1", response.getPlayers().get(0).getUsername());
        assertEquals("John Doe", response.getPlayers().get(0).getFullName());
        assertEquals("player1@test.com", response.getPlayers().get(0).getEmail());
        assertEquals("SENIOR", response.getPlayers().get(0).getPlayerCategory());
        assertEquals("ACTIVE", response.getPlayers().get(0).getStatus());
    }

    @Test
    public void testGetPlayersWithNameFilter() {
        Page<User> usersPage = new PageImpl<>(Arrays.asList(playerUser1), pageable, 1);
        when(userRepository.findByRoleAndNameContaining(eq(ERole.ROLE_PLAYER), eq("John"), any(Pageable.class)))
            .thenReturn(usersPage);
        
        PlayersPageResponse response = userService.getPlayers(0, 10, "John", null);
        
        assertNotNull(response);
        assertEquals(1, response.getPlayers().size());
        assertEquals("John Doe", response.getPlayers().get(0).getFullName());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    public void testGetPlayersWithCategoryFilter() {
        Page<User> usersPage = new PageImpl<>(Arrays.asList(playerUser2), pageable, 1);
        when(userRepository.findByRoleAndPlayerCategory(eq(ERole.ROLE_PLAYER), eq("JUNIOR"), any(Pageable.class)))
            .thenReturn(usersPage);
        
        PlayersPageResponse response = userService.getPlayers(0, 10, null, "JUNIOR");
        
        assertNotNull(response);
        assertEquals(1, response.getPlayers().size());
        assertEquals("JUNIOR", response.getPlayers().get(0).getPlayerCategory());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    public void testGetPlayersWithBothFilters() {
        Page<User> usersPage = new PageImpl<>(Arrays.asList(playerUser1), pageable, 1);
        when(userRepository.findByRoleAndNameContainingAndPlayerCategory(
            eq(ERole.ROLE_PLAYER), eq("John"), eq("SENIOR"), any(Pageable.class)))
            .thenReturn(usersPage);
        
        PlayersPageResponse response = userService.getPlayers(0, 10, "John", "SENIOR");
        
        assertNotNull(response);
        assertEquals(1, response.getPlayers().size());
        assertEquals("John Doe", response.getPlayers().get(0).getFullName());
        assertEquals("SENIOR", response.getPlayers().get(0).getPlayerCategory());
    }

    @Test
    public void testGetPlayersWithInvalidPagination() {
        Page<User> usersPage = new PageImpl<>(Arrays.asList(playerUser1, playerUser2), 
            PageRequest.of(0, 10, Sort.by("firstName").ascending()), 2);
        when(userRepository.findByRole(eq(ERole.ROLE_PLAYER), any(Pageable.class)))
            .thenReturn(usersPage);
        
        // Test negative page
        PlayersPageResponse response = userService.getPlayers(-1, 10, null, null);
        assertNotNull(response);
        assertEquals(0, response.getCurrentPage());
        
        // Test zero size
        response = userService.getPlayers(0, 0, null, null);
        assertNotNull(response);
        assertEquals(10, response.getSize());
    }

    @Test
    public void testGetPlayersEmptyResult() {
        Page<User> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        when(userRepository.findByRole(eq(ERole.ROLE_PLAYER), any(Pageable.class)))
            .thenReturn(emptyPage);
        
        PlayersPageResponse response = userService.getPlayers(0, 10, null, null);
        
        assertNotNull(response);
        assertEquals(0, response.getPlayers().size());
        assertEquals(0L, response.getTotalElements());
    }

    @Test
    public void testGetPlayersWithNullStatus() {
        // Create user with null status
        User userWithNullStatus = new User();
        userWithNullStatus.setId(4L);
        userWithNullStatus.setUsername("player3");
        userWithNullStatus.setEmail("player3@test.com");
        userWithNullStatus.setFirstName("Bob");
        userWithNullStatus.setLastName("Johnson");
        userWithNullStatus.setUuid("uuid-3");
        userWithNullStatus.setStatus(null); // Null status
        
        Page<User> usersPage = new PageImpl<>(Arrays.asList(userWithNullStatus), pageable, 1);
        when(userRepository.findByRole(eq(ERole.ROLE_PLAYER), any(Pageable.class)))
            .thenReturn(usersPage);
        
        PlayersPageResponse response = userService.getPlayers(0, 10, null, null);
        
        assertNotNull(response);
        assertEquals(1, response.getPlayers().size());
        assertEquals("UNKNOWN", response.getPlayers().get(0).getStatus());
    }

    @Test
    public void testGetPlayersWithWhitespaceFilters() {
        Page<User> usersPage = new PageImpl<>(Arrays.asList(playerUser1), pageable, 1);
        when(userRepository.findByRoleAndNameContainingAndPlayerCategory(
            eq(ERole.ROLE_PLAYER), eq("John"), eq("SENIOR"), any(Pageable.class)))
            .thenReturn(usersPage);
        
        // Test with whitespace that should be trimmed
        PlayersPageResponse response = userService.getPlayers(0, 10, "  John  ", "  SENIOR  ");
        
        assertNotNull(response);
        assertEquals(1, response.getPlayers().size());
    }

    // Tests for editPlayer method
    @Test
    public void testEditPlayerSuccess() {
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");
        request.setLastName("Updated Doe");
        request.setPassword("newpassword");
        request.setPlayerCategory("JUNIOR");
        request.setStatus("ACTIVE");
        
        Status newStatus = new Status(EStatus.ACTIVE);
        
        when(userRepository.findByUuid("uuid-1")).thenReturn(Optional.of(playerUser1));
        when(statusRepository.findByName(EStatus.ACTIVE)).thenReturn(Optional.of(newStatus));
        when(passwordEncoder.encode("newpassword")).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(playerUser1);
        
        String result = userService.editPlayer("uuid-1", request);
        
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("Jugador actualizado exitosamente", result);
        assertEquals("Updated John", playerUser1.getFirstName());
        assertEquals("Updated Doe", playerUser1.getLastName());
        assertEquals("JUNIOR", playerUser1.getPlayerCategory());
    }

    @Test
    public void testEditPlayerUserNotFound() {
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");
        
        when(userRepository.findByUuid("non-existent-uuid")).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.editPlayer("non-existent-uuid", request);
        });
        
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    public void testEditPlayerUserNotPlayer() {
        // Create admin user
        User adminUser = new User();
        adminUser.setId(5L);
        adminUser.setUsername("admin");
        adminUser.setUuid("admin-uuid");
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        adminUser.setRoles(Set.of(adminRole));
        
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated Admin");
        
        when(userRepository.findByUuid("admin-uuid")).thenReturn(Optional.of(adminUser));
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.editPlayer("admin-uuid", request);
        });
        
        assertEquals("User is not a player", exception.getMessage());
    }

    @Test
    public void testEditPlayerInvalidStatus() {
        EditPlayerRequest request = new EditPlayerRequest();
        request.setStatus("INVALID_STATUS");
        
        when(userRepository.findByUuid("uuid-1")).thenReturn(Optional.of(playerUser1));
        when(statusRepository.findByName(any())).thenReturn(Optional.empty());
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userService.editPlayer("uuid-1", request);
        });
        
        assertEquals("Invalid status: INVALID_STATUS", exception.getMessage());
    }

    @Test
    public void testEditPlayerPartialUpdate() {
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Only First Name");
        // Only updating first name, other fields should remain unchanged
        
        String originalLastName = playerUser1.getLastName();
        String originalCategory = playerUser1.getPlayerCategory();
        
        when(userRepository.findByUuid("uuid-1")).thenReturn(Optional.of(playerUser1));
        when(userRepository.save(any(User.class))).thenReturn(playerUser1);
        
        String result = userService.editPlayer("uuid-1", request);
        
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("Jugador actualizado exitosamente", result);
        assertEquals("Only First Name", playerUser1.getFirstName());
        assertEquals(originalLastName, playerUser1.getLastName());
        assertEquals(originalCategory, playerUser1.getPlayerCategory());
    }

    @Test
    public void testEditPlayerEmptyRequest() {
        EditPlayerRequest request = new EditPlayerRequest();
        // Empty request - no fields to update
        
        when(userRepository.findByUuid("uuid-1")).thenReturn(Optional.of(playerUser1));
        when(userRepository.save(any(User.class))).thenReturn(playerUser1);
        
        String result = userService.editPlayer("uuid-1", request);
        
        verify(userRepository, times(1)).save(any(User.class));
        assertEquals("Jugador actualizado exitosamente", result);
    }
}