package com.example.mcpclient.controller;

import com.example.mcpclient.model.McpServiceInfo;
import com.example.mcpclient.model.McpTool;
import com.example.mcpclient.service.McpServiceDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Web控制器，处理页面路由和模型
 */
@Controller
public class WebController {
    
    @Autowired
    private McpServiceDiscoveryService mcpServiceDiscoveryService;
    
    /**
     * 主页
     */
    @GetMapping("/")
    public String index(Model model) {
        // 获取所有MCP工具
        List<McpTool> tools = mcpServiceDiscoveryService.getAllServices().stream()
                .flatMap(service -> service.getTools().stream()
                        .map(tool -> new McpTool(
                                tool.getName(),
                                tool.getDescription(),
                                service.getDomain(),
                                service.getServiceName(),
                                tool.getInputSchema(),
                                tool.getOutputSchema(),
                                tool.getDocumentation()
                        )))
                .collect(Collectors.toList());
        
        // 获取所有领域
        Set<String> domains = mcpServiceDiscoveryService.getAllServices().stream()
                .map(McpServiceInfo::getDomain)
                .collect(Collectors.toSet());
        
        model.addAttribute("tools", tools);
        model.addAttribute("domains", domains);
        model.addAttribute("showCardsLink", true);
        
        return "index";
    }
} 