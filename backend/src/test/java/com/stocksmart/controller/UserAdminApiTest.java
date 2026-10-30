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
class UserAdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listUsersForbiddenForStaff() throws Exception {
        String token = login("staff", "Staff@123");
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsersReturnsSeededAccountsForAdmin() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.username=='admin')]").exists())
                .andExpect(jsonPath("$[?(@.username=='staff')]").exists());
    }

    @Test
    void adminCanCreateAndUpdateUser() throws Exception {
        String token = login("admin", "Admin@123");
        MvcResult created = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "cashier1",
                                  "email": "cashier1@stocksmart.local",
                                  "password": "Welcome@123",
                                  "firstName": "Cash",
                                  "lastName": "Ier",
                                  "role": "STAFF"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("cashier1"))
                .andExpect(jsonPath("$.roles[0]").value("STAFF"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/users/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "cashier1@stocksmart.local",
                                  "firstName": "Cash",
                                  "lastName": "Updated",
                                  "role": "INVENTORY_MANAGER",
                                  "active": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"))
                .andExpect(jsonPath("$.roles[0]").value("INVENTORY_MANAGER"));
    }

    @Test
    void duplicateUsernameReturns409() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "staff",
                                  "email": "anotherstaff@stocksmart.local",
                                  "password": "Welcome@123",
                                  "firstName": "Dup",
                                  "lastName": "User",
                                  "role": "STAFF"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void missingUserReturns404() throws Exception {
        String token = login("admin", "Admin@123");
        mockMvc.perform(get("/api/users/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void adminCannotDeactivateSelf() throws Exception {
        String token = login("admin", "Admin@123");
        long adminId = findUserId(token, "admin");
        mockMvc.perform(delete("/api/users/" + adminId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanDeactivateCreatedUser() throws Exception {
        String token = login("admin", "Admin@123");
        MvcResult created = mockMvc.perform(post("/api/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "tempstaff",
                                  "email": "tempstaff@stocksmart.local",
                                  "password": "Welcome@123",
                                  "firstName": "Temp",
                                  "lastName": "Staff",
                                  "role": "STAFF"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/users/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    private long findUserId(String token, String username) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode users = objectMapper.readTree(result.getResponse().getContentAsString());
        for (JsonNode user : users) {
            if (username.equals(user.get("username").asText())) {
                return user.get("id").asLong();
            }
        }
        throw new AssertionError("User not found: " + username);
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
