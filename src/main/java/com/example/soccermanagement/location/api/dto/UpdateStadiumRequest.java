package com.example.soccermanagement.location.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateStadiumRequest(@NotBlank String name) {
}
