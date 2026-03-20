package com.example.soccermanagement.league.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLeagueRequest(@NotBlank String name) {
}
