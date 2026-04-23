package com.example.soccermanagement.stadium.api.mapper;

import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.domain.Stadium;

public final class StadiumApiMapper {
    public static StadiumResponse toResponse(Stadium aggregate) {
        return new StadiumResponse(aggregate.getId(), aggregate.getName(), aggregate.getExternalVenueId(), aggregate.getCity(), aggregate.getCountry(), aggregate.getCapacity());
    }
}

