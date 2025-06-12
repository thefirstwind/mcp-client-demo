package com.example.mcpclient.controller;

import com.example.mcpclient.model.ChatRequest;
import com.example.mcpclient.model.ChatResponse;
import com.example.mcpclient.model.MessageCard;
import com.example.mcpclient.model.OrderMessageCard;
import com.example.mcpclient.service.ConversationService;
import com.example.mcpclient.service.LlmChatService;
import com.example.mcpclient.service.MessageCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class ApiControllerTest {

    @Mock
    private LlmChatService llmChatService;

    @Mock
    private ConversationService conversationService;
    
    @Mock
    private MessageCardService messageCardService;

    @InjectMocks
    private ApiController apiController;

    private MockHttpSession mockSession;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockSession = new MockHttpSession();
    }

    @Test
    public void testChat_OrderQuery() {
        // Arrange
        String message = "查询订单号 ORD20230001";
        String aiResponse = "您的订单ORD20230001已发货，订单金额为1498.0元，包含智能手表等商品。";
        
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        
        // Create a sample order card
        OrderMessageCard orderCard = OrderMessageCard.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .title("您的订单已发货")
                .description("订单 ORD20230001 已于今天开始配送")
                .createdTime(LocalDateTime.now())
                .iconUrl("/images/order-icon.png")
                .actionUrl("/orders/ORD20230001")
                .orderNumber("ORD20230001")
                .orderStatus("已发货")
                .orderTime(LocalDateTime.now().minusDays(1))
                .totalAmount(1498.0)
                .items(new ArrayList<>())
                .build();
        
        // Create expected response
        ChatResponse expectedResponse = new ChatResponse(aiResponse);
        
        // Mock LLM service
        when(llmChatService.processChat(any(ChatRequest.class), anyString()))
                .thenReturn(expectedResponse);
        
        // Mock order card creation service
        when(messageCardService.createOrderCardFromMessage(message))
                .thenReturn(orderCard);
        
        // Act
        ResponseEntity<ChatResponse> response = apiController.chat(request, mockSession);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(aiResponse, response.getBody().getMessage());
        assertTrue(response.getBody().isSuccess());
    }
    
    @Test
    public void testChat_OrderNotFound() {
        // Arrange
        String message = "查询订单号 ORD20230001";
        String aiResponse = "抱歉，未能找到订单号为ORD20230001的订单信息。";
        
        ChatRequest request = new ChatRequest();
        request.setMessage(message);
        
        // Create expected response
        ChatResponse expectedResponse = new ChatResponse(aiResponse);
        
        // Mock LLM service
        when(llmChatService.processChat(any(ChatRequest.class), anyString()))
                .thenReturn(expectedResponse);
        
        // Mock order card creation service (order not found)
        when(messageCardService.createOrderCardFromMessage(message))
                .thenReturn(null);
        
        // Act
        ResponseEntity<ChatResponse> response = apiController.chat(request, mockSession);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(aiResponse, response.getBody().getMessage());
        assertTrue(response.getBody().isSuccess());
    }
} 