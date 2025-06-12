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
 * 订单数据服务，负责从tradeCenter服务获取订单数据
 */
@Service
@Slf4j
public class OrderDataService {

    @Autowired
    private McpServiceDiscoveryService mcpServiceDiscoveryService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * 根据订单编号获取订单信息
     * 
     * @param orderNo 订单编号
     * @return 订单数据的JSON对象，包含用户信息和订单详情
     */
    public JsonNode getOrderByOrderNo(String orderNo) {
        try {
            // 获取tradeCenter服务的getOrderWithLogisticsByOrderNo工具
            McpToolInfo tool = mcpServiceDiscoveryService.getToolByName("getOrderWithLogisticsByOrderNo");
            if (tool == null) {
                log.error("无法找到getOrderWithLogisticsByOrderNo工具");
                return null;
            }
            
            // 准备请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("orderNo", orderNo);
            
            // 调用MCP接口
            JsonNode result = callMcpTool(tool, params);
            if (result != null && result.has("data") && !result.get("data").isNull()) {
                log.info("成功获取订单信息，订单号: {}", orderNo);
                return result.get("data");
            } else {
                log.warn("未找到订单信息，订单号: {}", orderNo);
                return null;
            }
        } catch (Exception e) {
            log.error("获取订单信息时出错，订单号: {}", orderNo, e);
            return null;
        }
    }
    
//    /**
//     * 根据用户ID获取订单列表
//     *
//     * @param userId 用户ID
//     * @return 订单列表的JSON对象
//     */
//    public JsonNode getOrdersByUserId(Long userId) {
//        try {
//            // 获取tradeCenter服务的getOrdersWithLogisticsByUserId工具
//            McpToolInfo tool = mcpServiceDiscoveryService.getToolByName("getOrdersWithLogisticsByUserId");
//            if (tool == null) {
//                log.error("无法找到getOrdersWithLogisticsByUserId工具");
//                return null;
//            }
//
//            // 准备请求参数
//            Map<String, Object> params = new HashMap<>();
//            params.put("userId", userId);
//
//            // 调用MCP接口
//            JsonNode result = callMcpTool(tool, params);
//            if (result != null && result.has("data") && !result.get("data").isNull()) {
//                log.info("成功获取用户订单列表，用户ID: {}", userId);
//                return result.get("data");
//            } else {
//                log.warn("未找到用户订单列表，用户ID: {}", userId);
//                return null;
//            }
//        } catch (Exception e) {
//            log.error("获取用户订单列表时出错，用户ID: {}", userId, e);
//            return null;
//        }
//    }
//
//    /**
//     * 根据订单ID获取订单信息
//     *
//     * @param orderId 订单ID
//     * @return 订单数据的JSON对象
//     */
//    public JsonNode getOrderById(Long orderId) {
//        try {
//            // 获取tradeCenter服务的getOrderWithLogisticsById工具
//            McpToolInfo tool = mcpServiceDiscoveryService.getToolByName("getOrderWithLogisticsById");
//            if (tool == null) {
//                log.error("无法找到getOrderWithLogisticsById工具");
//                return null;
//            }
//
//            // 准备请求参数
//            Map<String, Object> params = new HashMap<>();
//            params.put("orderId", orderId);
//
//            // 调用MCP接口
//            JsonNode result = callMcpTool(tool, params);
//            if (result != null && result.has("data") && !result.get("data").isNull()) {
//                log.info("成功获取订单信息，订单ID: {}", orderId);
//                return result.get("data");
//            } else {
//                log.warn("未找到订单信息，订单ID: {}", orderId);
//                return null;
//            }
//        } catch (Exception e) {
//            log.error("获取订单信息时出错，订单ID: {}", orderId, e);
//            return null;
//        }
//    }
    
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