package com.example.soccermanagement.match.application.port;

import java.util.Optional;
import java.util.UUID;

public interface TeamLookupPort {
    boolean existsById(UUID teamId);
    Optional<String> findNameById(UUID teamId);
    Optional<String> findExternalIdById(UUID teamId);
}
