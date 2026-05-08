package com.example.soccermanagement.location.api.mapper;

import com.example.soccermanagement.location.api.dto.StadiumResponse;
import com.example.soccermanagement.location.domain.Stadium;

/**
 * Converts between internal models and transport-friendly data structures.
 */
public final class StadiumApiMapper {

    private StadiumApiMapper() {
    }

    public static StadiumResponse toResponse(Stadium aggregate) {
        return new StadiumResponse(aggregate.getId(), aggregate.getName(), aggregate.getExternalVenueId(), aggregate.getCity(), aggregate.getCountry(), aggregate.getCapacity());
    }
}
