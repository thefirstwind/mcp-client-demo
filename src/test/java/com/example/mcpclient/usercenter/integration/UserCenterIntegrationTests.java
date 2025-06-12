package com.example.mcpclient.usercenter.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the userCenter MCP server.
 * 
 * Note: These tests require the userCenter MCP server to be running on localhost:8084.
 * Tests will be skipped if the server is not available.
 */
public class UserCenterIntegrationTests {

    private static final String USER_CENTER_URL = "http://localhost:8084";
    private static final String MCP_ENDPOINT = USER_CENTER_URL + "/mcp";
    private static final String SAY_HELLO_TOOL_NAME = "sayHello";
    
    private RestTemplate restTemplate;

    /**
     * Check if the userCenter server is running and available.
     */
    static boolean isUserCenterServerAvailable() {
        try {
            RestTemplate template = new RestTemplate();
            var response = template.getForEntity(USER_CENTER_URL, String.class);
            return response.getStatusCode() != HttpStatus.NOT_FOUND;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
    }

    /**
     * Test calling the sayHello tool on the real userCenter server.
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testSayHelloTool() {
        // Create a request to call the sayHello tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", SAY_HELLO_TOOL_NAME);
        request.put("arguments", Map.of());
        
        // Call the tool
        String result = restTemplate.postForObject(
            MCP_ENDPOINT + "/tools/call",
            request,
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("hello");
    }
    
    /**
     * Test listing tools on the real userCenter server.
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testListTools() {
        // List the available tools
        String tools = restTemplate.getForObject(
            MCP_ENDPOINT + "/tools",
            String.class
        );
        
        // Verify the result
        assertThat(tools).isNotNull();
        
        // Print all available tools for reference
        System.out.println("Available tools on userCenter server:");
        System.out.println(tools);
        
        // Verify sayHello tool is in the list
        assertThat(tools).contains(SAY_HELLO_TOOL_NAME);
    }
}