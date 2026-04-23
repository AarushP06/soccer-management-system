package com.example.soccermanagement.stadium.api.dto;

import java.util.UUID;

public record StadiumResponse(UUID id, String name, Integer externalVenueId, String city, String country, Integer capacity) {}

