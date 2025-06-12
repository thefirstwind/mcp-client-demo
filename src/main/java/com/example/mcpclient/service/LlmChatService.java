package com.example.mcpclient.service;

import com.example.mcpclient.model.ChatRequest;
import com.example.mcpclient.model.ChatResponse;
import com.example.mcpclient.model.ConversationMessage;
import com.example.mcpclient.model.McpServiceInfo;
import com.example.mcpclient.model.McpToolInfo;
import com.example.mcpclient.model.MessageCard;
import com.example.mcpclient.service.deepseek.DeepSeekClient;
import com.example.mcpclient.service.deepseek.DeepSeekMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that integrates DeepSeek AI with MCP tools for chat functionality.
 */
@Service
@Slf4j
public class LlmChatService {

    @Autowired
    private DeepSeekClient deepSeekClient;
    
    @Autowired
    private McpServiceDiscoveryService mcpServiceDiscoveryService;
    
    @Autowired
    private ConversationService conversationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MessageCardService messageCardService;
    
    /**
     * Process a chat request by generating a response using DeepSeek AI and available MCP tools.
     * 
     * @param request The chat request from the user
     * @param sessionId The session ID for conversation history
     * @return A response containing the LLM's message and used tools
     */
    public ChatResponse processChat(ChatRequest request, String sessionId) {
        try {
            log.info("Processing chat request from session {}: {}", sessionId, request.getMessage());
            
            // Add user message to conversation history
            conversationService.addUserMessage(sessionId, request.getMessage(), request.getDomain());
            
            // 检测是否包含需要卡片展示的内容
            MessageCard card = messageCardService.detectCardFromMessage(request.getMessage());
            if (card != null) {
                log.info("Detected message card from user message: {}", card.getType());
                // 保存卡片
                messageCardService.addCard(card);
                
                // 使用卡片ID创建一个特殊标记，插入到响应中
                String cardMarkup = generateCardMarkup(card);
                
                // 添加助手回复
                String response = "我已为您创建了以下信息卡片：\n\n" + cardMarkup;
                conversationService.addAssistantMessage(sessionId, response, request.getDomain());
                
                return new ChatResponse(response);
            }
            
            // Get conversation history
            List<ConversationMessage> history = conversationService.getConversationHistory(sessionId);
            
            // If domain is specified in the request, use it; otherwise try to determine from the message
            String domain = request.getDomain();
            if (domain == null || domain.isEmpty()) {
                domain = determineDomainFromMessage(request.getMessage());
                log.debug("Determined domain from message: {}", domain);
            }
            
            // Get tools organized by domain
            Map<String, List<McpToolInfo>> toolsByDomain = getToolsOrganizedByDomain();
            
            // Get all available tools across domains for context
            List<McpToolInfo> allTools = new ArrayList<>();
            toolsByDomain.values().forEach(allTools::addAll);
            
            // Get domain-specific tools if a domain was determined
            List<McpToolInfo> domainTools = new ArrayList<>();
            if (domain != null && !domain.isEmpty()) {
                domainTools = toolsByDomain.getOrDefault(domain, new ArrayList<>());
                log.debug("Found {} tools for domain: {}", domainTools.size(), domain);
            }
            
            // Create system prompt with all tools but highlighting domain-specific ones
            String systemPrompt = createSystemPromptWithDomainFocus(allTools, domainTools, domain);
            
            // Convert conversation history to DeepSeek messages
            List<DeepSeekMessage> messages = new ArrayList<>();
            messages.add(DeepSeekMessage.systemMessage(systemPrompt));
            
            // Add conversation history as messages
            for (ConversationMessage msg : history) {
                if ("user".equals(msg.getRole())) {
                    messages.add(DeepSeekMessage.userMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(DeepSeekMessage.assistantMessage(msg.getContent()));
                }
            }
            
            // Call DeepSeek API with the messages
            String responseText = deepSeekClient.chatCompletion(messages)
                    .map(response -> {
                        if (response.getChoices() != null && !response.getChoices().isEmpty()) {
                            return response.getChoices().get(0).getMessage().getContent();
                        }
                        return "No response generated";
                    })
                    .block(); // Block to get the response synchronously
            
            // Add assistant response to conversation history
            conversationService.addAssistantMessage(sessionId, responseText, domain);
            
            return new ChatResponse(responseText);
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            return new ChatResponse("An error occurred while processing your request: " + e.getMessage(), false);
        }
    }
    
    /**
     * 生成卡片标记
     */
    private String generateCardMarkup(MessageCard card) {
        return "@cards[" + card.getId() + "," + card.getType() + "]";
    }
    
    /**
     * Process a chat request without session ID (for backward compatibility)
     */
    public ChatResponse processChat(ChatRequest request) {
        // Generate a random session ID for requests without one
        String sessionId = java.util.UUID.randomUUID().toString();
        return processChat(request, sessionId);
    }
    
    /**
     * Attempt to determine the most relevant domain based on the user's message
     */
    private String determineDomainFromMessage(String message) {
        // Get all domains and their tools
        Map<String, List<McpToolInfo>> toolsByDomain = getToolsOrganizedByDomain();
        
        if (toolsByDomain.isEmpty()) {
            return null;
        }
        
        // Simple keyword matching for domains in the message
        // In a production system, this could use more sophisticated NLP techniques
        String messageLower = message.toLowerCase();
        
        // First, try direct domain name mentions
        for (String domain : toolsByDomain.keySet()) {
            if (messageLower.contains(domain.toLowerCase())) {
                return domain;
            }
        }
        
        // If no direct domain match, try to match based on tool descriptions
        Map<String, Integer> domainScores = new HashMap<>();
        
        for (Map.Entry<String, List<McpToolInfo>> entry : toolsByDomain.entrySet()) {
            String domain = entry.getKey();
            List<McpToolInfo> tools = entry.getValue();
            
            int score = 0;
            for (McpToolInfo tool : tools) {
                // Check if tool name is mentioned
                if (tool.getName() != null && messageLower.contains(tool.getName().toLowerCase())) {
                    score += 3;
                }
                
                // Check for keywords from descriptions
                if (tool.getDescription() != null) {
                    String descLower = tool.getDescription().toLowerCase();
                    String[] words = descLower.split("\\s+");
                    for (String word : words) {
                        if (word.length() > 3 && messageLower.contains(word)) {
                            score += 1;
                        }
                    }
                }
            }
            
            if (score > 0) {
                domainScores.put(domain, score);
            }
        }
        
        // Find domain with highest score
        String bestDomain = null;
        int highestScore = 0;
        
        for (Map.Entry<String, Integer> entry : domainScores.entrySet()) {
            if (entry.getValue() > highestScore) {
                highestScore = entry.getValue();
                bestDomain = entry.getKey();
            }
        }
        
        return bestDomain;
    }
    
    /**
     * Get all tools organized by domain
     */
    private Map<String, List<McpToolInfo>> getToolsOrganizedByDomain() {
        Map<String, List<McpToolInfo>> toolsByDomain = new HashMap<>();
        
        // Get all services
        List<McpServiceInfo> services = mcpServiceDiscoveryService.getAllServices();
        
        // Group tools by domain
        for (McpServiceInfo service : services) {
            String domain = service.getDomain();
            if (domain != null && !domain.isEmpty()) {
                List<McpToolInfo> domainTools = toolsByDomain.computeIfAbsent(domain, k -> new ArrayList<>());
                domainTools.addAll(service.getTools());
            }
        }
        
        return toolsByDomain;
    }
    
    /**
     * Create a system prompt with information about available MCP tools, with focus on domain-specific tools.
     * 
     * @param allTools All available tools
     * @param domainTools Domain-specific tools
     * @param domain The current domain focus
     * @return A system prompt text
     */
    private String createSystemPromptWithDomainFocus(List<McpToolInfo> allTools, List<McpToolInfo> domainTools, String domain) throws JsonProcessingException {
        String allToolsJson = objectMapper.writeValueAsString(allTools);
        String domainToolsJson = objectMapper.writeValueAsString(domainTools);
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
            You are an AI assistant with access to specialized MCP tools in various business domains.
            Your task is to help the user by providing information or performing actions using these tools.
            
            """);
        
        // If we have a specific domain focus, highlight it
        if (domain != null && !domain.isEmpty() && !domainTools.isEmpty()) {
            prompt.append("Based on the user's message, I've determined that the ").append(domain).append(" domain is most relevant.\n\n");
            prompt.append("Priority tools for the ").append(domain).append(" domain:\n");
            prompt.append(domainToolsJson).append("\n\n");
        }
        
        prompt.append("All available tools across domains:\n");
        prompt.append(allToolsJson).append("\n\n");
        
        prompt.append("""
            Guidelines for tool selection and response:
            1. First determine which domain is most relevant to the user's query.
            2. Then select the most appropriate tool(s) based on their descriptions.
            3. Explain how the selected tool(s) can address the user's query.
            4. If the domain is unclear, analyze the content to determine the most appropriate domain.
            5. For queries spanning multiple domains, explain which tools from each domain could be helpful.
            6. Do not invent tool capabilities beyond what is described in the tool information.
            7. If no suitable tools exist for a query, explain that you don't have access to tools for that specific request.
            8. Keep your responses focused, clear, and helpful.
            9. Maintain context of the conversation history and refer back to previous questions when relevant.
            10. If the user's query relates to orders, logistics or package tracking, suggest using the special message card feature.
            
            Format your response as a helpful AI assistant integrating knowledge about the available tools.
            """);
        
        return prompt.toString();
    }
    
    /**
     * Create a standard system prompt with information about available MCP tools.
     * 
     * @param tools The available tools to include in the prompt
     * @return A system prompt text
     */
    private String createSystemPrompt(List<McpToolInfo> tools) throws JsonProcessingException {
        String toolsJson = objectMapper.writeValueAsString(tools);
        
        return """
            You are an AI assistant with access to specialized MCP tools in various business domains.
            Your task is to help the user by providing information or performing actions using these tools.
            
            Available tools:
            """ + toolsJson + """
            
            Guidelines:
            1. When responding to user queries, identify which tools would be most relevant
            2. If a user question can be answered using the available tools, explain how those tools could be used
            3. Do not invent tool capabilities beyond what is described in the tool information
            4. If no suitable tools exist for a query, explain that you don't have access to tools for that specific request
            5. Keep your responses focused, clear, and helpful
            6. Maintain context of the conversation history and refer back to previous questions when relevant
            7. If the user's query relates to orders, logistics or package tracking, suggest using the special message card feature.
            
            Format your response as a helpful AI assistant integrating knowledge about the available tools.
            """;
    }
} 