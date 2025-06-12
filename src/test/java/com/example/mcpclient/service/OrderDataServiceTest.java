package com.example.mcpclient.service;

import com.example.mcpclient.model.McpToolInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OrderDataServiceTest {

    @Mock
    private McpServiceDiscoveryService mcpServiceDiscoveryService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderDataService orderDataService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // Set up a mock RestTemplate using reflection
        try {
            java.lang.reflect.Field field = OrderDataService.class.getDeclaredField("restTemplate");
            field.setAccessible(true);
            field.set(orderDataService, restTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetOrderByOrderNo_ORD20230001() throws Exception {
        // Arrange
        String orderNo = "ORD20230001";
        
        // Mock the tool info
        McpToolInfo mockTool = new McpToolInfo();
        mockTool.setName("getOrderWithLogisticsByOrderNo");
        Map<String, String> connectionDetails = new HashMap<>();
        connectionDetails.put("ip", "127.0.0.1");
        connectionDetails.put("port", "8080");
        mockTool.setConnectionDetails(connectionDetails);
        
        when(mcpServiceDiscoveryService.getToolByName("getOrderWithLogisticsByOrderNo"))
                .thenReturn(mockTool);
        
        // Mock the response from MCP service
        String mockResponseJson = "{"
                + "\"code\": 200,"
                + "\"message\": \"success\","
                + "\"data\": {"
                + "  \"id\": 12345,"
                + "  \"orderNo\": \"ORD20230001\","
                + "  \"userId\": 10001,"
                + "  \"status\": 2,"
                + "  \"amount\": 1498.0,"
                + "  \"createdAt\": \"2023-06-01T12:34:56\","
                + "  \"address\": \"北京市海淀区中关村大街1号\","
                + "  \"itemId\": 1001,"
                + "  \"quantity\": 1"
                + "}"
                + "}";
        
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);
        
        // Mock ObjectMapper to parse the response
        JsonNode mockDataNode = Mockito.mock(JsonNode.class);
        JsonNode mockRootNode = Mockito.mock(JsonNode.class);
        
        when(objectMapper.readTree(mockResponseJson)).thenReturn(mockRootNode);
        when(mockRootNode.has("data")).thenReturn(true);
        when(mockRootNode.get("data")).thenReturn(mockDataNode);
        when(mockRootNode.get("data").isNull()).thenReturn(false);
        
        // Act
        JsonNode result = orderDataService.getOrderByOrderNo(orderNo);
        
        // Assert
        assertNotNull(result);
        assertEquals(mockDataNode, result);
    }
    
    @Test
    public void testGetOrderByOrderNo_NotFound() throws Exception {
        // Arrange
        String orderNo = "ORD20230001-NOT-FOUND";
        
        // Mock the tool info
        McpToolInfo mockTool = new McpToolInfo();
        mockTool.setName("getOrderWithLogisticsByOrderNo");
        Map<String, String> connectionDetails = new HashMap<>();
        connectionDetails.put("ip", "127.0.0.1");
        connectionDetails.put("port", "8080");
        mockTool.setConnectionDetails(connectionDetails);
        
        when(mcpServiceDiscoveryService.getToolByName("getOrderWithLogisticsByOrderNo"))
                .thenReturn(mockTool);
        
        // Mock the response from MCP service with no data
        String mockResponseJson = "{"
                + "\"code\": 404,"
                + "\"message\": \"Order not found\","
                + "\"data\": null"
                + "}";
        
        ResponseEntity<String> mockResponseEntity = new ResponseEntity<>(mockResponseJson, HttpStatus.OK);
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(mockResponseEntity);
        
        // Mock ObjectMapper to parse the response
        JsonNode mockRootNode = Mockito.mock(JsonNode.class);
        
        when(objectMapper.readTree(mockResponseJson)).thenReturn(mockRootNode);
        when(mockRootNode.has("data")).thenReturn(true);
        when(mockRootNode.get("data")).thenReturn(null);
        
        // Act
        JsonNode result = orderDataService.getOrderByOrderNo(orderNo);
        
        // Assert
        assertNull(result);
    }
} 