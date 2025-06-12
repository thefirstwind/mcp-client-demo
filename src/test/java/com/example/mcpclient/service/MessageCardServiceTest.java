package com.example.mcpclient.service;

import com.example.mcpclient.model.OrderMessageCard;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class MessageCardServiceTest {

    @Mock
    private OrderDataService orderDataService;

    @Mock
    private UserDataService userDataService;

    @InjectMocks
    private MessageCardService messageCardService;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreateOrderCardFromMessage_QueryOrderNumber() {
        // Arrange
        String message = "查询订单号 ORD20230001";
        
        // Create mock order data
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
        
        // Create mock user data
        ObjectNode userData = objectMapper.createObjectNode()
                .put("id", 10001)
                .put("username", "张三")
                .put("phone", "13512345678");
        
        // Mock the service calls
        when(orderDataService.getOrderByOrderNo("ORD20230001")).thenReturn(orderData);
        when(userDataService.getUserById(10001L)).thenReturn(userData);
        
        // Act
        OrderMessageCard result = messageCardService.createOrderCardFromMessage(message);
        
        // Assert
        assertNotNull(result);
        assertEquals("ORD20230001", result.getOrderNumber());
        assertEquals("已发货", result.getOrderStatus());
        assertEquals(1498.0, result.getTotalAmount());
        assertEquals("张三", result.getUserName());
        assertEquals("135****5678", result.getUserPhone());
        assertEquals("北京市海淀区中关村大街1号", result.getUserAddress());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("智能手表", result.getItems().get(0).getProductName());
    }
    
    @Test
    public void testCreateOrderCardFromMessage_OrderNotFound() {
        // Arrange
        String message = "查询订单号 ORD20230001";
        
        // Mock the service call to return null (order not found)
        when(orderDataService.getOrderByOrderNo(anyString())).thenReturn(null);
        
        // Act
        OrderMessageCard result = messageCardService.createOrderCardFromMessage(message);
        
        // Assert
        assertNull(result);
    }
} 