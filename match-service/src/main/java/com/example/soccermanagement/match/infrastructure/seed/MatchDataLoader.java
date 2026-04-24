package com.example.soccermanagement.match.infrastructure.seed;

import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.domain.Match;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MatchDataLoader implements CommandLineRunner {

    private final MatchRepository matchRepository;

    public MatchDataLoader(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (matchRepository.findAll().isEmpty()) {
            // Use stable UUIDs and reference the teams and stadiums by the UUIDs seeded in their services
            UUID leagueId = UUID.fromString("aaaaaaaa-1111-2222-3333-aaaaaaaaaaaa");
            UUID homeTeam = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID awayTeam = UUID.fromString("22222222-2222-2222-2222-222222222222");
            UUID stadium = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

            matchRepository.save(Match.rehydrate(UUID.fromString("99999999-9999-9999-9999-999999999999"), leagueId, homeTeam, awayTeam, stadium, "m1", "SCHEDULED"));
        }
    }
}

