package com.scheduleflow.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI configuration for ScheduleFlow Event Service.
 *
 * <p>Swagger UI is available at: {@code /swagger-ui.html}
 * OpenAPI JSON spec is at: {@code /v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ScheduleFlow Event Service API")
                        .description("""
                                REST API for the ScheduleFlow Event Service.

                                **Covers:**
                                - Phase 7A: General Event Management (CRUD)
                                - Phase 7B: Room Reservation & Availability Checking
                                - Phase 7C: Academic Event Scheduling, Timetable Impact Analysis & Execution

                                **Architecture:**
                                EVENT-SERVICE orchestrates across RESOURCE-SERVICE (room metadata) and
                                TIMETABLE-SERVICE (lecture impact & execution).
                                All inter-service calls use Spring Cloud OpenFeign with 5s connect/read timeouts.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ScheduleFlow Team")
                                .email("support@scheduleflow.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://scheduleflow.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8083").description("Local Development"),
                        new Server().url("https://scheduleflow-event.onrender.com").description("Production (Render)")
                ));
    }
}
