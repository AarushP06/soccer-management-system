package com.example.soccermanagement.match.application.port;

import java.util.UUID;

public interface StadiumLookupPort {
    boolean existsById(UUID stadiumId);
}
