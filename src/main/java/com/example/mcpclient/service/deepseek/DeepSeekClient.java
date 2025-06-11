package com.example.mcpclient.service.deepseek;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Client for interacting with the DeepSeek API
 */
@Service
@Slf4j
public class DeepSeekClient {

    private final WebClient webClient;
    private final String model;
    private final Integer maxTokens;
    private final Double temperature;

    public DeepSeekClient(
            @Value("${deepseek.api-key}") String apiKey,
            @Value("${deepseek.base-url}") String baseUrl,
            @Value("${deepseek.model}") String model,
            @Value("${deepseek.max-tokens}") Integer maxTokens,
            @Value("${deepseek.temperature}") Double temperature
    ) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
        this.model = model;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        
        log.info("DeepSeek client initialized with model: {}", model);
    }

    /**
     * Send a chat completion request to DeepSeek API
     */
    public Mono<DeepSeekChatResponse> chatCompletion(List<DeepSeekMessage> messages) {
        DeepSeekChatRequest request = DeepSeekChatRequest.builder()
                .model(model)
                .messages(messages)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();
        
        log.debug("Sending chat completion request to DeepSeek API with {} messages", messages.size());
        
        return webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(DeepSeekChatResponse.class)
                .doOnSuccess(response -> log.debug("Received response from DeepSeek API: {}", response))
                .doOnError(error -> log.error("Error calling DeepSeek API: {}", error.getMessage()));
    }

    /**
     * Convenience method to send a simple message with system context
     */
    public Mono<String> sendMessage(String systemPrompt, String userMessage) {
        List<DeepSeekMessage> messages = new ArrayList<>();
        
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(DeepSeekMessage.systemMessage(systemPrompt));
        }
        
        messages.add(DeepSeekMessage.userMessage(userMessage));
        
        return chatCompletion(messages)
                .map(response -> {
                    if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                        return response.getChoices().get(0).getMessage().getContent();
                    }
                    return "No response generated";
                });
    }
} 