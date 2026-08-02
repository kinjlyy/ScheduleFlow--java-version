package com.scheduleflow.event.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * FeignAuthInterceptor — Propagates the incoming {@code Authorization} header
 * from the current HTTP request context to all outbound Feign calls.
 *
 * <p>This ensures that when Event Service calls Timetable Service via Feign
 * (e.g. {@code /api/timetables/{id}/event-execution} or {@code /api/timetables/active}),
 * the Bearer token is forwarded and Timetable Service does not return 403 Forbidden.
 *
 * <p>Registered as a global Feign request interceptor via {@link Bean}.
 */
@Configuration
public class FeignAuthInterceptor {

    private static final Logger log = LoggerFactory.getLogger(FeignAuthInterceptor.class);

    @Bean
    public RequestInterceptor authorizationHeaderInterceptor() {
        return (RequestTemplate requestTemplate) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && !authHeader.isBlank()) {
                    requestTemplate.header("Authorization", authHeader);
                    log.debug("Feign: forwarding Authorization header to {}", requestTemplate.url());
                } else {
                    log.debug("Feign: no Authorization header present in current request context");
                }
            }
        };
    }
}
