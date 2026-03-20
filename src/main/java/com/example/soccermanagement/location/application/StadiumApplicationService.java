package com.example.soccermanagement.location.application;

import com.example.soccermanagement.location.api.dto.CreateStadiumRequest;
import com.example.soccermanagement.location.api.dto.UpdateStadiumRequest;
import com.example.soccermanagement.location.api.dto.StadiumResponse;
import com.example.soccermanagement.location.api.mapper.StadiumApiMapper;
import com.example.soccermanagement.location.application.exception.StadiumConflictException;
import com.example.soccermanagement.location.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.location.application.port.StadiumRepository;
import com.example.soccermanagement.location.domain.Stadium;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class StadiumApplicationService {

    private final StadiumRepository repository;

    public StadiumApplicationService(StadiumRepository repository) {
        this.repository = repository;
    }

    public List<StadiumResponse> getAll() {
        return repository.findAll().stream().map(StadiumApiMapper::toResponse).toList();
    }

    public StadiumResponse getOne(UUID id) {
        return repository.findById(id)
                .map(StadiumApiMapper::toResponse)
                .orElseThrow(() -> new StadiumNotFoundException("Stadium not found"));
    }

    public StadiumResponse create(CreateStadiumRequest request) {
        try {
            return StadiumApiMapper.toResponse(repository.save(Stadium.create(request.name())));
        } catch (DataIntegrityViolationException ex) {
            throw new StadiumConflictException("Stadium conflict: duplicate or invalid state");
        }
    }

    public StadiumResponse update(UUID id, UpdateStadiumRequest request) {
        Stadium aggregate = repository.findById(id).orElseThrow(() -> new StadiumNotFoundException("Stadium not found"));
        aggregate.rename(request.name());
        try {
            return StadiumApiMapper.toResponse(repository.save(aggregate));
        } catch (DataIntegrityViolationException ex) {
            throw new StadiumConflictException("Stadium conflict: duplicate or invalid state");
        }
    }

    public void delete(UUID id) {
        repository.deleteById(id);
    }
}
