package com.stocksmart.controller;

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
class InventoryApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/inventory"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCanViewAndStockOutButCannotStockIn() throws Exception {
        String token = login("staff", "Staff@123");
        String adminToken = login("admin", "Admin@123");

        long locId = createLocation(adminToken, "Staff Inv Loc", "STORE");
        long prodId = createProduct(adminToken, "Staff Inv Prod", "SKU-STAFF-INV", "BC-STAFF-INV");

        mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"quantity\":10}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void stockInAndStockOutWorkflow() throws Exception {
        String managerToken = login("manager", "Manager@123");
        String staffToken = login("staff", "Staff@123");

        long locId = createLocation(managerToken, "Main Store", "STORE");
        long prodId = createProduct(managerToken, "Widget Pro", "SKU-WDG-PRO", "BC-WDG-PRO");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"quantity\":50,\"referenceType\":\"PURCHASE_ORDER\",\"referenceId\":\"PO-100\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(50))
                .andExpect(jsonPath("$.locationId").value(locId))
                .andExpect(jsonPath("$.productId").value(prodId));

        mockMvc.perform(post("/api/inventory/stock-out")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"quantity\":20,\"referenceType\":\"ORDER\",\"referenceId\":\"ORD-200\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(30));

        mockMvc.perform(get("/api/inventory/transactions")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productSku").value("SKU-WDG-PRO"));
    }

    @Test
    void stockOutInsufficientStockFailsWith400() throws Exception {
        String managerToken = login("manager", "Manager@123");

        long locId = createLocation(managerToken, "Low Stock Loc", "STORE");
        long prodId = createProduct(managerToken, "Rare Item", "SKU-RARE-1", "BC-RARE-1");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"quantity\":5}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5));

        mockMvc.perform(post("/api/inventory/stock-out")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"quantity\":10}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient stock")));
    }

    @Test
    void adjustStockUpdatesExactQuantity() throws Exception {
        String managerToken = login("manager", "Manager@123");

        long locId = createLocation(managerToken, "Audit Loc", "WAREHOUSE");
        long prodId = createProduct(managerToken, "Count Item", "SKU-COUNT-1", "BC-COUNT-1");

        mockMvc.perform(post("/api/inventory/adjust")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"newQuantity\":42,\"reason\":\"Physical audit recount\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(42));
    }

    @Test
    void barcodeLookupReturnsProductAndBalances() throws Exception {
        String managerToken = login("manager", "Manager@123");

        long locId = createLocation(managerToken, "Scan Store", "STORE");
        long prodId = createProduct(managerToken, "Scan Gadget", "SKU-SCAN-GADGET", "BC-SCAN-8888");

        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"locationId\":" + locId + ",\"productId\":" + prodId + ",\"quantity\":15}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/inventory/barcode/BC-SCAN-8888")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.sku").value("SKU-SCAN-GADGET"))
                .andExpect(jsonPath("$.totalQuantity").value(15))
                .andExpect(jsonPath("$.balances[0].quantity").value(15));
    }

    private long createLocation(String token, String name, String type) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/locations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"type\":\"" + type + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createProduct(String token, String name, String sku, String barcode) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\",\"sku\":\"" + sku + "\",\"barcode\":\"" + barcode + "\"}"))
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
