package com.example.mcpclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a single message in a conversation
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
    private String id;
    private String role; // "user" or "assistant"
    private String content;
    private String domain;
    private LocalDateTime timestamp;
    
    public static ConversationMessage userMessage(String content, String domain) {
        return new ConversationMessage(
                java.util.UUID.randomUUID().toString(),
                "user",
                content,
                domain,
                LocalDateTime.now()
        );
    }
    
    public static ConversationMessage assistantMessage(String content, String domain) {
        return new ConversationMessage(
                java.util.UUID.randomUUID().toString(),
                "assistant",
                content,
                domain,
                LocalDateTime.now()
        );
    }
} 