package com.stocksmart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LocationApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/locations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanListButCannotCreate() throws Exception {
        String token = login("staff", "Staff@123");
        mockMvc.perform(get("/api/locations")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Staff Store\",\"type\":\"STORE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanCreateUpdateAndDelete() throws Exception {
        String token = login("manager", "Manager@123");
        MvcResult created = mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Main Warehouse\",\"type\":\"WAREHOUSE\",\"address\":\"123 Main St\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Main Warehouse"))
                .andExpect(jsonPath("$.type").value("WAREHOUSE"))
                .andExpect(jsonPath("$.address").value("123 Main St"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/locations/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"North Store\",\"type\":\"STORE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("North Store"))
                .andExpect(jsonPath("$.type").value("STORE"));

        mockMvc.perform(delete("/api/locations/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void duplicateNameReturns409() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"East Store\",\"type\":\"STORE\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"east store\",\"type\":\"WAREHOUSE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void missingLocationReturns404() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(get("/api/locations/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidTypeReturns400() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bad Location\",\"type\":\"INVALID_TYPE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingRequiredFieldsReturns400() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("accessToken").asText();
    }
}
