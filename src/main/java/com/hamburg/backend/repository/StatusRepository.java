package com.hamburg.backend.repository;

import com.hamburg.backend.model.EStatus;
import com.hamburg.backend.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {
    Optional<Status> findByName(EStatus name);
}