package com.scheduleflow.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Custom OpenFeign Infrastructure Configuration for {@code RESOURCE-SERVICE}.
 *
 * <p>Phase 5 Microservice Migration:
 * Serves as the central, framework-decoupled configuration point for OpenFeign clients.
 *
 * <p><strong>Production Extension Points:</strong>
 * <ul>
 *   <li><b>Feign Logger Level:</b> Configures HTTP logging detail level.</li>
 *   <li><b>Connection & Read Timeouts:</b> Prevents thread starvation on slow network calls.</li>
 *   <li><b>Retry Strategy:</b> Extension point for custom {@link Retryer} (currently disabled to avoid cascading delays).</li>
 *   <li><b>Error Decoder:</b> Extension point for custom {@link ErrorDecoder} handling specific status codes.</li>
 *   <li><b>Request Interceptors:</b> Extension point for injecting authentication headers (e.g. JWT / API tokens).</li>
 *   <li><b>Metrics & Tracing:</b> Extension point for Micrometer / Zipkin distributed tracing.</li>
 * </ul>
 */
@Configuration
public class ResourceFeignConfiguration {

    /**
     * Configures Feign logging level. Use {@code Logger.Level.FULL} during debugging.
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Configures connect and read timeouts for HTTP calls.
     */
    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
                5, TimeUnit.SECONDS,   // Connect timeout (5s)
                10, TimeUnit.SECONDS,  // Read timeout (10s)
                true                   // Follow redirects
        );
    }

    /**
     * Configures Feign retryer. Default is NEVER_RETRY to prevent cascading latency spikes.
     */
    @Bean
    public Retryer feignRetryer() {
        return Retryer.NEVER_RETRY;
    }

    /*
     * ── Future Resilience Extension Points ─────────────────────────────────────
     *
     * @Bean
     * public ErrorDecoder customErrorDecoder() {
     *     return new ResourceErrorDecoder();
     * }
     *
     * @Bean
     * public RequestInterceptor bearerTokenInterceptor() {
     *     return template -> template.header("Authorization", "Bearer ...");
     * }
     */
}
