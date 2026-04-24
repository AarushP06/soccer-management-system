package com.example.soccermanagement.match.application;

import com.example.soccermanagement.match.api.MatchController;
import com.example.soccermanagement.match.api.dto.MatchDetailsResponse;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.application.port.LeagueLookupPort;
import com.example.soccermanagement.match.application.port.StadiumLookupPort;
import com.example.soccermanagement.match.application.port.TeamLookupPort;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MatchOrchestrationService {

    private final MatchRepository repository;
    private final LeagueLookupPort leagueLookupPort;
    private final TeamLookupPort teamLookupPort;
    private final StadiumLookupPort stadiumLookupPort;

    public MatchOrchestrationService(MatchRepository repository, LeagueLookupPort leagueLookupPort, TeamLookupPort teamLookupPort, StadiumLookupPort stadiumLookupPort) {
        this.repository = repository;
        this.leagueLookupPort = leagueLookupPort;
        this.teamLookupPort = teamLookupPort;
        this.stadiumLookupPort = stadiumLookupPort;
    }

    public MatchDetailsResponse getMatchDetails(UUID id) {
        var match = repository.findById(id).orElseThrow(() -> new MatchNotFoundException("Match not found"));

        MatchDetailsResponse response = new MatchDetailsResponse(
                match.getId(),
                match.getLeagueId(),
                match.getHomeTeamId(),
                match.getAwayTeamId(),
                match.getStadiumId(),
                match.getStatus()
        );

        // resolve names via ports
        String leagueName = leagueLookupPort.findNameById(match.getLeagueId()).orElse(null);
        String homeName = teamLookupPort.findNameById(match.getHomeTeamId()).orElse(null);
        String awayName = teamLookupPort.findNameById(match.getAwayTeamId()).orElse(null);
        String stadiumName = stadiumLookupPort.findNameById(match.getStadiumId()).orElse(null);

        response.setLeagueName(leagueName);
        response.setHomeTeamName(homeName);
        response.setAwayTeamName(awayName);
        response.setStadiumName(stadiumName);

        Link self = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(MatchController.class).getOne(id)).withSelfRel();
        Link collection = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(MatchController.class).getAll()).withRel("matches");

        response.add(self);
        response.add(collection);
        return response;
    }
}
