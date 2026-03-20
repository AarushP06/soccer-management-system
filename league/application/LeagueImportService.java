package league.application;

}
    }
        return new LeagueResponse(saved.getId(), saved.getName());
        League saved = leagueRepository.save(new League(null, name));

        }
            throw new LeagueConflictException("League already exists with name: " + name);
        if (leagueRepository.existsByName(name)) {

        String name = competition.name();
        var competition = footballDataLeagueClient.getCompetitionByCode(code);
    public LeagueResponse importByCompetitionCode(String code) {
    @Transactional

    }
        this.leagueRepository = leagueRepository;
        this.footballDataLeagueClient = footballDataLeagueClient;
    public LeagueImportService(FootballDataLeagueClient footballDataLeagueClient, LeagueRepository leagueRepository) {

    private final LeagueRepository leagueRepository;
    private final FootballDataLeagueClient footballDataLeagueClient;

public class LeagueImportService {
@Service

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import league.infrastructure.integration.FootballDataLeagueClient;
import league.domain.exception.LeagueConflictException;
import league.domain.League;
import league.application.port.LeagueRepository;
import league.api.dto.LeagueResponse;

