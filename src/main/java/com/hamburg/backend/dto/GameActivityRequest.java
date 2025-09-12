package com.hamburg.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameActivityRequest {
    
    @NotBlank(message = "UUID del usuario es requerido")
    private String userUuid;
    
    @NotNull(message = "Fecha y hora del juego es requerida")
    private LocalDateTime gameDate;
}