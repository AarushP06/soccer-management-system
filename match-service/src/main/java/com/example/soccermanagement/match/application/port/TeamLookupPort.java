package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

public interface TeamLookupPort {
    boolean existsById(UUID teamId);
    Optional<String> findNameById(UUID teamId);
    Optional<String> findExternalIdById(UUID teamId);
    Optional<com.example.soccermanagement.match.application.dto.TeamInfo> findByName(String name);
}

