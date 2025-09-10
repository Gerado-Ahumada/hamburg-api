package com.hamburg.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameActivityResponse {
    private Long id;
    private LocalDateTime gameDate;
    private String userUuid;
    private String username;
    private String userFirstName;
    private String userLastName;
}