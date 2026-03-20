package com.example.soccermanagement.match.application.port;

import java.util.UUID;

public interface TeamLookupPort {
    boolean existsById(UUID teamId);
}
