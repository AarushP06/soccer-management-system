package com.example.soccermanagement.team.infrastructure.persistence;

import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TeamRepositoryAdapter implements TeamRepository {

    private final SpringDataTeamRepository repository;

    public TeamRepositoryAdapter(SpringDataTeamRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Team> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Team> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Team save(Team aggregate) {
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

    @Override
    public Optional<Team> findByName(String name) {
        return repository.findByName(name).map(this::toDomain);
    }

    private Team toDomain(TeamJpaEntity entity) {
        return Team.rehydrate(entity.getId(), entity.getName(), entity.getExternalId());
    }

    private TeamJpaEntity toJpa(Team aggregate) {
        TeamJpaEntity entity = new TeamJpaEntity();
        entity.setId(aggregate.getId());
        entity.setName(aggregate.getName());
        entity.setExternalId(aggregate.getExternalId());
        return entity;
    }
}
