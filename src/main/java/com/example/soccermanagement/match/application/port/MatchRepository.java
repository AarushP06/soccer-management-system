package com.example.soccermanagement.match.application.port;

import com.example.soccermanagement.match.domain.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {
    List<Match> findAll();
    Optional<Match> findById(UUID id);
    Match save(Match match);
    void deleteById(UUID id);
}
