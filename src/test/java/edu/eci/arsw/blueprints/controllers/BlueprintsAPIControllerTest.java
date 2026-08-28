package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlueprintsAPIController.class)
class BlueprintsAPIControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlueprintsServices services;

    private static final Blueprint HOUSE =
            new Blueprint("john", "house", List.of(new Point(0, 0), new Point(1, 1)));

    @Test
    void getAllReturns200WithApiResponse() throws Exception {
        when(services.getAllBlueprints()).thenReturn(Set.of(HOUSE));

        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].author").value("john"));
    }

    @Test
    void getByAuthorNotFoundReturns404() throws Exception {
        when(services.getBlueprintsByAuthor("nadie"))
                .thenThrow(new BlueprintNotFoundException("No blueprints for author: nadie"));

        mockMvc.perform(get("/api/v1/blueprints/nadie"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    void getByAuthorAndNameReturns200() throws Exception {
        when(services.getBlueprint("john", "house")).thenReturn(HOUSE);

        mockMvc.perform(get("/api/v1/blueprints/john/house"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("house"));
    }

    @Test
    void postCreatesBlueprintReturns201() throws Exception {
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "author":"kevin", "name":"kitchen", "points":[{"x":1,"y":1}] }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.data.author").value("kevin"));
    }

    @Test
    void postDuplicateBlueprintReturns400() throws Exception {
        doThrow(new BlueprintPersistenceException("Blueprint already exists: kevin:kitchen"))
                .when(services).addNewBlueprint(any());

        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "author":"kevin", "name":"kitchen", "points":[] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void postWithBlankAuthorReturns400ValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/blueprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "author":"", "name":"", "points":[] }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void putAddPointReturns202() throws Exception {
        mockMvc.perform(put("/api/v1/blueprints/john/house/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "x":3, "y":3 }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.code").value(202));
    }

    @Test
    void putAddPointNotFoundReturns404() throws Exception {
        doThrow(new BlueprintNotFoundException("Blueprint not found: nadie/nada"))
                .when(services).addPoint(eq("nadie"), eq("nada"), anyInt(), anyInt());

        mockMvc.perform(put("/api/v1/blueprints/nadie/nada/points")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "x":1, "y":1 }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
