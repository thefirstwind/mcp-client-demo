package com.example.mcpclient.integration;

import com.example.mcpclient.model.ChatRequest;
import com.example.mcpclient.model.ChatResponse;
import com.example.mcpclient.model.OrderMessageCard;
import com.example.mcpclient.service.LlmChatService;
import com.example.mcpclient.service.MessageCardService;
import com.example.mcpclient.service.OrderDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Integration test for querying an order using the message "查询订单号 ORD20230001"
 * Note: This test mocks external services but tests the real flow through the application
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderQueryIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private OrderDataService orderDataService;
    
    @MockBean
    private LlmChatService llmChatService;
    
    @BeforeEach
    public void setup() {
        // Mock order data
        ObjectNode orderData = objectMapper.createObjectNode()
                .put("id", 12345)
                .put("orderNo", "ORD20230001")
                .put("userId", 10001)
                .put("status", 2)
                .put("amount", 1498.0)
                .put("createdAt", "2023-06-01T12:34:56")
                .put("address", "北京市海淀区中关村大街1号")
                .put("itemId", 1001)
                .put("quantity", 1);
        
        // Mock user data
        ObjectNode userData = objectMapper.createObjectNode()
                .put("id", 10001)
                .put("username", "张三")
                .put("phone", "13512345678");
        
        // Set up mock responses
        when(orderDataService.getOrderByOrderNo("ORD20230001")).thenReturn(orderData);
        
        // Set up LLM mock response
        ChatResponse chatResponse = new ChatResponse("您的订单ORD20230001已发货，订单金额为1498.0元，包含智能手表等商品。");
        when(llmChatService.processChat(org.mockito.ArgumentMatchers.any(), anyString())).thenReturn(chatResponse);
    }
    
    @Test
    public void testOrderQuery() {
        // Prepare request
        String message = "查询订单号 ORD20230001";
        
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
        
        // Execute request
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                "/api/chat",
                entity,
                ChatResponse.class
        );
        
        // Assert response
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getMessage());
        assertTrue(response.getBody().getMessage().contains("ORD20230001"));
        assertTrue(response.getBody().isSuccess());
    }
} 