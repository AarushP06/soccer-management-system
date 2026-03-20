package com.example.soccermanagement.team.application;

import com.example.soccermanagement.team.api.dto.CreateTeamRequest;
import com.example.soccermanagement.team.api.dto.UpdateTeamRequest;
import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.api.mapper.TeamApiMapper;
import com.example.soccermanagement.team.application.exception.TeamConflictException;
import com.example.soccermanagement.team.application.exception.TeamNotFoundException;
import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TeamApplicationService {

    private final TeamRepository repository;

    public TeamApplicationService(TeamRepository repository) {
        this.repository = repository;
    }

    public List<TeamResponse> getAll() {
        return repository.findAll().stream().map(TeamApiMapper::toResponse).toList();
    }

    public TeamResponse getOne(UUID id) {
        return repository.findById(id)
                .map(TeamApiMapper::toResponse)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
    }

    public TeamResponse create(CreateTeamRequest request) {
        try {
            return TeamApiMapper.toResponse(repository.save(Team.create(request.name())));
        } catch (DataIntegrityViolationException ex) {
            throw new TeamConflictException("Team conflict: duplicate or invalid state");
        }
    }

    public TeamResponse update(UUID id, UpdateTeamRequest request) {
        Team aggregate = repository.findById(id)
                .orElseThrow(() -> new TeamNotFoundException("Team not found"));
        aggregate.rename(request.name());
        try {
            return TeamApiMapper.toResponse(repository.save(aggregate));
        } catch (DataIntegrityViolationException ex) {
            throw new TeamConflictException("Team conflict: duplicate or invalid state");
        }
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
