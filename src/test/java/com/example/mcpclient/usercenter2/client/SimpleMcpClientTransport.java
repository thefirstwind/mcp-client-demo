package com.example.mcpclient.usercenter2.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.JSONRPCMessage;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * A simple implementation of the McpClientTransport interface for HTTP-based communication.
 * This implementation is designed for testing purposes.
 */
public class SimpleMcpClientTransport implements McpClientTransport {
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final Sinks.Many<JSONRPCMessage> messageSink = Sinks.many().unicast().onBackpressureBuffer();
    
    public SimpleMcpClientTransport(WebClient webClient) {
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
    public Mono<Void> sendMessage(JSONRPCMessage message) {
        messageSink.tryEmitNext(message);
        return Mono.empty();
    }
    
    @Override
    public Mono<Void> connect(Function<Mono<JSONRPCMessage>, Mono<JSONRPCMessage>> handler) {
        if (connected.compareAndSet(false, true)) {
            return messageSink.asFlux()
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
        messageSink.tryEmitComplete();
    }
    
    @Override
    public Mono<Void> closeGracefully() {
        return Mono.defer(() -> {
            connected.set(false);
            messageSink.tryEmitComplete();
            return Mono.empty();
        });
    }
} 