package com.hamburg.backend.dto;

import com.hamburg.backend.entity.UserBenefit.BenefitStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BenefitDTO {
    private Long id;
    private String name;
    private String description;
    private Integer requiredGames;
    private Integer costLevel;
    private BenefitStatus status;
    private boolean canClaim;
    private LocalDateTime activatedAt;
    private LocalDateTime claimedAt;
}