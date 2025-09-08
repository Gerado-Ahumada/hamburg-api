package com.hamburg.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "statuses")
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EStatus name;
    
    public Status() {
    }
    
    public Status(EStatus name) {
        this.name = name;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public EStatus getName() {
        return name;
    }
    
    public void setName(EStatus name) {
        this.name = name;
    }
}