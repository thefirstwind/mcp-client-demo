package com.example.mcpclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Main application class for the MCP Client Demo
 * This class explicitly excludes Spring AI OpenAI auto-configurations to prevent startup issues
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class
})
@ComponentScan(basePackages = {"com.example.mcpclient"})
@EnableScheduling
public class McpClientDemoApplication {
    
    static {
        // Explicitly disable OpenAI features via system properties
        System.setProperty("spring.ai.openai.enabled", "false");
        System.setProperty("spring.ai.openai.audio.speech.enabled", "false");
        System.setProperty("spring.ai.openai.speech.enabled", "false");
        System.setProperty("spring.ai.openai.api-key", "dummy-key");
        System.setProperty("spring.ai.openai.audio.speech.api-key", "dummy-key");
        System.setProperty("spring.ai.openai.speech.api-key", "dummy-key");
        System.setProperty("spring.autoconfigure.exclude", 
            "org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration," +
            "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration," +
            "org.springframework.ai.model.openai.autoconfigure.OpenAiAutoConfiguration");
    }
    
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(McpClientDemoApplication.class);
        
        // Add additional properties to disable Spring AI OpenAI auto-configuration
        app.setDefaultProperties(java.util.Collections.singletonMap(
            "spring.autoconfigure.exclude", 
            "org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration," +
            "org.springframework.ai.model.openai.autoconfigure.OpenAiAudioSpeechAutoConfiguration," +
            "org.springframework.ai.model.openai.autoconfigure.OpenAiAutoConfiguration"
        ));
        
        app.run(args);
    }
} 