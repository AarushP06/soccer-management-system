package com.example.soccermanagement.match.api;

import com.example.soccermanagement.match.api.dto.CreateMatchRequest;
import com.example.soccermanagement.match.api.dto.MatchDetailsResponse;
import com.example.soccermanagement.match.api.dto.MatchResponse;
import com.example.soccermanagement.match.api.dto.UpdateMatchRequest;
import com.example.soccermanagement.match.application.MatchApplicationService;
import com.example.soccermanagement.match.application.MatchImportService;
import com.example.soccermanagement.match.application.MatchOrchestrationService;
import com.example.soccermanagement.match.application.exception.ExternalServiceException;
import com.example.soccermanagement.match.application.exception.MatchConflictException;
import com.example.soccermanagement.match.application.exception.MatchImportException;
import com.example.soccermanagement.match.application.exception.MatchNotFoundException;
import com.example.soccermanagement.match.application.exception.RelatedEntityNotFoundException;
import com.example.soccermanagement.match.infrastructure.integration.LeagueReferenceMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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
 * Tests controller validation, status codes, and error mapping for match endpoints.
 */
@WebMvcTest(MatchController.class)
@Import(AdviceController.class)
@ActiveProfiles("testing")
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MatchApplicationService matchApplicationService;
    @MockBean private MatchOrchestrationService matchOrchestrationService;
    @MockBean private MatchImportService matchImportService;

    @Test
    void createMatchReturnsCreated() throws Exception {
        MatchResponse response = sampleResponse();
        when(matchApplicationService.create(any(CreateMatchRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()));
    }

    @Test
    void getAllMatchesReturnsOk() throws Exception {
        when(matchApplicationService.getAll()).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].leagueName").value("Premier League"));
    }

    @Test
    void getMatchByIdReturnsOk() throws Exception {
        MatchResponse response = sampleResponse();
        when(matchApplicationService.getOne(response.id())).thenReturn(response);

        mockMvc.perform(get("/api/matches/{id}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.homeTeamName").value("Arsenal"));
    }

    @Test
    void updateMatchReturnsOk() throws Exception {
        MatchResponse response = new MatchResponse(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "CANCELLED", null, "Premier League", "Arsenal", "Liverpool", "Old Trafford");
        when(matchApplicationService.update(response.id(), new UpdateMatchRequest("CANCELLED"))).thenReturn(response);

        mockMvc.perform(put("/api/matches/{id}", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CANCELLED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void deleteMatchReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(matchApplicationService).delete(id);

        mockMvc.perform(delete("/api/matches/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void getMatchDetailsReturnsAggregationAndLinks() throws Exception {
        UUID id = UUID.randomUUID();
        MatchDetailsResponse response = new MatchDetailsResponse(id, LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "SCHEDULED");
        response.setLeagueName("Premier League");
        response.setHomeTeamName("Arsenal");
        response.setAwayTeamName("Liverpool");
        response.setStadiumName("Old Trafford");
        response.add(Link.of("http://localhost/api/matches/" + id, "self"));
        response.add(Link.of("http://localhost/api/matches", "matches"));
        when(matchOrchestrationService.getMatchDetails(id)).thenReturn(response);

        mockMvc.perform(get("/api/matches/{id}/details", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.leagueName").value("Premier League"))
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.matches.href").exists());
    }

    @Test
    void duplicateMatchReturns409() throws Exception {
        when(matchApplicationService.create(any(CreateMatchRequest.class))).thenThrow(new MatchConflictException("Match conflict: duplicate or invalid state"));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isConflict())
                .andExpect(content().string("Match conflict: duplicate or invalid state"));
    }

    @Test
    void missingMatchReturns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(matchApplicationService.getOne(id)).thenThrow(new MatchNotFoundException("Match not found"));

        mockMvc.perform(get("/api/matches/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Match not found"));
    }

    @Test
    void invalidRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leagueId\":\"\",\"homeTeamId\":\"a\",\"awayTeamId\":\"b\",\"stadiumId\":\"c\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidUuidBodyReturns400() throws Exception {
        when(matchApplicationService.create(any(CreateMatchRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid UUID string: bad-home"));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "leagueId":"100",
                                  "homeTeamId":"bad-home",
                                  "awayTeamId":"%s",
                                  "stadiumId":"%s"
                                }
                                """.formatted(UUID.randomUUID(), UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid UUID string: bad-home"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"leagueId\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void downstreamSupportingService404Returns404() throws Exception {
        when(matchApplicationService.create(any(CreateMatchRequest.class))).thenThrow(new RelatedEntityNotFoundException("League not found: 100"));

        mockMvc.perform(post("/api/matches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("League not found: 100"));
    }

    @Test
    void downstreamSupportingService503Returns503() throws Exception {
        when(matchApplicationService.getAll()).thenThrow(new ExternalServiceException("Team service unavailable"));

        mockMvc.perform(get("/api/matches"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Upstream service unavailable: Team service unavailable"));
    }

    @Test
    void bulkCreateReturnsCreatedAndReusesExistingMatchOnConflict() throws Exception {
        MatchResponse created = sampleResponse();
        MatchResponse existing = new MatchResponse(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "SCHEDULED", "ext-1", "Premier League", "Arsenal", "Liverpool", "Old Trafford");
        when(matchApplicationService.create(any(CreateMatchRequest.class)))
                .thenReturn(created)
                .thenThrow(new MatchConflictException("Match conflict: duplicate or invalid state"));
        when(matchApplicationService.getAll()).thenReturn(List.of(existing));

        mockMvc.perform(post("/api/matches/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"leagueId":"100","homeTeamId":"%s","awayTeamId":"%s","stadiumId":"%s"},
                                  {"leagueId":"100","homeTeamId":"%s","awayTeamId":"%s","stadiumId":"%s"}
                                ]
                                """.formatted(
                                created.homeTeamId(), created.awayTeamId(), created.stadiumId(),
                                existing.homeTeamId(), existing.awayTeamId(), existing.stadiumId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(created.id().toString()))
                .andExpect(jsonPath("$[1].id").value(existing.id().toString()));
    }

    @Test
    void bulkCreateSkipsConflictFallbackWhenIdsAreMalformed() throws Exception {
        when(matchApplicationService.create(any(CreateMatchRequest.class)))
                .thenThrow(new MatchConflictException("Match conflict: duplicate or invalid state"));

        mockMvc.perform(post("/api/matches/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"leagueId":"100","homeTeamId":"bad-home","awayTeamId":"bad-away","stadiumId":"bad-stadium"}
                                ]
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().json("[]"));
    }

    @Test
    void importLocalMatchesReturnsCreatedSummary() throws Exception {
        UUID stadiumId = UUID.randomUUID();
        when(matchImportService.importMatchesFromLocal(stadiumId))
                .thenReturn(new com.example.soccermanagement.match.api.dto.MatchImportSummary("local", stadiumId, 2, 1, 0, 0));

        mockMvc.perform(post("/api/matches/import/local")
                        .param("stadiumId", stadiumId.toString()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.competitionCode").value("local"))
                .andExpect(jsonPath("$.imported").value(2))
                .andExpect(jsonPath("$.skipped").value(1));
    }

    @Test
    void importLocalMatchesInvalidUuidReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/matches/import/local")
                        .param("stadiumId", "bad-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void importLocalMatchesMissingStadiumReturnsNotFound() throws Exception {
        UUID stadiumId = UUID.randomUUID();
        when(matchImportService.importMatchesFromLocal(stadiumId))
                .thenThrow(new MatchImportException("Failed to import matches: Stadium not found: " + stadiumId,
                        new MatchImportException("Stadium not found: " + stadiumId)));

        mockMvc.perform(post("/api/matches/import/local")
                        .param("stadiumId", stadiumId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Failed to import matches: Stadium not found: " + stadiumId));
    }

    @Test
    void deleteMissingMatchReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new MatchNotFoundException("Match not found")).when(matchApplicationService).delete(id);

        mockMvc.perform(delete("/api/matches/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Match not found"));
    }

    private MatchResponse sampleResponse() {
        return new MatchResponse(UUID.randomUUID(), LeagueReferenceMapper.toInternalUuid(100L), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "SCHEDULED", null, "Premier League", "Arsenal", "Liverpool", "Old Trafford");
    }

    private String validCreateJson() {
        MatchResponse response = sampleResponse();
        return """
                {
                  "leagueId":"%s",
                  "homeTeamId":"%s",
                  "awayTeamId":"%s",
                  "stadiumId":"%s"
                }
                """.formatted(response.leagueId(), response.homeTeamId(), response.awayTeamId(), response.stadiumId());
    }
}
