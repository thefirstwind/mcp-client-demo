package com.example.mcpclient.usercenter2.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ListToolsResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Sync client tests for the UserCenter MCP server.
 * These tests verify that the client can communicate with the userCenter MCP server synchronously.
 * 
 * Note: These tests require the userCenter MCP server to be running on localhost:8084.
 */
public class UserCenter2McpSyncClientTests {

    private static final String USER_CENTER_SERVER_URL = "http://localhost:8084";
    private static final String MCP_ENDPOINT = USER_CENTER_SERVER_URL + "/mcp";
    private static final String SAY_HELLO_TOOL_NAME = "sayHello";
    
    private static WebClient webClient;
    
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
    
    @BeforeAll
    static void setupClient() {
        webClient = WebClient.create(MCP_ENDPOINT);
    }
    
    protected McpClientTransport createMcpTransport() {
        return new SimpleMcpClientTransport(webClient);
    }
    
    protected Duration getRequestTimeout() {
        // Use a shorter timeout for tests
        return Duration.ofSeconds(5);
    }
    
    protected Duration getInitializationTimeout() {
        return Duration.ofSeconds(2);
    }

    McpSyncClient client() {
        return client(Function.identity());
    }

    McpSyncClient client(Function<McpClient.SyncSpec, McpClient.SyncSpec> customizer) {
        AtomicReference<McpSyncClient> client = new AtomicReference<>();

        assertThatCode(() -> {
            McpClient.SyncSpec builder = McpClient.sync(createMcpTransport())
                .requestTimeout(getRequestTimeout())
                .initializationTimeout(getInitializationTimeout())
                .capabilities(McpSchema.ClientCapabilities.builder().build());
            builder = customizer.apply(builder);
            client.set(builder.build());
        }).doesNotThrowAnyException();

        return client.get();
    }

    void withClient(Consumer<McpSyncClient> c) {
        var client = client();
        try {
            c.accept(client);
        }
        finally {
            assertThat(client.closeGracefully()).isTrue();
        }
    }
    
    /**
     * Test that verifies the client can call the sayHello tool
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testCallSayHelloTool() {
        withClient(mcpSyncClient -> {
            mcpSyncClient.initialize();
            CallToolRequest request = new CallToolRequest(SAY_HELLO_TOOL_NAME, Map.of());
            
            CallToolResult result = mcpSyncClient.callTool(request);
            
            assertThat(result).isNotNull();
            assertThat(result.content()).isNotEmpty();
            String text = ((TextContent) result.content().get(0)).text();
            assertThat(text).isEqualTo("hello");
        });
    }
    
    /**
     * Test that verifies the client can list tools
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testListTools() {
        withClient(mcpSyncClient -> {
            mcpSyncClient.initialize();
            ListToolsResult result = mcpSyncClient.listTools(null);
            
            assertThat(result).isNotNull();
            assertThat(result.tools()).isNotEmpty();
            
            // Verify that sayHello tool is present
            boolean foundSayHelloTool = result.tools().stream()
                    .map(Tool::name)
                    .anyMatch(SAY_HELLO_TOOL_NAME::equals);
            
            assertThat(foundSayHelloTool).isTrue();
        });
    }
    
    /**
     * Test that verifies the client can ping the server
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testPing() {
        withClient(mcpSyncClient -> {
            mcpSyncClient.initialize();
            assertThatCode(() -> mcpSyncClient.ping()).doesNotThrowAnyException();
        });
    }
    
    /**
     * Test that verifies the client initialization process
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testInitialization() {
        withClient(mcpSyncClient -> {
            assertThatCode(() -> mcpSyncClient.initialize()).doesNotThrowAnyException();
            
            // Verify that re-initializing doesn't cause any issues
            assertThatCode(() -> mcpSyncClient.initialize()).doesNotThrowAnyException();
        });
    }
} 