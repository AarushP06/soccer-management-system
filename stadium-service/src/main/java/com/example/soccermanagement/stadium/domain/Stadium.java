package com.example.soccermanagement.stadium.domain;

import com.example.soccermanagement.stadium.domain.exception.DomainException;

import java.util.UUID;

public class Stadium {

    private final UUID id;
    private String name;
    private Integer externalVenueId; // optional external venue id from API-Football
    private String city;
    private String country;
    private Integer capacity;

    private Stadium(UUID id, String name, Integer externalVenueId, String city, String country, Integer capacity) {
        if (name == null || name.isBlank()) {
            throw new DomainException("Stadium name cannot be blank");
        }
        this.id = id;
        this.name = name;
        this.externalVenueId = externalVenueId;
        this.city = city;
        this.country = country;
        this.capacity = capacity;
    }

    public static Stadium create(String name) {
        return new Stadium(UUID.randomUUID(), name, null, null, null, null);
    }

    public static Stadium createFromExternal(String name, Integer externalVenueId) {
        return new Stadium(UUID.randomUUID(), name, externalVenueId, null, null, null);
    }

    public static Stadium createFromExternal(String name, Integer externalVenueId, String city, String country, Integer capacity) {
        return new Stadium(UUID.randomUUID(), name, externalVenueId, city, country, capacity);
    }

    public static Stadium rehydrate(UUID id, String name) {
        return new Stadium(id, name, null, null, null, null);
    }

    public static Stadium rehydrate(UUID id, String name, Integer externalVenueId) {
        return new Stadium(id, name, externalVenueId, null, null, null);
    }

    public static Stadium rehydrate(UUID id, String name, Integer externalVenueId, String city, String country, Integer capacity) {
        return new Stadium(id, name, externalVenueId, city, country, capacity);
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

    public Integer getExternalVenueId() {
        return externalVenueId;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

    public Integer getCapacity() {
        return capacity;
    }
}

