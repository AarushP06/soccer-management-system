package com.example.soccermanagement.team.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank String name) {
}
