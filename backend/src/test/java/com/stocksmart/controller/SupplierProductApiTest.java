package com.stocksmart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class SupplierProductApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/suppliers/1/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanListButCannotLink() throws Exception {
        String token = login("staff", "Staff@123");
        String adminToken = login("admin", "Admin@123");

        long supplierId = createSupplier(adminToken, "Staff Test Supplier");
        long productId = createProduct(adminToken, "Staff Test Product", "SKU-SP-STAFF");

        mockMvc.perform(get("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanLinkAndUnlinkProduct() throws Exception {
        String token = login("manager", "Manager@123");

        long supplierId = createSupplier(token, "Acme Parts");
        long productId = createProduct(token, "Component X", "SKU-COMP-X");

        mockMvc.perform(post("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + ",\"unitCost\":19.99}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplierId").value(supplierId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.unitCost").value(19.99));

        mockMvc.perform(get("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productSku").value("SKU-COMP-X"));

        mockMvc.perform(get("/api/products/" + productId + "/suppliers")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].supplierName").value("Acme Parts"));

        mockMvc.perform(delete("/api/suppliers/" + supplierId + "/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void duplicateLinkReturns409() throws Exception {
        String token = login("admin", "Admin@123");

        long supplierId = createSupplier(token, "Dupe Supplier");
        long productId = createProduct(token, "Dupe Product", "SKU-DUPE-SP");

        mockMvc.perform(post("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidSupplierReturns404() throws Exception {
        String token = login("admin", "Admin@123");
        long productId = createProduct(token, "Orphan Product", "SKU-ORPHAN");

        mockMvc.perform(post("/api/suppliers/999999/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":" + productId + "}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void invalidProductReturns404() throws Exception {
        String token = login("admin", "Admin@123");
        long supplierId = createSupplier(token, "Valid Supplier");

        mockMvc.perform(post("/api/suppliers/" + supplierId + "/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":999999}"))
                .andExpect(status().isNotFound());
    }

    private long createSupplier(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/suppliers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createProduct(String token, String name, String sku) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"sku\":\"" + sku + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
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
