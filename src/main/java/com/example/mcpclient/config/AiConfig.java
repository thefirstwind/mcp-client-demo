package com.example.mcpclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configuration class to provide dummy beans for Spring AI components
 * to prevent auto-configuration issues
 */
@Configuration
public class AiConfig {

    @Bean
    @Primary
    public Object openAiAudioSpeechModel() {
        return new DummyAiModel();
    }

    @Bean
    @Primary
    public Object openAiChatModel() {
        return new DummyAiModel();
    }

    @Bean
    @Primary
    public Object openAiEmbeddingModel() {
        return new DummyAiModel();
    }

    @Bean
    @Primary
    public Object openAiImageModel() {
        return new DummyAiModel();
    }

    @Bean
    @Primary
    public Object openAiTranscriptionModel() {
        return new DummyAiModel();
    }

    @Bean
    @Primary
    public Object openAiApi() {
        return new DummyAiModel();
    }

    /**
     * Dummy class to satisfy Spring AI dependencies
     */
    public static class DummyAiModel {
        public List<?> generate(Object prompt) {
            return Collections.emptyList();
        }

        public Map<String, Object> getProperties() {
            return Collections.emptyMap();
        }

        public void setProperties(Map<String, Object> properties) {
            // Do nothing
        }
    }
} 