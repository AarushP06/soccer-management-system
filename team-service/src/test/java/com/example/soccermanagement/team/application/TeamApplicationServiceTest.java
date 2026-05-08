package com.example.soccermanagement.team.application;

import com.example.soccermanagement.team.api.dto.CreateTeamRequest;
import com.example.soccermanagement.team.api.dto.UpdateTeamRequest;
import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.application.exception.TeamConflictException;
import com.example.soccermanagement.team.application.exception.TeamNotFoundException;
import com.example.soccermanagement.team.application.port.TeamRepository;
import com.example.soccermanagement.team.domain.Team;
import com.example.soccermanagement.team.domain.exception.DomainException;
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
 * Verifies application-layer branches and use-case behavior for the team service.
 */
@ExtendWith(MockitoExtension.class)
class TeamApplicationServiceTest {

    @Mock
    private TeamRepository repository;

    @InjectMocks
    private TeamApplicationService service;

    @Test
    void createTeamReturnsSavedResponse() {
        Team saved = Team.rehydrate(UUID.randomUUID(), "Arsenal", null);
        when(repository.save(any(Team.class))).thenReturn(saved);

        TeamResponse response = service.create(new CreateTeamRequest("Arsenal"));

        assertThat(response.id()).isEqualTo(saved.getId());
        assertThat(response.name()).isEqualTo("Arsenal");
        verify(repository).save(any(Team.class));
    }

    @Test
    void createTeamMapsDuplicateToConflict() {
        when(repository.save(any(Team.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.create(new CreateTeamRequest("Arsenal")))
                .isInstanceOf(TeamConflictException.class)
                .hasMessage("Team conflict: duplicate or invalid state");
    }

    @Test
    void getAllTeamsReturnsMappedResponses() {
        List<Team> teams = List.of(
                Team.rehydrate(UUID.randomUUID(), "Arsenal", null),
                Team.rehydrate(UUID.randomUUID(), "Liverpool", "64")
        );
        when(repository.findAll()).thenReturn(teams);

        List<TeamResponse> responses = service.getAll();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("Arsenal");
        assertThat(responses.get(1).externalId()).isEqualTo("64");
    }

    @Test
    void getOneReturnsMappedResponse() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.of(Team.rehydrate(id, "Ajax", null)));

        TeamResponse response = service.getOne(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Ajax");
    }

    @Test
    void getOneThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getOne(id))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team not found");
    }

    @Test
    void updateTeamReturnsUpdatedResponse() {
        UUID id = UUID.randomUUID();
        Team existing = Team.rehydrate(id, "Ajax", null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        TeamResponse response = service.update(id, new UpdateTeamRequest("Ajax Updated"));

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("Ajax Updated");
        verify(repository).save(existing);
    }

    @Test
    void updateTeamThrowsConflictOnDuplicate() {
        UUID id = UUID.randomUUID();
        Team existing = Team.rehydrate(id, "Ajax", null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.update(id, new UpdateTeamRequest("Ajax Updated")))
                .isInstanceOf(TeamConflictException.class)
                .hasMessage("Team conflict: duplicate or invalid state");
    }

    @Test
    void updateTeamThrowsNotFoundWhenMissing() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new UpdateTeamRequest("Ajax Updated")))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("Team not found");
    }

    @Test
    void deleteTeamDelegatesToRepository() {
        UUID id = UUID.randomUUID();

        service.delete(id);

        verify(repository).deleteById(id);
    }

    @Test
    void createTeamPropagatesDomainValidationForBlankName() {
        assertThatThrownBy(() -> service.create(new CreateTeamRequest(" ")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Team name cannot be blank");
    }

    @Test
    void updateTeamPropagatesDomainValidationForBlankName() {
        UUID id = UUID.randomUUID();
        Team existing = Team.rehydrate(id, "Ajax", null);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, new UpdateTeamRequest(" ")))
                .isInstanceOf(DomainException.class)
                .hasMessage("Team name cannot be blank");
    }
}
