package com.example.soccermanagement.league.infrastructure.persistence;

import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class LeagueRepositoryAdapter implements LeagueRepository {
    private final SpringDataLeagueRepository springDataLeagueRepository;

    public LeagueRepositoryAdapter(SpringDataLeagueRepository springDataLeagueRepository) {
        this.springDataLeagueRepository = springDataLeagueRepository;
    }

    @Override
    public League save(League league) {
        LeagueEntity entity = new LeagueEntity(league.getId(), league.getName());
        LeagueEntity saved = springDataLeagueRepository.save(entity);
        return new League(saved.getId(), saved.getName());
    }

    @Override
    public Optional<League> findById(Long id) {
        return springDataLeagueRepository.findById(id).map(e -> new League(e.getId(), e.getName()));
    }

    @Override
    public void deleteById(Long id) {
        springDataLeagueRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return springDataLeagueRepository.existsByName(name);
    }

    @Override
    public List<League> findAll() {
        return springDataLeagueRepository.findAll().stream()
                .map(e -> new League(e.getId(), e.getName()))
                .collect(Collectors.toList());
    }
}
