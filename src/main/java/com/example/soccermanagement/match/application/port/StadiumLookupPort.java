package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

public interface StadiumLookupPort {
    boolean existsById(UUID stadiumId);
    Optional<String> findNameById(UUID stadiumId);
    Optional<Integer> findExternalVenueIdById(UUID stadiumId);
}
