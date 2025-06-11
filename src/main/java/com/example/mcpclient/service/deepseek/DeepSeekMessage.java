package com.example.mcpclient.service.deepseek;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a message in the DeepSeek chat format
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeepSeekMessage {
    
    private String role;
    private String content;
    
    // Factory methods for common message types
    public static DeepSeekMessage systemMessage(String content) {
        return DeepSeekMessage.builder()
                .role("system")
                .content(content)
                .build();
    }
    
    public static DeepSeekMessage userMessage(String content) {
        return DeepSeekMessage.builder()
                .role("user")
                .content(content)
                .build();
    }
    
    public static DeepSeekMessage assistantMessage(String content) {
        return DeepSeekMessage.builder()
                .role("assistant")
                .content(content)
                .build();
    }
} 