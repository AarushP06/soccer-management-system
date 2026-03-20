package com.example.soccermanagement.league.application;

import com.example.soccermanagement.league.api.dto.CreateLeagueRequest;
import com.example.soccermanagement.league.api.dto.UpdateLeagueRequest;
import com.example.soccermanagement.league.api.dto.LeagueResponse;
import com.example.soccermanagement.league.api.mapper.LeagueApiMapper;
import com.example.soccermanagement.league.application.exception.LeagueConflictException;
import com.example.soccermanagement.league.application.exception.LeagueNotFoundException;
import com.example.soccermanagement.league.application.port.LeagueRepository;
import com.example.soccermanagement.league.domain.League;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeagueApplicationService {

    private final LeagueRepository repository;

    public LeagueApplicationService(LeagueRepository repository) {
        this.repository = repository;
    }

    public List<LeagueResponse> getAll() {
        return repository.findAll().stream().map(LeagueApiMapper::toResponse).toList();
    }

    public LeagueResponse getOne(UUID id) {
        return repository.findById(id)
                .map(LeagueApiMapper::toResponse)
                .orElseThrow(() -> new LeagueNotFoundException("League not found"));
    }

    public LeagueResponse create(CreateLeagueRequest request) {
        try {
            return LeagueApiMapper.toResponse(repository.save(League.create(request.name())));
        } catch (DataIntegrityViolationException ex) {
            throw new LeagueConflictException("League conflict: duplicate or invalid state");
        }
    }

    public LeagueResponse update(UUID id, UpdateLeagueRequest request) {
        League aggregate = repository.findById(id)
                .orElseThrow(() -> new LeagueNotFoundException("League not found"));
        aggregate.rename(request.name());
        try {
            return LeagueApiMapper.toResponse(repository.save(aggregate));
        } catch (DataIntegrityViolationException ex) {
            throw new LeagueConflictException("League conflict: duplicate or invalid state");
        }
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
