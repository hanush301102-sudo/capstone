package com.creatorhire.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI creatorHireOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CreatorHire API")
                .version("0.1.0")
                .description("Style-first, verified, match-scored marketplace for hiring video editors, designers, and scriptwriters."));
    }
}
