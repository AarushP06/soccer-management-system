package com.example.soccermanagement.location.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateStadiumRequest(@NotBlank String name) {
}
