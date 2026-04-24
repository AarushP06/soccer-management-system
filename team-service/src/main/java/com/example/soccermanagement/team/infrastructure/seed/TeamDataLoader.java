package com.example.soccermanagement.team.infrastructure.seed;

import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TeamDataLoader implements CommandLineRunner {

    private final TeamRepository teamRepository;

    public TeamDataLoader(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (teamRepository.findAll().isEmpty()) {
            // Use stable UUIDs so tests can rely on them
            teamRepository.save(Team.rehydrate(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Manchester City", "65"));
            teamRepository.save(Team.rehydrate(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Liverpool", "64"));
            teamRepository.save(Team.rehydrate(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Leeds United", "66"));
        }
    }
}

