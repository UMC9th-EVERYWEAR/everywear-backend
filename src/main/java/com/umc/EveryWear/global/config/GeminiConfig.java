package com.umc.EveryWear.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gemini API와의 통신을 위한 설정 클래스입니다.
 */
@Configuration
public class GeminiConfig {

    @Bean
    Client geminiClient(@Value("${gemini.api.key}") String apiKey) {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
