package com.example.soccermanagement.league.domain;

/**
 * Represents core domain behavior and rules for the league service.
 */
public class League {
    private Long id;
    private String name;

    public League() {}

    public League(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}

