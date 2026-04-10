package com.example.soccermanagement.league.domain;

import com.example.soccermanagement.shared.domain.DomainException;

import java.util.UUID;

public class League {

    private final UUID id;
    private String name;
    private String externalCode; // external competition code (from football-data)

    private League(UUID id, String name, String externalCode) {
        if (name == null || name.isBlank()) {
            throw new DomainException("League name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.externalCode = externalCode;
    }

    public static League create(String name) {
        return new League(UUID.randomUUID(), name, null);
    }

    public static League createFromExternal(String name, String externalCode) {
        return new League(UUID.randomUUID(), name, externalCode);
    }

    public static League rehydrate(UUID id, String name) {
        return new League(id, name, null);
    }

    public static League rehydrate(UUID id, String name, String externalCode) {
        return new League(id, name, externalCode);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new DomainException("League name cannot be blank");
        }
        this.name = newName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExternalCode() {
        return externalCode;
    }
}
