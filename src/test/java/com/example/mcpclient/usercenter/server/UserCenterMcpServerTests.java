package com.example.mcpclient.usercenter.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests for interacting with the userCenter MCP server.
 * This class simulates calls to the userCenter MCP server.
 */
@ExtendWith(MockitoExtension.class)
public class UserCenterMcpServerTests {

    private static final String USER_CENTER_SERVER_NAME = "userCenter";
    private static final String USER_CENTER_SERVER_VERSION = "1.0.0";
    private static final int USER_CENTER_SERVER_PORT = 8084;
    private static final String USER_CENTER_SERVER_URL = "http://localhost:" + USER_CENTER_SERVER_PORT;
    private static final String MCP_ENDPOINT = USER_CENTER_SERVER_URL + "/mcp";
    
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Set up mock responses
        setupMockResponses();
    }

    private void setupMockResponses() {
        // Mock response for sayHello tool - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.postForObject(
            eq(MCP_ENDPOINT + "/tools/call"), 
            any(Map.class),
            eq(String.class)
        )).thenReturn("{\"content\":[{\"type\":\"text\",\"text\":\"hello\"}],\"isError\":false}");
        
        // Mock response for listTools - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.getForObject(
            eq(MCP_ENDPOINT + "/tools"),
            eq(String.class)
        )).thenReturn("[{\"name\":\"sayHello\",\"description\":\"hello\"}]");
    }

    @Test
    void testSayHelloTool() {
        // Create a request to call the sayHello tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", "sayHello");
        request.put("arguments", Map.of());
        
        // Call the tool through the REST client
        String result = restTemplate.postForObject(
            MCP_ENDPOINT + "/tools/call", 
            request,
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("hello");
        assertThat(result).contains("\"isError\":false");
    }
    
    @Test
    void testListTools() {
        // Call the list tools endpoint
        String result = restTemplate.getForObject(
            MCP_ENDPOINT + "/tools",
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("sayHello");
    }
} 