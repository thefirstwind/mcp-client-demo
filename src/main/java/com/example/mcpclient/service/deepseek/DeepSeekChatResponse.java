package com.example.mcpclient.service.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a chat completion response from the DeepSeek API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeepSeekChatResponse {
    
    private String id;
    private String object;
    private long created;
    private String model;
    private List<DeepSeekChatChoice> choices;
    private DeepSeekUsage usage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeepSeekChatChoice {
        private int index;
        private DeepSeekMessage message;
        
        @JsonProperty("finish_reason")
        private String finishReason;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeepSeekUsage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        
        @JsonProperty("completion_tokens")
        private int completionTokens;
        
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
} 