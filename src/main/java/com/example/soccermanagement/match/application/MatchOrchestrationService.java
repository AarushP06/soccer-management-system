package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.MatchController;
import com.example.soccermanagement.match.api.dto.MatchDetailsResponse;
import com.example.soccermanagement.match.application.port.MatchRepository;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MatchOrchestrationService {

    private final MatchRepository repository;

    public MatchOrchestrationService(MatchRepository repository) {
        this.repository = repository;
    }

    public MatchDetailsResponse getMatchDetails(UUID id) {
        var match = repository.findById(id).orElseThrow(() -> new RuntimeException("Match not found"));

        MatchDetailsResponse response = new MatchDetailsResponse(
                match.getId(),
                match.getLeagueId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getStadiumId(),
                match.getStatus()
        );

        Link self = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(MatchController.class).getOne(id)).withSelfRel();
        Link collection = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(MatchController.class).getAll()).withRel("matches");

        response.add(self);
        response.add(collection);
        return response;
    }
}
