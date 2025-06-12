package com.example.mcpclient.usercenter2.server;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Server-side tests for the UserCenter MCP server.
 * These tests create a simple MCP server that implements similar functionality
 * as the userCenter MCP server and validates its operation.
 */
public class UserCenter2McpServerTests {

    private static final String USER_CENTER_SERVER_NAME = "userCenter";
    private static final String USER_CENTER_SERVER_VERSION = "1.0.0";
    private static final String USER_CENTER_SERVER_URL = "http://localhost:8084";
    
    private McpSyncServer mcpSyncServer;
    
    /**
     * Check if the userCenter server is running and available.
     */
    static boolean isUserCenterServerAvailable() {
        try {
            RestTemplate template = new RestTemplate();
            ResponseEntity<String> response = template.getForEntity(USER_CENTER_SERVER_URL, String.class);
            return response.getStatusCode() != HttpStatus.NOT_FOUND;
        } catch (Exception e) {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        // Create an in-memory transport provider
        McpServerTransportProvider transportProvider = new InMemoryMcpServerTransportProvider();
        
        // Create a server with tools similar to the userCenter server
        McpServer.SyncSpecification serverBuilder = McpServer.sync(transportProvider)
                .serverInfo(USER_CENTER_SERVER_NAME, USER_CENTER_SERVER_VERSION)
                .capabilities(ServerCapabilities.builder().tools(true).build());
        
        // Add the sayHello tool
        Tool sayHelloTool = new Tool("sayHello", "A simple hello tool", emptyJsonSchema());
        serverBuilder = serverBuilder.tool(sayHelloTool,
                      (exchange, args) -> new CallToolResult(List.of(new TextContent("hello")), false));
        
        // Add the getUserById tool
        Tool getUserByIdTool = new Tool("getUserById", "Get user by ID", userIdJsonSchema());
        serverBuilder = serverBuilder.tool(getUserByIdTool,
                      (exchange, args) -> {
                          Long userId = ((Number) args.get("id")).longValue();
                          return new CallToolResult(
                              List.of(new TextContent(
                                  String.format("{\"id\":%d,\"name\":\"User %d\"}", userId, userId)
                              )),
                              false
                          );
                      });
        
        // Create the server
        mcpSyncServer = serverBuilder.build();
    }

    @AfterEach
    void tearDown() {
        if (mcpSyncServer != null) {
            mcpSyncServer.closeGracefully();
        }
    }
    
    /**
     * Helper method to create an empty JSON schema
     */
    private String emptyJsonSchema() {
        return """
                {
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "properties": {}
                }
                """;
    }
    
    /**
     * Helper method to create a JSON schema for user ID
     */
    private String userIdJsonSchema() {
        return """
                {
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "properties": {
                        "id": { "type": "integer" }
                    },
                    "required": ["id"]
                }
                """;
    }
    
    /**
     * Test server initialization
     */
    @Test
    void testServerInitialization() {
        // Verify that the server can be initialized without errors
        assertThatCode(() -> {
            // The server is already initialized in setUp(), so this should succeed
            assertThat(mcpSyncServer).isNotNull();
        }).doesNotThrowAnyException();
    }
    
    /**
     * Simple in-memory MCP server transport provider for testing
     */
    static class InMemoryMcpServerTransportProvider implements McpServerTransportProvider {
        private McpServerSession.Factory sessionFactory;
        
        @Override
        public void setSessionFactory(McpServerSession.Factory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }
        
        @Override
        public Mono<Void> closeGracefully() {
            return Mono.empty();
        }
        
        @Override
        public Mono<Void> notifyClients(String method, Object params) {
            return Mono.empty();
        }
    }
} 