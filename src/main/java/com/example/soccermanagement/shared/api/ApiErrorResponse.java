package com.example.soccermanagement.shared.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents outgoing API data for the shared service.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
