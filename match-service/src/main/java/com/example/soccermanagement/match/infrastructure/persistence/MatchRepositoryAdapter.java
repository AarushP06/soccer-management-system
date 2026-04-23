package com.example.soccermanagement.match.infrastructure.persistence;

import com.example.soccermanagement.match.application.port.MatchRepository;
import com.example.soccermanagement.match.domain.Match;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class MatchRepositoryAdapter implements MatchRepository {
    private final SpringDataMatchRepository repository;

    public MatchRepositoryAdapter(SpringDataMatchRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Match> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Match> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Match save(Match match) {
        MatchJpaEntity entity = toJpa(match);
        MatchJpaEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByLeagueAndTeams(UUID leagueId, UUID homeTeamId, UUID awayTeamId) {
        return repository.existsByLeagueIdAndHomeTeamIdAndAwayTeamId(leagueId, homeTeamId, awayTeamId);
    }

    private Match toDomain(MatchJpaEntity entity) {
        return Match.rehydrate(entity.getId(), entity.getLeagueId(), entity.getHomeTeamId(), entity.getAwayTeamId(), entity.getStadiumId(), entity.getExternalMatchId(), entity.getStatus());
    }

    private MatchJpaEntity toJpa(Match match) {
        MatchJpaEntity e = new MatchJpaEntity();
        e.setId(match.getId());
        e.setLeagueId(match.getLeagueId());
        e.setHomeTeamId(match.getHomeTeamId());
        e.setAwayTeamId(match.getAwayTeamId());
        e.setStadiumId(match.getStadiumId());
        e.setExternalMatchId(match.getExternalMatchId());
        e.setStatus(match.getStatus());
        return e;
    }
}

