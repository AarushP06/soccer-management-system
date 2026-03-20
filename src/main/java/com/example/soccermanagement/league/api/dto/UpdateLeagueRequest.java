package com.example.soccermanagement.league.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateLeagueRequest(@NotBlank String name) {
}
