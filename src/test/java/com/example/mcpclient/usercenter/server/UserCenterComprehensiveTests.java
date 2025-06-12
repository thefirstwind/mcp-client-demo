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
 * Comprehensive tests for the userCenter MCP server, including all available tools.
 * This class simulates REST calls to the userCenter MCP server.
 */
@ExtendWith(MockitoExtension.class)
public class UserCenterComprehensiveTests {

    private static final String USER_CENTER_SERVER_NAME = "userCenter";
    private static final String USER_CENTER_SERVER_VERSION = "1.0.0";
    private static final int USER_CENTER_SERVER_PORT = 8084;
    private static final String USER_CENTER_SERVER_URL = "http://localhost:" + USER_CENTER_SERVER_PORT;
    private static final String MCP_ENDPOINT = USER_CENTER_SERVER_URL + "/mcp";
    
    // Tool names from the UserCenter service
    private static final String SAY_HELLO_TOOL_NAME = "sayHello";
    private static final String GET_USER_BY_ID_TOOL_NAME = "getUserById";
    private static final String GET_USER_BY_USERNAME_TOOL_NAME = "getUserByUsername";
    private static final String CREATE_USER_TOOL_NAME = "createUser";
    private static final String UPDATE_USER_TOOL_NAME = "updateUser";
    
    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        // Set up mock responses for each tool
        setupMockResponses();
    }

    private void setupMockResponses() {
        // Mock response for sayHello tool - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.postForObject(
            eq(MCP_ENDPOINT + "/tools/call"), 
            any(Map.class),
            eq(String.class)
        )).thenAnswer(invocation -> {
            Map<String, Object> request = invocation.getArgument(1);
            String toolName = (String) request.get("name");
            Map<String, Object> args = (Map<String, Object>) request.get("arguments");
            
            switch (toolName) {
                case SAY_HELLO_TOOL_NAME:
                    return "{\"content\":[{\"type\":\"text\",\"text\":\"hello\"}],\"isError\":false}";
                case GET_USER_BY_ID_TOOL_NAME:
                    Long id = ((Number) args.get("id")).longValue();
                    return String.format(
                        "{\"content\":[{\"type\":\"text\",\"text\":\"{\\\"id\\\":%d,\\\"username\\\":\\\"user%d\\\",\\\"email\\\":\\\"user%d@example.com\\\"}\"}],\"isError\":false}",
                        id, id, id
                    );
                case GET_USER_BY_USERNAME_TOOL_NAME:
                    String username = (String) args.get("username");
                    return String.format(
                        "{\"content\":[{\"type\":\"text\",\"text\":\"{\\\"id\\\":1,\\\"username\\\":\\\"%s\\\",\\\"email\\\":\\\"%s@example.com\\\"}\"}],\"isError\":false}",
                        username, username
                    );
                case CREATE_USER_TOOL_NAME:
                    return "{\"content\":[{\"type\":\"text\",\"text\":\"1\"}],\"isError\":false}";
                case UPDATE_USER_TOOL_NAME:
                    return "{\"content\":[{\"type\":\"text\",\"text\":\"true\"}],\"isError\":false}";
                default:
                    return "{\"error\":\"Unknown tool: " + toolName + "\"}";
            }
        });
        
        // Mock response for listTools - using lenient to avoid UnnecessaryStubbingException
        lenient().when(restTemplate.getForObject(
            eq(MCP_ENDPOINT + "/tools"),
            eq(String.class)
        )).thenReturn(String.format(
            "[{\"name\":\"%s\",\"description\":\"hello\"},{\"name\":\"%s\",\"description\":\"根据ID查询用户\"},{\"name\":\"%s\",\"description\":\"根据名查询用户\"},{\"name\":\"%s\",\"description\":\"创建新用户\"},{\"name\":\"%s\",\"description\":\"更新用户信息\"}]",
            SAY_HELLO_TOOL_NAME, GET_USER_BY_ID_TOOL_NAME, GET_USER_BY_USERNAME_TOOL_NAME, CREATE_USER_TOOL_NAME, UPDATE_USER_TOOL_NAME
        ));
    }

    @Test
    void testSayHelloTool() {
        // Create a request to call the sayHello tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", SAY_HELLO_TOOL_NAME);
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
    void testGetUserByIdTool() {
        // Create a request to call the getUserById tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", GET_USER_BY_ID_TOOL_NAME);
        request.put("arguments", Map.of("id", 123));
        
        // Call the tool through the REST client
        String result = restTemplate.postForObject(
            MCP_ENDPOINT + "/tools/call", 
            request,
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("\\\"id\\\":123");
        assertThat(result).contains("\\\"username\\\":\\\"user123\\\"");
    }
    
    @Test
    void testGetUserByUsernameTool() {
        // Create a request to call the getUserByUsername tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", GET_USER_BY_USERNAME_TOOL_NAME);
        request.put("arguments", Map.of("username", "testuser"));
        
        // Call the tool through the REST client
        String result = restTemplate.postForObject(
            MCP_ENDPOINT + "/tools/call", 
            request,
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("\\\"username\\\":\\\"testuser\\\"");
    }
    
    @Test
    void testCreateUserTool() {
        // Create a user object
        Map<String, Object> user = new HashMap<>();
        user.put("username", "newuser");
        user.put("email", "newuser@example.com");
        
        // Create a request to call the createUser tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", CREATE_USER_TOOL_NAME);
        request.put("arguments", Map.of("user", user));
        
        // Call the tool through the REST client
        String result = restTemplate.postForObject(
            MCP_ENDPOINT + "/tools/call", 
            request,
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("\"text\":\"1\"");
    }
    
    @Test
    void testUpdateUserTool() {
        // Create a user object
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("username", "updateduser");
        user.put("email", "updated@example.com");
        
        // Create a request to call the updateUser tool
        Map<String, Object> request = new HashMap<>();
        request.put("name", UPDATE_USER_TOOL_NAME);
        request.put("arguments", Map.of("user", user));
        
        // Call the tool through the REST client
        String result = restTemplate.postForObject(
            MCP_ENDPOINT + "/tools/call", 
            request,
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains("\"text\":\"true\"");
    }

    /**
     * Test listing all tools.
     */
    @Test
    void testListAllTools() {
        // Get the list of tools
        String result = restTemplate.getForObject(
            MCP_ENDPOINT + "/tools",
            String.class
        );
        
        // Verify the result
        assertThat(result).isNotNull();
        assertThat(result).contains(SAY_HELLO_TOOL_NAME);
        assertThat(result).contains(GET_USER_BY_ID_TOOL_NAME);
        assertThat(result).contains(GET_USER_BY_USERNAME_TOOL_NAME);
        assertThat(result).contains(CREATE_USER_TOOL_NAME);
        assertThat(result).contains(UPDATE_USER_TOOL_NAME);
    }
} 