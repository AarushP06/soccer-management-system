package com.example.soccermanagement.stadium.api;

import com.example.soccermanagement.stadium.api.dto.CreateStadiumRequest;
import com.example.soccermanagement.stadium.api.dto.UpdateStadiumRequest;
import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.application.StadiumApplicationService;
import com.example.soccermanagement.stadium.application.StadiumImportService;
import com.example.soccermanagement.stadium.application.StadiumBulkImportService;
import com.example.soccermanagement.stadium.application.exception.StadiumConflictException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stadiums")
@Tag(name = "Stadium", description = "Stadium CRUD and import operations")
public class StadiumController {

    private final StadiumApplicationService service;
    private final StadiumImportService importService;
    private final StadiumBulkImportService bulkImportService;

    public StadiumController(
            StadiumApplicationService service,
            StadiumImportService importService,
            StadiumBulkImportService bulkImportService
    ) {
        this.service = service;
        this.importService = importService;
        this.bulkImportService = bulkImportService;
    }

    @GetMapping
    @Operation(summary = "Get all stadiums", description = "Return a list of local stadiums with optional external venue metadata")
    public ResponseEntity<List<StadiumResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a stadium", description = "Get a single stadium by its local UUID")
    public ResponseEntity<StadiumResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    @Operation(summary = "Create a new stadium", description = "Create a stadium with a local name")
    public ResponseEntity<StadiumResponse> create(@Valid @RequestBody CreateStadiumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Bulk create stadiums", description = "Create multiple stadiums in a single request")
    public ResponseEntity<List<StadiumResponse>> bulkCreate(@Valid @RequestBody List<CreateStadiumRequest> requests) {
        var result = new ArrayList<StadiumResponse>();

        for (var request : requests) {
            try {
                var created = service.create(request);
                result.add(created);
            } catch (StadiumConflictException ex) {
                var found = service.getAll().stream()
                        .filter(stadium -> stadium.name().equalsIgnoreCase(request.name()))
                        .findFirst();

                found.ifPresent(result::add);
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/import/local")
    @Operation(
            summary = "Import stadiums from local JSON data",
            description = "Import stadiums from src/main/resources/data/stadiums.json. Each record may include an explicit UUID id and optional externalVenueId metadata. Duplicates by name are skipped."
    )
    public ResponseEntity<StadiumBulkImportService.StadiumBulkImportSummary> importLocal() {
        var summary = bulkImportService.importFromLocal();
        return ResponseEntity.status(HttpStatus.CREATED).body(summary);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a stadium", description = "Update a stadium's name or metadata")
    public ResponseEntity<StadiumResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStadiumRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a stadium", description = "Delete a stadium by local UUID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}