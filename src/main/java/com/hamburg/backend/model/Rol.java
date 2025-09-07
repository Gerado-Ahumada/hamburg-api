package com.hamburg.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Rol {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERol nombre;
    
    public Rol() {
    }
    
    public Rol(ERol nombre) {
        this.nombre = nombre;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public ERol getNombre() {
        return nombre;
    }
    
    public void setNombre(ERol nombre) {
        this.nombre = nombre;
    }
}