package com.example.mcpclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a response from the LLM chat service
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String message;
    private boolean success = true;
    
    public ChatResponse(String message) {
        this.message = message;
        this.success = true;
    }
} 