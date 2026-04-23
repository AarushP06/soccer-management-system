package com.example.soccermanagement.team.domain;

import com.example.soccermanagement.team.domain.exception.DomainException;

import java.util.UUID;

public class Team {

    private final UUID id;
    private String name;
    private String externalId; // external id from football-data

    private Team(UUID id, String name, String externalId) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Team name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.externalId = externalId;
    }

    public static Team create(String name) {
        return new Team(UUID.randomUUID(), name, null);
    }

    public static Team createFromExternal(String name, String externalId) {
        return new Team(UUID.randomUUID(), name, externalId);
    }

    public static Team rehydrate(UUID id, String name) {
        return new Team(id, name, null);
    }

    public static Team rehydrate(UUID id, String name, String externalId) {
        return new Team(id, name, externalId);
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new DomainException("Team name cannot be blank");
        }
        this.name = newName;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }
}

