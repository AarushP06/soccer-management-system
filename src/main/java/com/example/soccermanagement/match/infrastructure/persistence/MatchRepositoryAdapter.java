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
        return toDomain(repository.save(toJpa(match)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    private Match toDomain(MatchJpaEntity entity) {
        return Match.rehydrate(
                entity.getId(),
                entity.getLeagueId(),
                entity.getHomeTeamId(),
                entity.getAwayTeamId(),
                entity.getStadiumId(),
                entity.getStatus()
        );
    }

    private MatchJpaEntity toJpa(Match match) {
        MatchJpaEntity entity = new MatchJpaEntity();
        entity.setId(match.getId());
        entity.setLeagueId(match.getLeagueId());
        entity.setHomeTeamId(match.getHomeTeamId());
        entity.setAwayTeamId(match.getAwayTeamId());
        entity.setStadiumId(match.getStadiumId());
        entity.setStatus(match.getStatus());
        return entity;
    }
}
