package com.scheduleflow.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Local (log-based) implementation of {@link AuditPublisher}.
 *
 * <p>Writes audit entries to the application log via SLF4J at INFO level.
 * This provides a minimal but functional audit trail visible in any log aggregator
 * (e.g., AWS CloudWatch, Render, or a local file) without requiring an external service.
 *
 * <p><strong>Replacement strategy:</strong>
 * Implement {@link AuditPublisher} to write to an audit database table or external service.
 * Annotate with {@code @Primary} to override this bean. Service layer is unchanged.
 */
@Component
@Primary
public class LocalAuditPublisher implements AuditPublisher {

    private static final Logger log = LoggerFactory.getLogger(LocalAuditPublisher.class);

    @Override
    public void audit(String actor, String action, String detail) {
        log.info("[AUDIT] actor={} | action={} | detail={}", actor, action, detail);
    }
}
