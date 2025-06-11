package com.example.mcpclient.controller;

import com.example.mcpclient.model.ChatRequest;
import com.example.mcpclient.model.ChatResponse;
import com.example.mcpclient.model.ConversationMessage;
import com.example.mcpclient.model.McpServiceInfo;
import com.example.mcpclient.model.McpToolInfo;
import com.example.mcpclient.service.ConversationService;
import com.example.mcpclient.service.LlmChatService;
import com.example.mcpclient.service.McpServiceDiscoveryService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API controller for MCP client operations
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {

    @Autowired
    private LlmChatService llmChatService;
    
    @Autowired
    private McpServiceDiscoveryService mcpServiceDiscoveryService;
    
    @Autowired
    private ConversationService conversationService;
    
    /**
     * Chat endpoint to interact with LLM and MCP tools
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request, HttpSession session) {
        String sessionId = session.getId();
        log.info("Chat request received from session {} with message: {}", sessionId, request.getMessage());
        ChatResponse response = llmChatService.processChat(request, sessionId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Stream chat endpoint to support typed output effect
     */
    @PostMapping("/chat/stream")
    public ResponseEntity<ChatResponse> streamChat(@RequestBody ChatRequest request, HttpSession session) {
        // This is just a placeholder for the streaming functionality
        // The actual streaming will be implemented in the front-end
        String sessionId = session.getId();
        log.info("Stream chat request received from session {} with message: {}", sessionId, request.getMessage());
        ChatResponse response = llmChatService.processChat(request, sessionId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get conversation history for the current session
     */
    @GetMapping("/conversation")
    public ResponseEntity<List<ConversationMessage>> getConversation(HttpSession session) {
        String sessionId = session.getId();
        List<ConversationMessage> history = conversationService.getConversationHistory(sessionId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Clear conversation history for the current session
     */
    @PostMapping("/conversation/clear")
    public ResponseEntity<Void> clearConversation(HttpSession session) {
        String sessionId = session.getId();
        conversationService.clearConversation(sessionId);
        log.info("Cleared conversation history for session {}", sessionId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get all discovered MCP services
     */
    @GetMapping("/services")
    public ResponseEntity<List<McpServiceInfo>> getAllServices() {
        List<McpServiceInfo> services = mcpServiceDiscoveryService.getAllServices();
        return ResponseEntity.ok(services);
    }
    
    /**
     * Get all tools
     */
    @GetMapping("/tools")
    public ResponseEntity<List<McpToolInfo>> getAllTools() {
        List<McpToolInfo> tools = mcpServiceDiscoveryService.getAllTools();
        return ResponseEntity.ok(tools);
    }
    
    /**
     * Get tools by domain
     */
    @GetMapping("/tools/{domain}")
    public ResponseEntity<List<McpToolInfo>> getToolsByDomain(@PathVariable String domain) {
        List<McpToolInfo> tools = mcpServiceDiscoveryService.getToolsByDomain(domain);
        return ResponseEntity.ok(tools);
    }
    
    /**
     * Force refresh of services
     */
    @PostMapping("/refresh")
    public ResponseEntity<Integer> forceRefresh() {
        int count = mcpServiceDiscoveryService.forceRefresh();
        return ResponseEntity.ok(count);
    }
} 