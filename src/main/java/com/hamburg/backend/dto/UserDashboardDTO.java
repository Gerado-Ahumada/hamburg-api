package com.hamburg.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardDTO {
    
    private UserInfo user;
    private GameStats gameStats;
    private List<GameActivityDTO> gameActivities;
    private BenefitInfo benefits;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String uuid;
        private String username;
        private String email;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameStats {
        private Long totalGames;
        private Long gamesThisMonth;
        private Long gamesThisYear;
        private LocalDateTime lastGameDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenefitInfo {
        private List<BenefitDTO> available;
        private NextBenefit nextBenefit;
        private Long totalClaimed;
        private Long totalActive;
        
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NextBenefit {
            private String name;
            private String description;
            private Integer gamesRequired;
            private Integer gamesRemaining;
            private Integer costLevel;
        }
    }
}