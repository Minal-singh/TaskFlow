package com.minal.taskflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Array;
import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customConfig() {
        return new OpenAPI()
                .info(
                        new Info().title("TaskFlow App API")
                                .description("API documentation for TaskFlow App - a task management application")
                                .version("1.0")
                                .contact(new Contact()
                                        .name("Minal Singh"))
                ).addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization")
                        .description("Enter JWT token.")))
                .tags(List.of(
                        new Tag().name("Public APIs").description("Endpoints for user registration, login, and health check."),
                        new Tag().name("User APIs").description("Endpoints for managing user profiles."),
                        new Tag().name("Tasks APIs").description("Endpoints for creating, updating, retrieving, and deleting tasks.")
                             ));
    }
}
