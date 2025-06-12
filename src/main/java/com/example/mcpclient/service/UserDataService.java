package com.example.mcpclient.service;

import com.example.mcpclient.model.McpToolInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户数据服务，负责从userCenter服务获取用户数据
 */
@Service
@Slf4j
public class UserDataService {

    @Autowired
    private McpServiceDiscoveryService mcpServiceDiscoveryService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 根据用户ID获取用户信息
     * 
     * @param userId 用户ID
     * @return 用户数据的JSON对象
     */
    public JsonNode getUserById(Long userId) {
        try {
            // 获取userCenter服务的getUserById工具
            McpToolInfo tool = mcpServiceDiscoveryService.getToolByName("getUserById");
            if (tool == null) {
                log.error("无法找到getUserById工具");
                return null;
            }
            
            // 准备请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("id", userId);
            
            // 调用MCP接口
            JsonNode result = callMcpTool(tool, params);
            if (result != null && result.has("data") && !result.get("data").isNull()) {
                log.info("成功获取用户信息，用户ID: {}", userId);
                return result.get("data");
            } else {
                log.warn("未找到用户信息，用户ID: {}", userId);
                return null;
            }
        } catch (Exception e) {
            log.error("获取用户信息时出错，用户ID: {}", userId, e);
            return null;
        }
    }
    
    /**
     * 根据用户名获取用户信息
     * 
     * @param username 用户名
     * @return 用户数据的JSON对象
     */
    public JsonNode getUserByUsername(String username) {
        try {
            // 获取userCenter服务的getUserByUsername工具
            McpToolInfo tool = mcpServiceDiscoveryService.getToolByName("getUserByUsername");
            if (tool == null) {
                log.error("无法找到getUserByUsername工具");
                return null;
            }
            
            // 准备请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("username", username);
            
            // 调用MCP接口
            JsonNode result = callMcpTool(tool, params);
            if (result != null && result.has("data") && !result.get("data").isNull()) {
                log.info("成功获取用户信息，用户名: {}", username);
                return result.get("data");
            } else {
                log.warn("未找到用户信息，用户名: {}", username);
                return null;
            }
        } catch (Exception e) {
            log.error("获取用户信息时出错，用户名: {}", username, e);
            return null;
        }
    }
    
    /**
     * 调用MCP工具
     * 
     * @param tool MCP工具信息
     * @param params 请求参数
     * @return 响应的JSON对象
     */
    private JsonNode callMcpTool(McpToolInfo tool, Map<String, Object> params) throws Exception {
        // 获取连接详情
        Map<String, String> connectionDetails = tool.getConnectionDetails();
        String ip = connectionDetails.get("ip");
        String port = connectionDetails.get("port");
        
        // 构建请求URL
        String url = String.format("http://%s:%s/api/mcp/tools/%s", ip, port, tool.getName());
        
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        // 构建请求体
        String requestBody = objectMapper.writeValueAsString(params);
        
        // 发送请求
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        
        // 解析响应
        return objectMapper.readTree(response.getBody());
    }
} 