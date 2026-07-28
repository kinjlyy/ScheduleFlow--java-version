package com.scheduleflow.resource.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for ScheduleFlow Resource Service.
 *
 * <p>Swagger UI is available at: {@code /swagger-ui.html}
 * OpenAPI JSON spec is at: {@code /v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI resourceServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScheduleFlow Resource Service API")
                        .description("""
                                REST API for the ScheduleFlow Resource Service.

                                **Covers:**
                                - Room CRUD management (classrooms, labs, seminar halls, auditoriums)
                                - Active room queries for availability checking
                                - Room summary and capacity-based filtering

                                **Consumers:**
                                EVENT-SERVICE calls this service via OpenFeign to validate rooms during
                                room reservation and availability checking workflows.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ScheduleFlow Team")
                                .email("support@scheduleflow.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://scheduleflow.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development"),
                        new Server().url("https://scheduleflow-resource.onrender.com").description("Production (Render)")
                ));
    }
}
