package com.example.mcpclient.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * Configuration for LLM services
 */
@Configuration
public class LlmConfig {
    
    /**
     * Empty bean to prevent Spring Boot from trying to auto-configure OpenAI
     */
    @Bean
    @ConditionalOnMissingBean(name = "openAiAudioSpeechModel")
    public Object openAiAudioSpeechModel() {
        return new Object(); // Dummy object
    }
    
} 