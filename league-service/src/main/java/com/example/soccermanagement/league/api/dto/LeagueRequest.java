package com.example.soccermanagement.league.api.dto;

import jakarta.validation.constraints.NotBlank;

public record LeagueRequest(
        @NotBlank(message = "name must not be blank") String name
) {
}

