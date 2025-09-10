package com.hamburg.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameActivityRangeResponse {
    private int total_game_activity;
    private List<GameActivityResponse> activities;
}