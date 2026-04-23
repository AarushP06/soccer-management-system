package com.example.soccermanagement.stadium.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStadiumRequest(@NotBlank String name) {}

