package com.hamburg.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResponse {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String playerCategory;
    private String status;
    private String uuid;
}