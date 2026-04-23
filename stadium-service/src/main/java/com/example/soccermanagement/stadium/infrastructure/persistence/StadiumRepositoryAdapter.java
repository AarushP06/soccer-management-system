package com.example.soccermanagement.stadium.infrastructure.persistence;

import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.domain.Stadium;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StadiumRepositoryAdapter implements StadiumRepository {

    private final SpringDataStadiumRepository repository;

    public StadiumRepositoryAdapter(SpringDataStadiumRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Stadium> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Stadium> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Stadium save(Stadium aggregate) {
        return toDomain(repository.save(toJpa(aggregate)));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return repository.existsById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return repository.existsByName(name);
    }

    private Stadium toDomain(StadiumJpaEntity entity) {
        return Stadium.rehydrate(entity.getId(), entity.getName(), entity.getExternalVenueId(), entity.getCity(), entity.getCountry(), entity.getCapacity());
    }

    private StadiumJpaEntity toJpa(Stadium aggregate) {
        StadiumJpaEntity entity = new StadiumJpaEntity();
        entity.setId(aggregate.getId());
        entity.setName(aggregate.getName());
        entity.setExternalVenueId(aggregate.getExternalVenueId());
        entity.setCity(aggregate.getCity());
        entity.setCountry(aggregate.getCountry());
        entity.setCapacity(aggregate.getCapacity());
        return entity;
    }
}

