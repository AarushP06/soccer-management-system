package com.example.soccermanagement.team.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateTeamRequest(@NotBlank String name) {}

