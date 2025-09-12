package com.hamburg.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sessions")
public class Session extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime creationDate;
    
    @Column(nullable = false)
    private LocalDateTime expirationDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public Session(String token, LocalDateTime creationDate, LocalDateTime expirationDate, User user) {
        this.token = token;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.user = user;
        this.active = true;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationDate);
    }
    
    public void deactivate() {
        this.active = false;
    }
}