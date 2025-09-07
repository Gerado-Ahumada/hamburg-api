package com.hamburg.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hamburg.backend.model.Sesion;
import com.hamburg.backend.model.Usuario;

@Repository
public interface SesionRepository extends JpaRepository<Sesion, Long> {
    
    Optional<Sesion> findByTokenAndActivaTrue(String token);
    
    List<Sesion> findByUsuarioAndActivaTrue(Usuario usuario);
    
    @Modifying
    @Transactional
    @Query("UPDATE Sesion s SET s.activa = false WHERE s.usuario = :usuario")
    void desactivarSesionesPorUsuario(@Param("usuario") Usuario usuario);
    
    @Modifying
    @Transactional
    @Query("UPDATE Sesion s SET s.activa = false WHERE s.fechaExpiracion < :fechaActual")
    void desactivarSesionesExpiradas(@Param("fechaActual") LocalDateTime fechaActual);
    
    @Query("SELECT COUNT(s) FROM Sesion s WHERE s.usuario = :usuario AND s.activa = true")
    Long contarSesionesActivasPorUsuario(@Param("usuario") Usuario usuario);
    
    Boolean existsByTokenAndActivaTrue(String token);
}