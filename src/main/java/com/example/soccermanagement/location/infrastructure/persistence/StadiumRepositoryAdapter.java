package com.example.soccermanagement.location.infrastructure.persistence;

import com.example.soccermanagement.location.application.port.StadiumRepository;
import com.example.soccermanagement.location.domain.Stadium;
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

    private Stadium toDomain(StadiumJpaEntity entity) {
        return Stadium.rehydrate(entity.getId(), entity.getName());
    }

    private StadiumJpaEntity toJpa(Stadium aggregate) {
        StadiumJpaEntity entity = new StadiumJpaEntity();
        entity.setId(aggregate.getId());
        entity.setName(aggregate.getName());
        return entity;
    }
}
