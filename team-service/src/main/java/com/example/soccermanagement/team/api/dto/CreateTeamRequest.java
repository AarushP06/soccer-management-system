package com.example.soccermanagement.team.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@Schema(description = "Name of the team", example = "Benfica") @NotBlank String name) {
}

