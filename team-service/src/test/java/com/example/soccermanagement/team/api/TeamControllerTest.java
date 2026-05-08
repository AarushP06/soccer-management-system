package com.example.soccermanagement.team.api;

import com.example.soccermanagement.team.api.dto.CreateTeamRequest;
import com.example.soccermanagement.team.api.dto.TeamImportSummary;
import com.example.soccermanagement.team.api.dto.TeamResponse;
import com.example.soccermanagement.team.api.dto.UpdateTeamRequest;
import com.example.soccermanagement.team.application.TeamApplicationService;
import com.example.soccermanagement.team.application.TeamImportService;
import com.example.soccermanagement.team.application.exception.TeamConflictException;
import com.example.soccermanagement.team.application.exception.TeamNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests controller validation, status codes, and error mapping for team endpoints.
 */
@WebMvcTest(TeamController.class)
@Import(AdviceController.class)
@ActiveProfiles("testing")
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamApplicationService service;

    @MockBean
    private TeamImportService importService;

    @Test
    void createTeamReturnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(new CreateTeamRequest("Arsenal")))
                .thenReturn(new TeamResponse(id, "Arsenal", null));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Arsenal"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Arsenal"));
    }

    @Test
    void getAllTeamsReturnsOk() throws Exception {
        when(service.getAll()).thenReturn(List.of(
                new TeamResponse(UUID.randomUUID(), "Arsenal", null),
                new TeamResponse(UUID.randomUUID(), "Liverpool", "64")
        ));

        mockMvc.perform(get("/api/teams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Arsenal"))
                .andExpect(jsonPath("$[1].name").value("Liverpool"));
    }

    @Test
    void getTeamByIdReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getOne(id)).thenReturn(new TeamResponse(id, "Ajax", null));

        mockMvc.perform(get("/api/teams/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Ajax"));
    }

    @Test
    void updateTeamReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(id, new UpdateTeamRequest("Ajax Updated")))
                .thenReturn(new TeamResponse(id, "Ajax Updated", null));

        mockMvc.perform(put("/api/teams/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ajax Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Ajax Updated"));
    }

    @Test
    void deleteTeamReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/teams/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicateTeamReturnsConflict() throws Exception {
        when(service.create(new CreateTeamRequest("Arsenal")))
                .thenThrow(new TeamConflictException("Team conflict: duplicate or invalid state"));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Arsenal"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Team conflict: duplicate or invalid state"));
    }

    @Test
    void missingTeamReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getOne(id)).thenThrow(new TeamNotFoundException("Team not found"));

        mockMvc.perform(get("/api/teams/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Team not found"));
    }

    @Test
    void invalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMissingTeamReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new TeamNotFoundException("Team not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/teams/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Team not found"));
    }

    @Test
    void bulkCreateReturnsCreatedAndReusesExistingTeamsOnConflict() throws Exception {
        TeamResponse created = new TeamResponse(UUID.randomUUID(), "Arsenal", null);
        TeamResponse existing = new TeamResponse(UUID.randomUUID(), "Liverpool", "64");
        when(service.create(new CreateTeamRequest("Arsenal"))).thenReturn(created);
        when(service.create(new CreateTeamRequest("Liverpool")))
                .thenThrow(new TeamConflictException("Team conflict: duplicate or invalid state"));
        when(service.getAll()).thenReturn(List.of(existing));

        mockMvc.perform(post("/api/teams/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"name":"Arsenal"},
                                  {"name":"Liverpool"}
                                ]
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(created.id().toString()))
                .andExpect(jsonPath("$[1].id").value(existing.id().toString()));
    }

    @Test
    void importLocalTeamsReturnsCreatedSummary() throws Exception {
        when(importService.importFromLocal()).thenReturn(new TeamImportSummary(2, 1));

        mockMvc.perform(post("/api/teams/import/local"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(2))
                .andExpect(jsonPath("$.skipped").value(1));
    }
}
