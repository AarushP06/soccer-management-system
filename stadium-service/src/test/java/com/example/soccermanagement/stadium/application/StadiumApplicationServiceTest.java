package com.example.soccermanagement.stadium.application;

import com.example.soccermanagement.stadium.api.dto.CreateStadiumRequest;
import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.api.dto.UpdateStadiumRequest;
import com.example.soccermanagement.stadium.application.exception.StadiumConflictException;
import com.example.soccermanagement.stadium.application.exception.StadiumNotFoundException;
import com.example.soccermanagement.stadium.application.port.StadiumRepository;
import com.example.soccermanagement.stadium.domain.Stadium;
import com.example.soccermanagement.stadium.domain.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Verifies application-layer branches and use-case behavior for the stadium service.
 */
@ExtendWith(MockitoExtension.class)
class StadiumApplicationServiceTest {

    @Mock
    private StadiumRepository repository;

    @InjectMocks
    private StadiumApplicationService service;

    @Test
    void createStadiumReturnsSavedResponse() {
        Stadium saved = Stadium.rehydrate(UUID.randomUUID(), "Old Trafford", null, null, null, null);
        when(repository.save(any(Stadium.class))).thenReturn(saved);

        StadiumResponse response = service.create(new CreateStadiumRequest("Old Trafford"));

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Old Trafford");
        verify(repository).save(any(Stadium.class));
    }

    @Test
    void createStadiumMapsDuplicateToConflict() {
        when(repository.save(any(Stadium.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.create(new CreateStadiumRequest("Old Trafford")))
                .isInstanceOf(StadiumConflictException.class)
                .hasMessage("Stadium conflict: duplicate or invalid state");
    }

    @Test
    void getAllStadiumsReturnsMappedResponses() {
        List<Stadium> stadiums = List.of(
                Stadium.rehydrate(UUID.randomUUID(), "Old Trafford", 1, "Manchester", "England", 74879),
                Stadium.rehydrate(UUID.randomUUID(), "Anfield", 2, "Liverpool", "England", 54074)
        );
        when(repository.findAll()).thenReturn(stadiums);

        List<StadiumResponse> responses = service.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Old Trafford");
        assertThat(responses.get(1).externalVenueId()).isEqualTo(2);
    }

    @Test
    void getOneReturnsMappedResponse() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Stadium.rehydrate(id, "Camp Nou", null, null, null, null)));

        StadiumResponse response = service.getOne(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Camp Nou");
    }

    @Test
    void getOneThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOne(id))
                .isInstanceOf(StadiumNotFoundException.class)
                .hasMessage("Stadium not found");
    }

    @Test
    void updateStadiumReturnsUpdatedResponse() {
        UUID id = UUID.randomUUID();
        Stadium existing = Stadium.rehydrate(id, "Camp Nou", null, null, null, null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        StadiumResponse response = service.update(id, new UpdateStadiumRequest("Camp Nou Updated"));

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Camp Nou Updated");
        verify(repository).save(existing);
    }

    @Test
    void updateStadiumThrowsConflictOnDuplicate() {
        UUID id = UUID.randomUUID();
        Stadium existing = Stadium.rehydrate(id, "Camp Nou", null, null, null, null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.update(id, new UpdateStadiumRequest("Camp Nou Updated")))
                .isInstanceOf(StadiumConflictException.class)
                .hasMessage("Stadium conflict: duplicate or invalid state");
    }

    @Test
    void updateStadiumThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new UpdateStadiumRequest("Camp Nou Updated")))
                .isInstanceOf(StadiumNotFoundException.class)
                .hasMessage("Stadium not found");
    }

    @Test
    void deleteStadiumDelegatesToRepository() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void createAndUpdatePropagateDomainValidation() {
        assertThatThrownBy(() -> service.create(new CreateStadiumRequest(" ")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Stadium name cannot be blank");

        UUID id = UUID.randomUUID();
        Stadium existing = Stadium.rehydrate(id, "Camp Nou", null, null, null, null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, new UpdateStadiumRequest(" ")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Stadium name cannot be blank");
    }
}
