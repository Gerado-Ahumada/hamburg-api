package com.hamburg.backend.repository;

import com.hamburg.backend.model.ERol;
import com.hamburg.backend.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(ERol nombre);
}