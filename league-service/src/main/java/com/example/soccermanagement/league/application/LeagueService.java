package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implements application behavior for the league service.
 */
@Service
public class LeagueService {
    private final LeagueRepository leagueRepository;

    public LeagueService(LeagueRepository leagueRepository) {
        this.leagueRepository = leagueRepository;
    }

    @Transactional
    public League create(String name) {
        if (leagueRepository.existsByName(name)) {
            throw new LeagueConflictException("League already exists with name: " + name);
        }
        var entity = new League(null, name);
        return leagueRepository.save(entity);
    }

    public League getById(Long id) {
        return leagueRepository.findById(id).orElseThrow(() -> new com.example.soccermanagement.league.domain.exception.LeagueNotFoundException("League not found: " + id));
    }

    @Transactional
    public League update(Long id, String name) {
        League existing = getById(id);
        String normalizedName = name == null ? "" : name.trim();

        if (leagueRepository.existsByNameAndIdNot(normalizedName, id)) {
            throw new LeagueConflictException("League already exists with name: " + normalizedName);
        }

        existing.setName(normalizedName);
        return leagueRepository.save(existing);
    }

    public java.util.Optional<League> findByName(String name) {
        return leagueRepository.findByName(name);
    }

    @Transactional
    public void delete(Long id) {
        leagueRepository.deleteById(id);
    }

    public List<League> list() {
        return leagueRepository.findAll();
    }
}
