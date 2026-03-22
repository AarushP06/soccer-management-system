package com.example.soccermanagement.location.application.port;

import com.example.soccermanagement.location.domain.Stadium;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StadiumRepository {
    List<Stadium> findAll();
    Optional<Stadium> findById(UUID id);
    Stadium save(Stadium aggregate);
    void deleteById(UUID id);
    boolean existsById(UUID id);
}
