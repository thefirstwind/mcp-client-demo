package com.example.mcpclient.usercenter2.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A mock implementation of McpClientTransport for testing.
 * This transport allows sending requests to a real server via WebClient,
 * but it also provides methods for simulating responses.
 */
public class MockMcpClientTransport implements McpClientTransport {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Sinks.Many<McpSchema.JSONRPCMessage> inbound = Sinks.many().unicast().onBackpressureBuffer();
    private final List<McpSchema.JSONRPCMessage> sent = new ArrayList<>();

    public MockMcpClientTransport(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    public <T> Mono<T> sendRequest(String method, Object params, Class<T> resultType) {
        return webClient.post()
            .uri("/" + method)
            .bodyValue(params)
            .retrieve()
            .bodyToMono(resultType);
    }

    @Override
    public <T> T unmarshalFrom(Object value, TypeReference<T> typeRef) {
        return objectMapper.convertValue(value, typeRef);
    }

    @Override
    public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
        sent.add(message);
        return Mono.empty();
    }

    @Override
    public Mono<Void> connect(Function<Mono<McpSchema.JSONRPCMessage>, Mono<McpSchema.JSONRPCMessage>> handler) {
        if (connected.compareAndSet(false, true)) {
            return inbound.asFlux()
                .flatMap(message -> Mono.just(message).transform(handler))
                .doFinally(signal -> connected.set(false))
                .then();
        } else {
            return Mono.error(new IllegalStateException("Already connected"));
        }
    }

    @Override
    public void close() {
        connected.set(false);
        inbound.tryEmitComplete();
    }

    @Override
    public Mono<Void> closeGracefully() {
        return Mono.defer(() -> {
            connected.set(false);
            inbound.tryEmitComplete();
            return Mono.empty();
        });
    }

    /**
     * Simulate an incoming message from the server
     */
    public void simulateIncomingMessage(McpSchema.JSONRPCMessage message) {
        if (inbound.tryEmitNext(message).isFailure()) {
            throw new RuntimeException("Failed to process incoming message " + message);
        }
    }

    /**
     * Get the last message sent to the server
     */
    public McpSchema.JSONRPCMessage getLastSentMessage() {
        return !sent.isEmpty() ? sent.get(sent.size() - 1) : null;
    }
} 