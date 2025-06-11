package com.example.mcpclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * MCP工具模型，包含展示在UI上的工具信息
 */
@Data
@AllArgsConstructor
public class McpTool {
    private String name;
    private String description;
    private String domain;
    private String serviceName;
    private String inputSchema;
    private String outputSchema;
    private String documentation;
} 