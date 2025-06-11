package com.example.mcpclient.model;

import lombok.Data;

import java.util.Map;

/**
 * Represents information about a tool provided by an MCP service
 */
@Data
public class McpToolInfo {
    private String name;
    private String description;
    private String serviceName;
    private String domain;
    private Map<String, String> connectionDetails;
    private String inputSchema;
    private String outputSchema;
    private String documentation;
} 