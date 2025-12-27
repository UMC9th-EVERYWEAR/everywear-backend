package com.umc.EveryWear.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI swagger() {
        Info info = new Info().title("Every Wear API").description("Every Wear 백엔드 API 문서").version("v1.0.0");

        return new OpenAPI()
                .info(info);
    }
}