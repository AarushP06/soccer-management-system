package com.example.soccermanagement.stadium.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStadiumRequest(@NotBlank String name) {}

