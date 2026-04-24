package com.example.soccermanagement.league.infrastructure.seed;

import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LeagueDataLoader implements CommandLineRunner {

    private final LeagueRepository leagueRepository;

    public LeagueDataLoader(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed a couple of leagues with stable IDs
        if (leagueRepository.findAll().isEmpty()) {
            League premier = new League(100L, "Premier League");
            League laliga = new League(200L, "La Liga");
            leagueRepository.save(premier);
            leagueRepository.save(laliga);
        }
    }
}

