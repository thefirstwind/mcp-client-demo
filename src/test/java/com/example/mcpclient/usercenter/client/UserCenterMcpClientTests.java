package com.example.mcpclient.usercenter.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * Tests for interacting with the userCenter MCP server as a client.
 * This class simulates a client connecting to the userCenter MCP server.
 */
@ExtendWith(MockitoExtension.class)
public class UserCenterMcpClientTests {

    private static final String USER_CENTER_SERVER_NAME = "userCenter";
    private static final String USER_CENTER_SERVER_VERSION = "1.0.0";
    private static final String SAY_HELLO_TOOL_NAME = "sayHello";
    private static final String EXPECTED_HELLO_RESPONSE = "hello";
    private static final String MCP_SERVER_URL = "http://localhost:8084/mcp";
    
    @Mock
    private RestTemplate restTemplate;
    
    private McpRestClient mcpClient;

    /**
     * Simple MCP REST client that abstracts API calls to the MCP server
     */
    public class McpRestClient {
        private final RestTemplate restTemplate;
        private final String serverUrl;
        
        public McpRestClient(RestTemplate restTemplate, String serverUrl) {
            this.restTemplate = restTemplate;
            this.serverUrl = serverUrl;
        }
        
        /**
         * Call a tool on the MCP server
         */
        public Map<String, Object> callTool(String toolName, Map<String, Object> args) {
            Map<String, Object> request = new HashMap<>();
            request.put("name", toolName);
            request.put("arguments", args);
            
            String response = restTemplate.postForObject(
                serverUrl + "/tools/call",
                request,
                String.class
            );
            
            // In a real implementation, we would parse the JSON response
            // For testing purposes, we'll just return a mock response
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            return result;
        }
        
        /**
         * List all available tools on the MCP server
         */
        public Map<String, Object> listTools() {
            String response = restTemplate.getForObject(
                serverUrl + "/tools",
                String.class
            );
            
            // In a real implementation, we would parse the JSON response
            // For testing purposes, we'll just return a mock response
            Map<String, Object> result = new HashMap<>();
            result.put("response", response);
            return result;
        }
        
        /**
         * Check server health
         */
        public boolean ping() {
            ResponseEntity<String> response = restTemplate.getForEntity(
                serverUrl + "/ping",
                String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        }
    }

    @BeforeEach
    void setUp() {
        // Set up the mock transport to return expected responses
        setupMockResponses();
        
        // Create the MCP REST client
        mcpClient = new McpRestClient(restTemplate, MCP_SERVER_URL);
    }

    /**
     * Set up the mock responses for the RestTemplate
     */
    private void setupMockResponses() {
        // Mock response for sayHello tool - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.postForObject(
            eq(MCP_SERVER_URL + "/tools/call"),
            any(Map.class),
            eq(String.class)
        )).thenAnswer(invocation -> {
            Map<String, Object> request = invocation.getArgument(1);
            String toolName = (String) request.get("name");
            
            if (SAY_HELLO_TOOL_NAME.equals(toolName)) {
                return "{\"content\":[{\"type\":\"text\",\"text\":\"hello\"}],\"isError\":false}";
            } else {
                return "{\"error\":\"Unknown tool: " + toolName + "\"}";
            }
        });
        
        // Mock response for listTools - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.getForObject(
            eq(MCP_SERVER_URL + "/tools"),
            eq(String.class)
        )).thenReturn("[{\"name\":\"sayHello\",\"description\":\"A simple hello tool\"}]");
        
        // Mock response for ping - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.getForEntity(
            eq(MCP_SERVER_URL + "/ping"),
            eq(String.class)
        )).thenReturn(new ResponseEntity<>("", HttpStatus.OK));
    }

    /**
     * Test calling sayHello tool
     */
    @Test
    void testSayHelloTool() {
        // Call the sayHello tool
        Map<String, Object> result = mcpClient.callTool(SAY_HELLO_TOOL_NAME, Map.of());
        
        // Verify the result
        assertThat(result).isNotNull();
        String response = (String) result.get("response");
        assertThat(response).contains(EXPECTED_HELLO_RESPONSE);
        assertThat(response).contains("\"isError\":false");
    }
    
    /**
     * Test calling a non-existent tool
     */
    @Test
    void testNonExistentTool() {
        // Call a non-existent tool
        Map<String, Object> result = mcpClient.callTool("nonExistentTool", Map.of());
        
        // Verify the result contains an error
        String response = (String) result.get("response");
        assertThat(response).contains("Unknown tool");
    }

    /**
     * Test listing available tools
     */
    @Test
    void testListTools() {
        // List the available tools
        Map<String, Object> result = mcpClient.listTools();
        
        // Verify the result
        String response = (String) result.get("response");
        assertThat(response).contains(SAY_HELLO_TOOL_NAME);
    }
    
    /**
     * Test server ping
     */
    @Test
    void testPing() {
        // Ping the server
        boolean result = mcpClient.ping();
        
        // Verify the result
        assertThat(result).isTrue();
    }
} 