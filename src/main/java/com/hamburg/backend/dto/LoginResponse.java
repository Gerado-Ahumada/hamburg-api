package com.hamburg.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    
    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String username;
    private String email;
    private String role;
}