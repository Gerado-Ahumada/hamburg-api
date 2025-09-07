package com.hamburg.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estados")
public class Estado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EEstado nombre;
    
    public Estado() {
    }
    
    public Estado(EEstado nombre) {
        this.nombre = nombre;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public EEstado getNombre() {
        return nombre;
    }
    
    public void setNombre(EEstado nombre) {
        this.nombre = nombre;
    }
}