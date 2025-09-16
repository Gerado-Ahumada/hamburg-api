package com.hamburg.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hamburg.backend.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_benefits")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBenefit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid", referencedColumnName = "uuid", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password", "email"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "benefit_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Benefit benefit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BenefitStatus status = BenefitStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "claimed_at")
    private LocalDateTime claimedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum BenefitStatus {
        ACTIVE,
        CLAIMED,
        INACTIVE
    }
}