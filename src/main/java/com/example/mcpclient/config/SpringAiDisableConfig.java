package com.example.mcpclient.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class to explicitly disable Spring AI features
 */
@Configuration
public class SpringAiDisableConfig {

    /**
     * Property to explicitly disable Spring AI OpenAI Auto Configuration
     */
    @Bean
    public String disableSpringAiOpenAi() {
        System.setProperty("spring.ai.openai.enabled", "false");
        System.setProperty("spring.ai.openai.audio.speech.enabled", "false");
        System.setProperty("spring.ai.openai.speech.enabled", "false");
        System.setProperty("spring.ai.openai.api-key", "dummy-key");
        System.setProperty("spring.ai.openai.audio.speech.api-key", "dummy-key");
        System.setProperty("spring.ai.openai.speech.api-key", "dummy-key");
        return "springAiDisabled";
    }
} 