package com.hamburg.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamburg.backend.dto.GameActivityRequest;
import com.hamburg.backend.dto.GameActivityResponse;
import com.hamburg.backend.dto.GameActivityRangeResponse;
import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.UserRepository;
import com.hamburg.backend.service.GameActivityService;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class GameActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameActivityService gameActivityService;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private GameActivityRequest gameActivityRequest;
    private GameActivity gameActivity;
    private GameActivityResponse gameActivityResponse;
    private GameActivityRangeResponse gameActivityRangeResponse;
    private User testUser;

    @BeforeEach
    public void setup() {
        LocalDateTime gameDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        
        gameActivityRequest = new GameActivityRequest();
        gameActivityRequest.setUserUuid("test-uuid-123");
        gameActivityRequest.setGameDate(gameDate);
        
        gameActivity = new GameActivity();
        gameActivity.setId(1L);
        gameActivity.setGameDate(gameDate);
        gameActivity.setUserUuid("test-uuid-123");
        
        gameActivityResponse = new GameActivityResponse();
        gameActivityResponse.setId(1L);
        gameActivityResponse.setGameDate(gameDate);
        gameActivityResponse.setUserUuid("test-uuid-123");
        gameActivityResponse.setUsername("testplayer");
        gameActivityResponse.setUserFirstName("Test");
        gameActivityResponse.setUserLastName("Player");
        
        gameActivityRangeResponse = new GameActivityRangeResponse();
        gameActivityRangeResponse.setTotal_game_activity(2);
        gameActivityRangeResponse.setActivities(Arrays.asList(gameActivityResponse));
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testplayer");
        testUser.setUuid("test-uuid-123");
        testUser.setFirstName("Test");
        testUser.setLastName("Player");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testRegisterGameActivitySuccess() throws Exception {
        when(gameActivityService.registerGameActivity(any(GameActivityRequest.class)))
            .thenReturn(gameActivity);

        mockMvc.perform(post("/api/game-activity/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameActivityRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Actividad de juego registrada exitosamente"))
                .andExpect(jsonPath("$.activityId").value(1))
                .andExpect(jsonPath("$.userUuid").value("test-uuid-123"))
                .andExpect(jsonPath("$.gameDate").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testRegisterGameActivityWithInvalidData() throws Exception {
        GameActivityRequest invalidRequest = new GameActivityRequest();
        // Missing required fields
        
        mockMvc.perform(post("/api/game-activity/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testRegisterGameActivityServiceException() throws Exception {
        when(gameActivityService.registerGameActivity(any(GameActivityRequest.class)))
            .thenThrow(new RuntimeException("Usuario no encontrado"));

        mockMvc.perform(post("/api/game-activity/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameActivityRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testRegisterGameActivityInternalError() throws Exception {
        when(gameActivityService.registerGameActivity(any(GameActivityRequest.class)))
            .thenThrow(new Exception("Database error"));

        mockMvc.perform(post("/api/game-activity/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameActivityRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Error interno del servidor"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserActivitiesSuccess() throws Exception {
        when(gameActivityService.getGameActivitiesByUserUuid("test-uuid-123"))
            .thenReturn(Arrays.asList(gameActivityResponse));

        mockMvc.perform(get("/api/game-activity/user/test-uuid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userUuid").value("test-uuid-123"))
                .andExpect(jsonPath("$[0].username").value("testplayer"))
                .andExpect(jsonPath("$[0].userFirstName").value("Test"))
                .andExpect(jsonPath("$[0].userLastName").value("Player"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserActivitiesEmpty() throws Exception {
        when(gameActivityService.getGameActivitiesByUserUuid("test-uuid-123"))
            .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/game-activity/user/test-uuid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserActivitiesServiceException() throws Exception {
        when(gameActivityService.getGameActivitiesByUserUuid(anyString()))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/game-activity/user/test-uuid-123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetGameActivitiesByUserAndRangeSuccess() throws Exception {
        when(gameActivityService.getGameActivitiesByUserUuidAndRange("test-uuid-123", 2024, 1))
            .thenReturn(gameActivityRangeResponse);

        mockMvc.perform(get("/api/game-activity/user/test-uuid-123/range")
                .param("year", "2024")
                .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_game_activity").value(2))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities.length()").value(1))
                .andExpect(jsonPath("$.activities[0].id").value(1))
                .andExpect(jsonPath("$.activities[0].userUuid").value("test-uuid-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetGameActivitiesByUserAndRangeEmptyResult() throws Exception {
        GameActivityRangeResponse emptyResponse = new GameActivityRangeResponse();
        emptyResponse.setTotal_game_activity(0);
        emptyResponse.setActivities(Collections.emptyList());
        
        when(gameActivityService.getGameActivitiesByUserUuidAndRange("test-uuid-123", 2024, 1))
            .thenReturn(emptyResponse);

        mockMvc.perform(get("/api/game-activity/user/test-uuid-123/range")
                .param("year", "2024")
                .param("month", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_game_activity").value(0))
                .andExpect(jsonPath("$.activities").isArray())
                .andExpect(jsonPath("$.activities.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetGameActivitiesByUserAndRangeServiceException() throws Exception {
        when(gameActivityService.getGameActivitiesByUserUuidAndRange(anyString(), anyInt(), anyInt()))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/game-activity/user/test-uuid-123/range")
                .param("year", "2024")
                .param("month", "1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserUuidSuccess() throws Exception {
        when(userRepository.findByUsername("testplayer"))
            .thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/game-activity/test/user/testplayer/uuid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testplayer"))
                .andExpect(jsonPath("$.uuid").value("test-uuid-123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testGetUserUuidUserNotFound() throws Exception {
        when(userRepository.findByUsername("nonexistent"))
            .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/game-activity/test/user/nonexistent/uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Usuario no encontrado"));
    }

    @Test
    public void testRegisterGameActivityWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/api/game-activity/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameActivityRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserActivitiesWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/game-activity/user/test-uuid-123"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testGetUserUuidWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/game-activity/test/user/testplayer/uuid"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "PLAYER")
    public void testRegisterGameActivityWithInsufficientRole() throws Exception {
        mockMvc.perform(post("/api/game-activity/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(gameActivityRequest)))
                .andExpect(status().isForbidden());
    }
}