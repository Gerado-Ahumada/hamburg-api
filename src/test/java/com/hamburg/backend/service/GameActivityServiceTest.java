package com.hamburg.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hamburg.backend.dto.GameActivityRequest;
import com.hamburg.backend.dto.GameActivityResponse;
import com.hamburg.backend.dto.GameActivityRangeResponse;
import com.hamburg.backend.model.GameActivity;
import com.hamburg.backend.model.User;
import com.hamburg.backend.repository.GameActivityRepository;
import com.hamburg.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class GameActivityServiceTest {

    @Mock
    private GameActivityRepository gameActivityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GameActivityService gameActivityService;

    private User testUser;
    private GameActivity testGameActivity;
    private GameActivityRequest testRequest;
    private LocalDateTime testGameDate;

    @BeforeEach
    public void setup() {
        testGameDate = LocalDateTime.of(2024, 1, 15, 10, 30);
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setUuid("test-uuid-123");
        testUser.setUsername("testplayer");
        testUser.setFirstName("Test");
        testUser.setLastName("Player");
        
        testGameActivity = new GameActivity();
        testGameActivity.setId(1L);
        testGameActivity.setGameDate(testGameDate);
        testGameActivity.setUser(testUser);
        
        testRequest = new GameActivityRequest();
        testRequest.setUserUuid("test-uuid-123");
        testRequest.setGameDate(testGameDate);
    }

    @Test
    public void testRegisterGameActivitySuccess() {
        // Arrange
        when(userRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(testUser));
        when(gameActivityRepository.save(any(GameActivity.class))).thenReturn(testGameActivity);

        // Act
        GameActivity result = gameActivityService.registerGameActivity(testRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(testGameDate, result.getGameDate());
        assertEquals(testUser, result.getUser());
        
        verify(userRepository).findByUuid("test-uuid-123");
        verify(gameActivityRepository).save(any(GameActivity.class));
    }

    @Test
    public void testRegisterGameActivityUserNotFound() {
        // Arrange
        when(userRepository.findByUuid("nonexistent-uuid")).thenReturn(Optional.empty());
        
        GameActivityRequest invalidRequest = new GameActivityRequest();
        invalidRequest.setUserUuid("nonexistent-uuid");
        invalidRequest.setGameDate(testGameDate);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameActivityService.registerGameActivity(invalidRequest);
        });
        
        assertEquals("Usuario no encontrado con UUID: nonexistent-uuid", exception.getMessage());
        verify(userRepository).findByUuid("nonexistent-uuid");
    }

    @Test
    public void testGetGameActivitiesByUserUuidSuccess() {
        // Arrange
        GameActivity activity1 = new GameActivity();
        activity1.setId(1L);
        activity1.setGameDate(testGameDate);
        activity1.setUser(testUser);
        
        GameActivity activity2 = new GameActivity();
        activity2.setId(2L);
        activity2.setGameDate(testGameDate.plusDays(1));
        activity2.setUser(testUser);
        
        List<GameActivity> activities = Arrays.asList(activity1, activity2);
        
        when(userRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(testUser));
        when(gameActivityRepository.findByUserOrderByGameDateDesc(testUser)).thenReturn(activities);

        // Act
        List<GameActivityResponse> result = gameActivityService.getGameActivitiesByUserUuid("test-uuid-123");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        
        GameActivityResponse response1 = result.get(0);
        assertEquals(1L, response1.getId());
        assertEquals(testGameDate, response1.getGameDate());
        assertEquals("test-uuid-123", response1.getUserUuid());
        assertEquals("testplayer", response1.getUsername());
        assertEquals("Test", response1.getUserFirstName());
        assertEquals("Player", response1.getUserLastName());
        
        GameActivityResponse response2 = result.get(1);
        assertEquals(2L, response2.getId());
        assertEquals(testGameDate.plusDays(1), response2.getGameDate());
        
        verify(userRepository).findByUuid("test-uuid-123");
        verify(gameActivityRepository).findByUserOrderByGameDateDesc(testUser);
    }

    @Test
    public void testGetGameActivitiesByUserUuidEmptyResult() {
        // Arrange
        when(userRepository.findByUuid("test-uuid-123")).thenReturn(Optional.of(testUser));
        when(gameActivityRepository.findByUserOrderByGameDateDesc(testUser)).thenReturn(Collections.emptyList());

        // Act
        List<GameActivityResponse> result = gameActivityService.getGameActivitiesByUserUuid("test-uuid-123");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(userRepository).findByUuid("test-uuid-123");
        verify(gameActivityRepository).findByUserOrderByGameDateDesc(testUser);
    }

    @Test
    public void testGetGameActivitiesByUserUuidUserNotFound() {
        // Arrange
        when(userRepository.findByUuid("nonexistent-uuid")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            gameActivityService.getGameActivitiesByUserUuid("nonexistent-uuid");
        });
        
        assertEquals("Usuario no encontrado con UUID: nonexistent-uuid", exception.getMessage());
        verify(userRepository).findByUuid("nonexistent-uuid");
    }

    @Test
    public void testGetGameActivitiesByUserUuidAndRangeSuccess() {
        // Arrange
        List<GameActivity> activities = Arrays.asList(testGameActivity);
        
        when(gameActivityRepository.findByUserUuidAndYearAndMonthOrderByGameDateDesc("test-uuid-123", 2024, 1))
            .thenReturn(activities);

        // Act
        GameActivityRangeResponse result = gameActivityService.getGameActivitiesByUserUuidAndRange("test-uuid-123", 2024, 1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotal_game_activity());
        assertEquals(1, result.getActivities().size());
        
        GameActivityResponse activityResponse = result.getActivities().get(0);
        assertEquals(1L, activityResponse.getId());
        assertEquals(testGameDate, activityResponse.getGameDate());
        assertEquals("test-uuid-123", activityResponse.getUserUuid());
        assertEquals("testplayer", activityResponse.getUsername());
        assertEquals("Test", activityResponse.getUserFirstName());
        assertEquals("Player", activityResponse.getUserLastName());
        
        verify(gameActivityRepository).findByUserUuidAndYearAndMonthOrderByGameDateDesc("test-uuid-123", 2024, 1);
    }

    @Test
    public void testGetGameActivitiesByUserUuidAndRangeEmptyResult() {
        // Arrange
        when(gameActivityRepository.findByUserUuidAndYearAndMonthOrderByGameDateDesc("test-uuid-123", 2024, 1))
            .thenReturn(Collections.emptyList());

        // Act
        GameActivityRangeResponse result = gameActivityService.getGameActivitiesByUserUuidAndRange("test-uuid-123", 2024, 1);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotal_game_activity());
        assertTrue(result.getActivities().isEmpty());
        
        verify(gameActivityRepository).findByUserUuidAndYearAndMonthOrderByGameDateDesc("test-uuid-123", 2024, 1);
    }

    @Test
    public void testGetGameActivitiesByUserUuidAndRangeMultipleActivities() {
        // Arrange
        GameActivity activity1 = new GameActivity();
        activity1.setId(1L);
        activity1.setGameDate(LocalDateTime.of(2024, 1, 15, 10, 30));
        activity1.setUser(testUser);
        
        GameActivity activity2 = new GameActivity();
        activity2.setId(2L);
        activity2.setGameDate(LocalDateTime.of(2024, 1, 20, 14, 45));
        activity2.setUser(testUser);
        
        List<GameActivity> activities = Arrays.asList(activity1, activity2);
        
        when(gameActivityRepository.findByUserUuidAndYearAndMonthOrderByGameDateDesc("test-uuid-123", 2024, 1))
            .thenReturn(activities);

        // Act
        GameActivityRangeResponse result = gameActivityService.getGameActivitiesByUserUuidAndRange("test-uuid-123", 2024, 1);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotal_game_activity());
        assertEquals(2, result.getActivities().size());
        
        verify(gameActivityRepository).findByUserUuidAndYearAndMonthOrderByGameDateDesc("test-uuid-123", 2024, 1);
    }

    @Test
    public void testGetGameActivitiesByUserUuidAndDateRangeSuccess() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        List<GameActivity> activities = Arrays.asList(testGameActivity);
        
        when(gameActivityRepository.findByUserUuidAndGameDateBetween("test-uuid-123", startDate, endDate))
            .thenReturn(activities);

        // Act
        List<GameActivity> result = gameActivityService.getGameActivitiesByUserUuidAndDateRange("test-uuid-123", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testGameActivity, result.get(0));
        
        verify(gameActivityRepository).findByUserUuidAndGameDateBetween("test-uuid-123", startDate, endDate);
    }

    @Test
    public void testGetGameActivitiesByUserUuidAndDateRangeEmptyResult() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 2, 28, 23, 59);
        
        when(gameActivityRepository.findByUserUuidAndGameDateBetween("test-uuid-123", startDate, endDate))
            .thenReturn(Collections.emptyList());

        // Act
        List<GameActivity> result = gameActivityService.getGameActivitiesByUserUuidAndDateRange("test-uuid-123", startDate, endDate);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(gameActivityRepository).findByUserUuidAndGameDateBetween("test-uuid-123", startDate, endDate);
    }
}