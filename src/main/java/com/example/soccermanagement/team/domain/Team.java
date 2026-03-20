package com.example.soccermanagement.team.domain;

import com.example.soccermanagement.shared.domain.DomainException;

import java.util.UUID;

public class Team {

    private final UUID id;
    private String name;

    private Team(UUID id, String name) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Team name cannot be blank");
        }
        this.id = id;
        this.name = name;
    }

    public static Team create(String name) {
        return new Team(UUID.randomUUID(), name);
    }

    public static Team rehydrate(UUID id, String name) {
        return new Team(id, name);
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
}
