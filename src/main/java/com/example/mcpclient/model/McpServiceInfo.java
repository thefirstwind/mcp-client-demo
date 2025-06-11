package com.example.mcpclient.model;

import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents information about an MCP service discovered from Nacos
 */
@Data
public class McpServiceInfo {
    private String serviceName;
    private String domain;
    private String protocol;
    private String mcpVersion;
    private List<Instance> instances = new ArrayList<>();
    private List<McpToolInfo> tools = new ArrayList<>();
} 