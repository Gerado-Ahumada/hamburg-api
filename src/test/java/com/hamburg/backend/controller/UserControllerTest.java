package com.hamburg.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doAnswer;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.hamburg.backend.dto.EditPlayerRequest;
import com.hamburg.backend.dto.PlayerResponse;
import com.hamburg.backend.dto.PlayersPageResponse;
import com.hamburg.backend.service.AuthService;
import com.hamburg.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    
    @MockBean
    private AuthService authService;
    
    @Autowired
    private ObjectMapper objectMapper;

    private PlayersPageResponse playersPageResponse;
    private PlayerResponse player1;
    private PlayerResponse player2;

    @BeforeEach
    public void setup() {
        player1 = new PlayerResponse(
            "player1",
            "John Doe",
            "john@test.com",
            "123456789",
            "SENIOR",
            "ACTIVE",
            "uuid-1"
        );
        
        player2 = new PlayerResponse(
            "player2",
            "Jane Smith",
            "jane@test.com",
            "987654321",
            "JUNIOR",
            "ACTIVE",
            "uuid-2"
        );
        
        playersPageResponse = new PlayersPageResponse(
            Arrays.asList(player1, player2),
            0, // currentPage
            1, // totalPages
            2L, // totalElements
            10, // size
            false, // hasNext
            false  // hasPrevious
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersSuccess() throws Exception {
        when(userService.getPlayers(anyInt(), anyInt(), any(), any()))
            .thenReturn(playersPageResponse);

        mockMvc.perform(get("/api/users/players")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players.length()").value(2))
                .andExpect(jsonPath("$.players[0].username").value("player1"))
                .andExpect(jsonPath("$.players[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.players[0].email").value("john@test.com"))
                .andExpect(jsonPath("$.players[0].playerCategory").value("SENIOR"))
                .andExpect(jsonPath("$.players[1].username").value("player2"))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersWithNameFilter() throws Exception {
        PlayersPageResponse filteredResponse = new PlayersPageResponse(
            Collections.singletonList(player1),
            0, 1, 1L, 10, false, false
        );
        
        when(userService.getPlayers(anyInt(), anyInt(), anyString(), any()))
            .thenReturn(filteredResponse);

        mockMvc.perform(get("/api/users/players")
                .param("page", "0")
                .param("size", "10")
                .param("name", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players.length()").value(1))
                .andExpect(jsonPath("$.players[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersWithCategoryFilter() throws Exception {
        PlayersPageResponse filteredResponse = new PlayersPageResponse(
            Collections.singletonList(player2),
            0, 1, 1L, 10, false, false
        );
        
        when(userService.getPlayers(anyInt(), anyInt(), any(), anyString()))
            .thenReturn(filteredResponse);

        mockMvc.perform(get("/api/users/players")
                .param("page", "0")
                .param("size", "10")
                .param("category", "JUNIOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players.length()").value(1))
                .andExpect(jsonPath("$.players[0].playerCategory").value("JUNIOR"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersWithBothFilters() throws Exception {
        PlayersPageResponse filteredResponse = new PlayersPageResponse(
            Collections.singletonList(player1),
            0, 1, 1L, 10, false, false
        );
        
        when(userService.getPlayers(anyInt(), anyInt(), anyString(), anyString()))
            .thenReturn(filteredResponse);

        mockMvc.perform(get("/api/users/players")
                .param("page", "0")
                .param("size", "10")
                .param("name", "John")
                .param("category", "SENIOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players.length()").value(1))
                .andExpect(jsonPath("$.players[0].fullName").value("John Doe"))
                .andExpect(jsonPath("$.players[0].playerCategory").value("SENIOR"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersEmptyResult() throws Exception {
        PlayersPageResponse emptyResponse = new PlayersPageResponse(
            Collections.emptyList(),
            0, 0, 0L, 10, false, false
        );
        
        when(userService.getPlayers(anyInt(), anyInt(), any(), any()))
            .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/users/players")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.players").isArray())
                .andExpect(jsonPath("$.players.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersDefaultPagination() throws Exception {
        when(userService.getPlayers(0, 10, null, null))
            .thenReturn(playersPageResponse);

        mockMvc.perform(get("/api/users/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetPlayersServiceException() throws Exception {
        when(userService.getPlayers(anyInt(), anyInt(), any(), any()))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/users/players")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetPlayersWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/players"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    public void testGetPlayersWithInsufficientRole() throws Exception {
        mockMvc.perform(get("/api/users/players"))
                .andExpect(status().isForbidden());
    }

    // Tests for editPlayer endpoint
    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditPlayerSuccess() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");
        request.setLastName("Updated Doe");
        request.setPlayerCategory("JUNIOR");
        request.setStatus("ACTIVE");
        
        doNothing().when(authService).validateAdminRole(anyString());
        when(authService.validarSesion(anyString())).thenReturn(true);
        when(userService.editPlayer(anyString(), any(EditPlayerRequest.class))).thenReturn("Player updated successfully");

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Player updated successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditPlayerInvalidRole() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");
        
        doThrow(new RuntimeException("Access denied: Admin role required")).when(authService).validateAdminRole(anyString());

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer invalid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: Admin role required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditPlayerInvalidSession() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");
        
        doNothing().when(authService).validateAdminRole(anyString());
        when(authService.validarSesion(anyString())).thenReturn(false);

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer expired-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired session"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditPlayerUserNotFound() throws Exception {
        String playerUuid = "non-existent-uuid";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");
        
        doNothing().when(authService).validateAdminRole(anyString());
        when(authService.validarSesion(anyString())).thenReturn(true);
        when(userService.editPlayer(anyString(), any(EditPlayerRequest.class))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditPlayerMissingAuthorizationHeader() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Authorization header is required"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEditPlayerValidationError() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        // Empty request should trigger validation errors
        
        doNothing().when(authService).validateAdminRole(anyString());
        when(authService.validarSesion(anyString())).thenReturn(true);

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // Should still pass as all fields are optional
    }

    @Test
    public void testEditPlayerWithoutAuthentication() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    public void testEditPlayerWithInsufficientRole() throws Exception {
        String playerUuid = "uuid-1";
        EditPlayerRequest request = new EditPlayerRequest();
        request.setFirstName("Updated John");

        mockMvc.perform(put("/api/users/players/{uuid}", playerUuid)
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}