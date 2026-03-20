package com.example.soccermanagement.location.api;

import com.example.soccermanagement.location.api.dto.CreateStadiumRequest;
import com.example.soccermanagement.location.api.dto.UpdateStadiumRequest;
import com.example.soccermanagement.location.api.dto.StadiumResponse;
import com.example.soccermanagement.location.application.StadiumApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stadiums")
public class StadiumController {

    private final StadiumApplicationService service;

    public StadiumController(StadiumApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StadiumResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StadiumResponse> getOne(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getOne(id));
    }

    @PostMapping
    public ResponseEntity<StadiumResponse> create(@Valid @RequestBody CreateStadiumRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StadiumResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateStadiumRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
