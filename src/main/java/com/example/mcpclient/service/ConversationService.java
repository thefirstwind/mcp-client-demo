package com.example.mcpclient.service;

import com.example.mcpclient.model.ConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service to manage conversation history
 */
@Service
@Slf4j
public class ConversationService {

    // Map of sessionId to conversation history
    private final Map<String, List<ConversationMessage>> conversations = new ConcurrentHashMap<>();
    
    // Maximum number of messages to keep in history per conversation
    @Value("${conversation.max-history-length:10}")
    private int maxHistoryLength;
    
    /**
     * Add a user message to a conversation
     */
    public ConversationMessage addUserMessage(String sessionId, String content, String domain) {
        ConversationMessage message = ConversationMessage.userMessage(content, domain);
        addMessageToConversation(sessionId, message);
        return message;
    }
    
    /**
     * Add an assistant message to a conversation
     */
    public ConversationMessage addAssistantMessage(String sessionId, String content, String domain) {
        ConversationMessage message = ConversationMessage.assistantMessage(content, domain);
        addMessageToConversation(sessionId, message);
        return message;
    }
    
    /**
     * Get the conversation history for a session
     */
    public List<ConversationMessage> getConversationHistory(String sessionId) {
        return conversations.getOrDefault(sessionId, new ArrayList<>());
    }
    
    /**
     * Add a message to a conversation, creating the conversation if it doesn't exist
     */
    private void addMessageToConversation(String sessionId, ConversationMessage message) {
        List<ConversationMessage> history = conversations.computeIfAbsent(sessionId, k -> new ArrayList<>());
        
        // Add the new message
        history.add(message);
        
        // Trim history if it exceeds maximum length
        if (history.size() > maxHistoryLength) {
            history = history.subList(history.size() - maxHistoryLength, history.size());
            conversations.put(sessionId, history);
        }
        
        log.debug("Added message to conversation {}: {}", sessionId, message.getContent());
    }
    
    /**
     * Clear the conversation history for a session
     */
    public void clearConversation(String sessionId) {
        conversations.remove(sessionId);
        log.debug("Cleared conversation history for session {}", sessionId);
    }
} 