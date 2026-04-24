package com.example.soccermanagement.team.api.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record TeamBulkRequest(UUID id, @NotBlank String name) { }

