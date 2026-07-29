package com.scheduleflow.resource.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration for ScheduleFlow Resource Service.
 *
 * <p>Swagger UI is available at: {@code /swagger-ui.html}
 * OpenAPI JSON spec is at: {@code /v3/api-docs}
 *
 * <p><strong>Server URL strategy:</strong> No explicit server list is declared here.
 * SpringDoc automatically derives the server URL from the incoming HTTP request
 * (scheme + host + port), which means:
 * <ul>
 *   <li>Local dev: Swagger executes against {@code http://localhost:8081}</li>
 *   <li>Render prod: Swagger executes against {@code https://scheduleflow-java-version-1.onrender.com}</li>
 * </ul>
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
                                .url("https://scheduleflow.com")));
        // No .servers() call — SpringDoc infers the base URL from the HTTP request,
        // so Swagger UI always targets the actual host serving the API.
    }
}
