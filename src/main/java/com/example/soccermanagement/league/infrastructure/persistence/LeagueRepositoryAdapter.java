package com.example.soccermanagement.league.infrastructure.persistence;

import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LeagueRepositoryAdapter implements LeagueRepository {

    private final SpringDataLeagueRepository repository;

    public LeagueRepositoryAdapter(SpringDataLeagueRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<League> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<League> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public League save(League aggregate) {
        return toDomain(repository.save(toJpa(aggregate)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    private League toDomain(LeagueJpaEntity entity) {
        return League.rehydrate(entity.getId(), entity.getName());
    }

    private LeagueJpaEntity toJpa(League aggregate) {
        LeagueJpaEntity entity = new LeagueJpaEntity();
        entity.setId(aggregate.getId());
        entity.setName(aggregate.getName());
        return entity;
    }
}
