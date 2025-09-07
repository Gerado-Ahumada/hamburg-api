package com.hamburg.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
    
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;
    
    @NotBlank
    @Size(max = 50)
    private String nombre;
    
    @NotBlank
    @Size(max = 50)
    private String apellido;
    
    @Size(max = 20)
    private String telefono;
    
    @Size(max = 50)
    private String categoriaJugador;
    
    private Set<String> rol;
    
    public SignupRequest() {
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getCategoriaJugador() {
        return categoriaJugador;
    }
    
    public void setCategoriaJugador(String categoriaJugador) {
        this.categoriaJugador = categoriaJugador;
    }
    
    public Set<String> getRol() {
        return rol;
    }
    
    public void setRol(Set<String> rol) {
        this.rol = rol;
    }
}