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
class WorkflowApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void purchaseOrderReceiveIncreasesInventory() throws Exception {
        String token = login("manager", "Manager@123");

        long supplierId = createSupplier(token, "PO Workflow Supplier");
        long locationId = createLocation(token, "PO Dest Warehouse", "WAREHOUSE");
        long productId = createProduct(token, "PO Product", "SKU-PO-WF", "BC-PO-WF");

        // 1. Create PO
        String poJson = String.format("""
                {
                    "supplierId": %d,
                    "locationId": %d,
                    "items": [
                        { "productId": %d, "quantity": 100, "unitCost": 15.50 }
                    ]
                }
                """, supplierId, locationId, productId);

        MvcResult poResult = mockMvc.perform(post("/api/purchase-orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(poJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andReturn();

        long poId = objectMapper.readTree(poResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Receive PO
        mockMvc.perform(post("/api/purchase-orders/" + poId + "/receive")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.items[0].quantityReceived").value(100));

        // 3. Verify Inventory balance increased to 100
        mockMvc.perform(get("/api/inventory/locations/" + locationId + "/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(100));
    }

    @Test
    void orderConfirmDecreasesInventoryAndChecksStock() throws Exception {
        String token = login("manager", "Manager@123");
        String staffToken = login("staff", "Staff@123");

        long locationId = createLocation(token, "Sales Store", "STORE");
        long productId = createProduct(token, "Order Item", "SKU-ORD-ITEM", "BC-ORD-ITEM");

        // Stock in 50 units
        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"locationId\":%d,\"productId\":%d,\"quantity\":50}", locationId, productId)))
                .andExpect(status().isCreated());

        // 1. Staff creates Order for 30 units
        String orderJson = String.format("""
                {
                    "locationId": %d,
                    "items": [
                        { "productId": %d, "quantity": 30, "unitPrice": 25.00 }
                    ]
                }
                """, locationId, productId);

        MvcResult orderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(750.00))
                .andReturn();

        long orderId = objectMapper.readTree(orderResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Staff confirms Order
        mockMvc.perform(post("/api/orders/" + orderId + "/confirm")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 3. Verify stock is reduced to 20
        mockMvc.perform(get("/api/inventory/locations/" + locationId + "/products/" + productId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));

        // 4. Create another Order for 25 units (exceeds available 20)
        String bigOrderJson = String.format("""
                {
                    "locationId": %d,
                    "items": [
                        { "productId": %d, "quantity": 25 }
                    ]
                }
                """, locationId, productId);

        MvcResult bigOrderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bigOrderJson))
                .andExpect(status().isCreated())
                .andReturn();

        long bigOrderId = objectMapper.readTree(bigOrderResult.getResponse().getContentAsString()).get("id").asLong();

        // Confirming must fail with 400 Bad Request
        mockMvc.perform(post("/api/orders/" + bigOrderId + "/confirm")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient stock")));
    }

    @Test
    void stockTransferMovesQuantityBetweenLocations() throws Exception {
        String token = login("manager", "Manager@123");

        long sourceLocId = createLocation(token, "Transfer Hub A", "WAREHOUSE");
        long destLocId = createLocation(token, "Transfer Outlet B", "STORE");
        long productId = createProduct(token, "Transfer Widget", "SKU-TR-WIDGET", "BC-TR-WIDGET");

        // Stock in 60 units at source location
        mockMvc.perform(post("/api/inventory/stock-in")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"locationId\":%d,\"productId\":%d,\"quantity\":60}", sourceLocId, productId)))
                .andExpect(status().isCreated());

        // 1. Create transfer for 25 units
        String transferJson = String.format("""
                {
                    "sourceLocationId": %d,
                    "destLocationId": %d,
                    "items": [
                        { "productId": %d, "quantity": 25 }
                    ]
                }
                """, sourceLocId, destLocId, productId);

        MvcResult trResult = mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        long transferId = objectMapper.readTree(trResult.getResponse().getContentAsString()).get("id").asLong();

        // 2. Complete transfer
        mockMvc.perform(post("/api/transfers/" + transferId + "/complete")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 3. Verify Source Location has 35 (60 - 25)
        mockMvc.perform(get("/api/inventory/locations/" + sourceLocId + "/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(35));

        // 4. Verify Dest Location has 25 (0 + 25)
        mockMvc.perform(get("/api/inventory/locations/" + destLocId + "/products/" + productId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(25));
    }

    @Test
    void transferSameLocationFails() throws Exception {
        String token = login("manager", "Manager@123");
        long locId = createLocation(token, "Same Loc", "STORE");
        long prodId = createProduct(token, "Same Item", "SKU-SAME", "BC-SAME");

        String invalidJson = String.format("""
                {
                    "sourceLocationId": %d,
                    "destLocationId": %d,
                    "items": [
                        { "productId": %d, "quantity": 5 }
                    ]
                }
                """, locId, locId, prodId);

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("cannot be the same")));
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
