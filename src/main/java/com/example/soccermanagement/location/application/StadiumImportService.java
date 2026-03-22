package com.example.soccermanagement.location.application;
import com.example.soccermanagement.location.api.dto.StadiumResponse;
import com.example.soccermanagement.location.api.mapper.StadiumApiMapper;
import com.example.soccermanagement.location.application.exception.StadiumConflictException;
import com.example.soccermanagement.location.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.location.application.port.StadiumRepository;
import com.example.soccermanagement.location.domain.Stadium;
import com.example.soccermanagement.location.infrastructure.integration.ApiFootballVenueClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class StadiumImportService {
    private final ApiFootballVenueClient apiFootballVenueClient;
    private final StadiumRepository stadiumRepository;
    public StadiumImportService(ApiFootballVenueClient apiFootballVenueClient,
                                StadiumRepository stadiumRepository) {
        this.apiFootballVenueClient = apiFootballVenueClient;
        this.stadiumRepository = stadiumRepository;
    }
    @Transactional
    public StadiumResponse importVenueByVenueId(Integer venueId) {
        var externalVenue = apiFootballVenueClient.getVenueById(venueId)
                .orElseThrow(() -> new StadiumNotFoundException("Venue not found for id: " + venueId));
        String stadiumName = externalVenue.name();
        if (stadiumRepository.existsByName(stadiumName)) {
            throw new StadiumConflictException("Stadium already exists: " + stadiumName);
        }
        Stadium saved = stadiumRepository.save(Stadium.create(stadiumName));
        return StadiumApiMapper.toResponse(saved);
    }
}