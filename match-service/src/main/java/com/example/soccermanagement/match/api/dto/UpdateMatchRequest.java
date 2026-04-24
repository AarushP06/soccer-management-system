package com.example.soccermanagement.match.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateMatchRequest(@NotBlank String status) { }

