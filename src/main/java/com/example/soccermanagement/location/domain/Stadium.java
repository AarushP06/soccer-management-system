package com.example.soccermanagement.location.domain;

import com.example.soccermanagement.shared.domain.DomainException;

import java.util.UUID;

public class Stadium {

    private final UUID id;
    private String name;

    private Stadium(UUID id, String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Stadium name cannot be blank");
        }
        this.id = id;
        this.name = name;
    }

    public static Stadium create(String name) {
        return new Stadium(UUID.randomUUID(), name);
    }

    public static Stadium rehydrate(UUID id, String name) {
        return new Stadium(id, name);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new DomainException("Stadium name cannot be blank");
        }
        this.name = newName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
