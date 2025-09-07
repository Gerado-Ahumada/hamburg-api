package com.hamburg.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Size(max = 20)
    private String username;
    
    @NotBlank
    @Size(max = 120)
    private String password;
    
    @NotBlank
    @Size(max = 50)
    private String nombre;
    
    @NotBlank
    @Size(max = 50)
    private String apellido;
    
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    @Size(max = 20)
    private String telefono;
    
    @Size(max = 50)
    private String categoriaJugador;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_roles",
               joinColumns = @JoinColumn(name = "usuario_id"),
               inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_id")
    private Estado estado;
    
    public Usuario(String username, String email, String password, String nombre, String apellido) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.nombre = nombre;
        this.apellido = apellido;
    }
}