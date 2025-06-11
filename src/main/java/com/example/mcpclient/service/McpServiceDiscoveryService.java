package com.example.mcpclient.service;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.example.mcpclient.model.McpServiceInfo;
import com.example.mcpclient.model.McpToolInfo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service responsible for discovering MCP services and tools from Nacos.
 */
@Service
@Slf4j
public class McpServiceDiscoveryService {

    @Value("${nacos.server-addr}")
    private String nacosAddress;

    @Value("${nacos.namespace}")
    private String nacosNamespace;

    @Value("${nacos.mcp.group}")
    private String mcpGroup;
    
    @Value("${mcp.client.domains}")
    private List<String> targetDomains;

    private NamingService namingService;

    // Map of service name to service info
    private final Map<String, McpServiceInfo> mcpServices = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            log.info("Initializing MCP Service Discovery with Nacos server: {}", nacosAddress);
            
            // Initialize Nacos naming service
            Properties properties = new Properties();
            properties.put("serverAddr", nacosAddress);
            properties.put("namespace", nacosNamespace);
            namingService = NacosFactory.createNamingService(properties);
            
            // Discover services immediately on startup
            discoverMcpServices();
            
            log.info("MCP Service Discovery initialized successfully");
        } catch (NacosException e) {
            log.error("Failed to initialize MCP Service Discovery", e);
        }
    }

    /**
     * Periodically discover MCP services from Nacos
     */
    @Scheduled(fixedDelayString = "${mcp.client.refresh-interval-ms}")
    public void refreshServices() {
        log.debug("Refreshing MCP services from Nacos");
        discoverMcpServices();
    }

    /**
     * Discover all MCP services from Nacos
     */
    private void discoverMcpServices() {
        try {
            log.debug("Discovering MCP services from Nacos");
            
            // Get all services in the MCP group
            ListView<String> serviceListView = namingService.getServicesOfServer(1, Integer.MAX_VALUE, mcpGroup);
            List<String> serviceNames = serviceListView.getData();
            log.debug("Found {} services in MCP group", serviceNames.size());
            
            // Filter services based on target domains
            List<String> filteredServices = filterServicesByDomains(serviceNames);
            log.debug("Filtered to {} services based on target domains", filteredServices.size());
            
            // Process each service to extract MCP information
            for (String serviceName : filteredServices) {
                try {
                    List<Instance> instances = namingService.getAllInstances(serviceName, mcpGroup);
                    if (!instances.isEmpty()) {
                        McpServiceInfo serviceInfo = createServiceInfo(serviceName, instances);
                        mcpServices.put(serviceName, serviceInfo);
                        log.debug("Processed MCP service: {} with {} tools", serviceName, serviceInfo.getTools().size());
                    }
                } catch (Exception e) {
                    log.error("Error processing service {}: {}", serviceName, e.getMessage());
                }
            }
            
            log.info("Discovered {} MCP services with a total of {} tools", 
                     mcpServices.size(), getAllTools().size());
        } catch (NacosException e) {
            log.error("Failed to discover MCP services from Nacos", e);
        }
    }

    /**
     * Filter services based on target domains
     */
    private List<String> filterServicesByDomains(List<String> serviceNames) {
        if (targetDomains == null || targetDomains.isEmpty()) {
            return serviceNames;
        }
        
        List<String> filteredServices = new ArrayList<>();
        for (String serviceName : serviceNames) {
            for (String domain : targetDomains) {
                if (serviceName.toLowerCase().contains(domain.toLowerCase())) {
                    filteredServices.add(serviceName);
                    break;
                }
            }
        }
        return filteredServices;
    }

    /**
     * Create service info from Nacos instances
     */
    private McpServiceInfo createServiceInfo(String serviceName, List<Instance> instances) {
        McpServiceInfo serviceInfo = new McpServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setInstances(instances);
        
        // Extract domain from service name (e.g., "userCenter-mcp" -> "userCenter")
        String domain = serviceName.replace("-mcp", "");
        serviceInfo.setDomain(domain);
        
        // Parse tool information from metadata
        Instance instance = instances.get(0); // Get first instance
        Map<String, String> metadata = instance.getMetadata();
        
        // Set service protocol and version
        serviceInfo.setProtocol(metadata.getOrDefault("protocol", "MCP"));
        serviceInfo.setMcpVersion(metadata.getOrDefault("mcp-version", "v1alpha1"));
        
        // Get number of tools
        String toolsCountStr = metadata.get("mcp-tools-count");
        if (toolsCountStr != null) {
            int toolsCount = Integer.parseInt(toolsCountStr);
            
            // Iterate through tools and extract info
            for (int i = 0; i < toolsCount; i++) {
                String toolName = metadata.get("tool-" + i + "-name");
                String toolDesc = metadata.get("tool-" + i + "-description");
                
                if (toolName != null) {
                    McpToolInfo toolInfo = new McpToolInfo();
                    toolInfo.setName(toolName);
                    toolInfo.setDescription(toolDesc);
                    toolInfo.setServiceName(serviceName);
                    toolInfo.setDomain(domain);
                    
                    // Create connection details
                    Map<String, String> connectionDetails = new HashMap<>();
                    connectionDetails.put("protocol", metadata.getOrDefault("protocol", "MCP"));
                    connectionDetails.put("version", metadata.getOrDefault("mcp-version", "v1alpha1"));
                    connectionDetails.put("ip", instance.getIp());
                    connectionDetails.put("port", String.valueOf(instance.getPort()));
                    connectionDetails.put("domain", domain);
                    connectionDetails.put("serviceName", serviceName);
                    toolInfo.setConnectionDetails(connectionDetails);
                    
                    serviceInfo.getTools().add(toolInfo);
                }
            }
        }
        
        return serviceInfo;
    }

    /**
     * Get all discovered MCP services
     */
    public List<McpServiceInfo> getAllServices() {
        return new ArrayList<>(mcpServices.values());
    }

    /**
     * Get all discovered MCP tools
     */
    public List<McpToolInfo> getAllTools() {
        List<McpToolInfo> allTools = new ArrayList<>();
        mcpServices.values().forEach(service -> allTools.addAll(service.getTools()));
        return allTools;
    }

    /**
     * Get tools filtered by domain
     */
    public List<McpToolInfo> getToolsByDomain(String domain) {
        List<McpToolInfo> filteredTools = new ArrayList<>();
        mcpServices.values().stream()
            .filter(service -> domain == null || service.getDomain().equalsIgnoreCase(domain))
            .forEach(service -> filteredTools.addAll(service.getTools()));
        return filteredTools;
    }

    /**
     * Get service by name
     */
    public McpServiceInfo getServiceByName(String serviceName) {
        return mcpServices.get(serviceName);
    }

    /**
     * Get tool by name
     */
    public McpToolInfo getToolByName(String toolName) {
        return getAllTools().stream()
                .filter(tool -> tool.getName().equals(toolName))
                .findFirst()
                .orElse(null);
    }

    /**
     * Force refresh of services
     */
    public int forceRefresh() {
        discoverMcpServices();
        return mcpServices.size();
    }
} 