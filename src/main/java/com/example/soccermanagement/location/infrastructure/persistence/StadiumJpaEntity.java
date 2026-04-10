package com.example.soccermanagement.location.infrastructure.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "stadiums")
public class StadiumJpaEntity {

    @Id
    private UUID id;
    private String name;
    private Integer externalVenueId; // external API-Football venue id
    private String city;
    private String country;
    private Integer capacity;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getExternalVenueId() {
        return externalVenueId;
    }

    public void setExternalVenueId(Integer externalVenueId) {
        this.externalVenueId = externalVenueId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }
}
