package com.example.soccermanagement.stadium.infrastructure.seed;

import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.domain.Stadium;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StadiumDataLoader implements CommandLineRunner {

    private final StadiumRepository stadiumRepository;

    public StadiumDataLoader(StadiumRepository stadiumRepository) {
        this.stadiumRepository = stadiumRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (stadiumRepository.findAll().isEmpty()) {
            stadiumRepository.save(Stadium.rehydrate(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "Old Trafford", 1, "Manchester", "England", 74879));
            stadiumRepository.save(Stadium.rehydrate(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "Anfield", 2, "Liverpool", "England", 54074));
        }
    }
}

