package com.hamburg.backend.repository;

import com.hamburg.backend.model.EEstado;
import com.hamburg.backend.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoRepository extends JpaRepository<Estado, Long> {
    Optional<Estado> findByNombre(EEstado nombre);
}