package com.example.soccermanagement.stadium.api;

import com.example.soccermanagement.stadium.api.dto.CreateStadiumRequest;
import com.example.soccermanagement.stadium.api.dto.StadiumResponse;
import com.example.soccermanagement.stadium.api.dto.UpdateStadiumRequest;
import com.example.soccermanagement.stadium.application.StadiumApplicationService;
import com.example.soccermanagement.stadium.application.StadiumBulkImportService;
import com.example.soccermanagement.stadium.application.StadiumImportService;
import com.example.soccermanagement.stadium.application.exception.StadiumConflictException;
import com.example.soccermanagement.stadium.application.exception.StadiumNotFoundException;
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
 * Tests controller validation, status codes, and error mapping for stadium endpoints.
 */
@WebMvcTest(StadiumController.class)
@Import(AdviceController.class)
@ActiveProfiles("testing")
class StadiumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StadiumApplicationService service;

    @MockBean
    private StadiumImportService importService;

    @MockBean
    private StadiumBulkImportService bulkImportService;

    @Test
    void createStadiumReturnsCreated() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.create(new CreateStadiumRequest("Old Trafford")))
                .thenReturn(new StadiumResponse(id, "Old Trafford", null, null, null, null));

        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Old Trafford"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Old Trafford"));
    }

    @Test
    void getAllStadiumsReturnsOk() throws Exception {
        when(service.getAll()).thenReturn(List.of(
                new StadiumResponse(UUID.randomUUID(), "Old Trafford", 1, "Manchester", "England", 74879),
                new StadiumResponse(UUID.randomUUID(), "Anfield", 2, "Liverpool", "England", 54074)
        ));

        mockMvc.perform(get("/api/stadiums"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Old Trafford"))
                .andExpect(jsonPath("$[1].name").value("Anfield"));
    }

    @Test
    void getStadiumByIdReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getOne(id)).thenReturn(new StadiumResponse(id, "Camp Nou", 3, "Barcelona", "Spain", 99354));

        mockMvc.perform(get("/api/stadiums/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Camp Nou"));
    }

    @Test
    void updateStadiumReturnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.update(id, new UpdateStadiumRequest("Camp Nou Updated")))
                .thenReturn(new StadiumResponse(id, "Camp Nou Updated", 3, "Barcelona", "Spain", 99354));

        mockMvc.perform(put("/api/stadiums/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Camp Nou Updated"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Camp Nou Updated"));
    }

    @Test
    void deleteStadiumReturnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mockMvc.perform(delete("/api/stadiums/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicateStadiumReturnsConflict() throws Exception {
        when(service.create(new CreateStadiumRequest("Old Trafford")))
                .thenThrow(new StadiumConflictException("Stadium conflict: duplicate or invalid state"));

        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Old Trafford"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().string("Stadium conflict: duplicate or invalid state"));
    }

    @Test
    void missingStadiumReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getOne(id)).thenThrow(new StadiumNotFoundException("Stadium not found"));

        mockMvc.perform(get("/api/stadiums/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Stadium not found"));
    }

    @Test
    void invalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void malformedJsonReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/stadiums")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMissingStadiumReturnsNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new StadiumNotFoundException("Stadium not found")).when(service).delete(id);

        mockMvc.perform(delete("/api/stadiums/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Stadium not found"));
    }

    @Test
    void bulkCreateReturnsCreatedAndReusesExistingStadiumOnConflict() throws Exception {
        StadiumResponse created = new StadiumResponse(UUID.randomUUID(), "Old Trafford", 1, "Manchester", "England", 74879);
        StadiumResponse existing = new StadiumResponse(UUID.randomUUID(), "Anfield", 2, "Liverpool", "England", 54074);
        when(service.create(new CreateStadiumRequest("Old Trafford"))).thenReturn(created);
        when(service.create(new CreateStadiumRequest("Anfield")))
                .thenThrow(new StadiumConflictException("Stadium conflict: duplicate or invalid state"));
        when(service.getAll()).thenReturn(List.of(existing));

        mockMvc.perform(post("/api/stadiums/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                [
                                  {"name":"Old Trafford"},
                                  {"name":"Anfield"}
                                ]
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].id").value(created.id().toString()))
                .andExpect(jsonPath("$[1].id").value(existing.id().toString()));
    }

    @Test
    void importLocalReturnsCreatedSummary() throws Exception {
        when(bulkImportService.importFromLocal()).thenReturn(new StadiumBulkImportService.StadiumBulkImportSummary(2, 1, List.of()));

        mockMvc.perform(post("/api/stadiums/import/local"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(2))
                .andExpect(jsonPath("$.skipped").value(1));
    }
}
