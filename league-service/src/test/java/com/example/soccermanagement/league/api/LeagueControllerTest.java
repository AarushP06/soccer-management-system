package com.example.soccermanagement.league.api;

import com.example.soccermanagement.league.application.LeagueImportService;
import com.example.soccermanagement.league.application.LeagueService;
import com.example.soccermanagement.league.domain.League;
import com.example.soccermanagement.league.domain.exception.LeagueConflictException;
import com.example.soccermanagement.league.domain.exception.LeagueNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
 * Tests controller validation, status codes, and error mapping for league endpoints.
 */
@WebMvcTest(LeagueController.class)
@Import(AdviceController.class)
@ActiveProfiles("testing")
class LeagueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeagueService leagueService;

    @MockBean
    private LeagueImportService leagueImportService;

    @Test
    void createLeagueReturnsCreated() throws Exception {
        when(leagueService.create("Premier League")).thenReturn(new League(1L, "Premier League"));

        mockMvc.perform(post("/api/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premier League"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Premier League"));
    }

    @Test
    void getAllLeaguesReturnsOk() throws Exception {
        when(leagueService.list()).thenReturn(List.of(
                new League(1L, "Premier League"),
                new League(2L, "La Liga")
        ));

        mockMvc.perform(get("/api/leagues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Premier League"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].name").value("La Liga"));
    }

    @Test
    void getLeagueByIdReturnsOk() throws Exception {
        when(leagueService.getById(1L)).thenReturn(new League(1L, "Premier League"));

        mockMvc.perform(get("/api/leagues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Premier League"));
    }

    @Test
    void updateLeagueReturnsOk() throws Exception {
        when(leagueService.update(eq(1L), anyString())).thenReturn(new League(1L, "Premier League Updated"));

        mockMvc.perform(put("/api/leagues/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premier League Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Premier League Updated"));
    }

    @Test
    void deleteLeagueReturnsNoContent() throws Exception {
        doNothing().when(leagueService).delete(1L);

        mockMvc.perform(delete("/api/leagues/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicateLeagueReturnsConflict() throws Exception {
        when(leagueService.create("Premier League"))
                .thenThrow(new LeagueConflictException("League already exists with name: Premier League"));

        mockMvc.perform(post("/api/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Premier League"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("League already exists with name: Premier League"));
    }

    @Test
    void missingLeagueReturnsNotFound() throws Exception {
        when(leagueService.getById(999L))
                .thenThrow(new LeagueNotFoundException("League not found: 999"));

        mockMvc.perform(get("/api/leagues/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("League not found: 999"));
    }

    @Test
    void invalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/leagues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name"
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMissingLeagueReturnsNotFound() throws Exception {
        doThrow(new LeagueNotFoundException("League not found: 999")).when(leagueService).delete(999L);

        mockMvc.perform(delete("/api/leagues/999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("League not found: 999"));
    }

    @Test
    void bulkCreateReturnsCreatedAndReusesExistingLeaguesOnFailure() throws Exception {
        when(leagueService.create("Premier League")).thenReturn(new League(1L, "Premier League"));
        when(leagueService.create("La Liga")).thenThrow(new RuntimeException("duplicate"));
        when(leagueService.findByName("La Liga")).thenReturn(java.util.Optional.of(new League(2L, "La Liga")));

        mockMvc.perform(post("/api/leagues/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"name":"Premier League"},
                                  {"name":"La Liga"}
                                ]
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));
    }

    @Test
    void importLocalReturnsCreatedLeague() throws Exception {
        when(leagueImportService.importFromLocal()).thenReturn(new com.example.soccermanagement.league.api.dto.LeagueResponse(9L, "Bundesliga"));

        mockMvc.perform(post("/api/leagues/import/local"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9L))
                .andExpect(jsonPath("$.name").value("Bundesliga"));
    }
}
