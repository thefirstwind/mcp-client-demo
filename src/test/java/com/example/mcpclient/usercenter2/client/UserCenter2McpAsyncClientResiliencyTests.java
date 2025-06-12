package com.example.mcpclient.usercenter2.client;

import io.modelcontextprotocol.client.McpAsyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Resilience tests for the UserCenter MCP client.
 * These tests verify the client's ability to handle network failures and server restarts.
 * 
 * Note: These tests require the userCenter MCP server to be running on localhost:8084.
 */
public class UserCenter2McpAsyncClientResiliencyTests {

    private static final String USER_CENTER_SERVER_URL = "http://localhost:8084";
    private static final String MCP_ENDPOINT = USER_CENTER_SERVER_URL + "/mcp";
    
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
    
    McpAsyncClient client() {
        return client(Function.identity());
    }
    
    McpAsyncClient client(Function<McpClient.AsyncSpec, McpClient.AsyncSpec> customizer) {
        AtomicReference<McpAsyncClient> client = new AtomicReference<>();

        assertThatCode(() -> {
            McpClient.AsyncSpec builder = McpClient.async(createMcpTransport())
                .requestTimeout(getRequestTimeout())
                .initializationTimeout(getInitializationTimeout())
                .capabilities(McpSchema.ClientCapabilities.builder().build());
            builder = customizer.apply(builder);
            client.set(builder.build());
        }).doesNotThrowAnyException();

        return client.get();
    }

    void withClient(Consumer<McpAsyncClient> c) {
        var client = client();
        try {
            c.accept(client);
        } finally {
            StepVerifier.create(client.closeGracefully())
                .expectComplete()
                .verify(Duration.ofSeconds(10));
        }
    }
    
    /**
     * Custom test to verify the client can call the userCenter sayHello tool
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testCallSayHelloTool() {
        withClient(mcpAsyncClient -> {
            var mono = mcpAsyncClient.initialize()
                .then(mcpAsyncClient.callTool(new McpSchema.CallToolRequest("sayHello", Map.of())))
                .map(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.content()).isNotEmpty();
                    String text = ((McpSchema.TextContent) result.content().get(0)).text();
                    assertThat(text).isEqualTo("hello");
                    return true;
                });
            
            StepVerifier.create(mono)
                .expectNext(true)
                .verifyComplete();
        });
    }
    
    /**
     * Test ping functionality
     */
    @Test
    @EnabledIf("isUserCenterServerAvailable")
    void testPing() {
        withClient(mcpAsyncClient -> {
            StepVerifier.create(mcpAsyncClient.initialize().then(mcpAsyncClient.ping()))
                .expectNextCount(1)
                .verifyComplete();
        });
    }
} 