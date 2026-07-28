package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.ExecutionStrategy;
import jakarta.validation.constraints.NotNull;

public class ExecutionPlanRequest {

    @NotNull(message = "eventId is required")
    private Long eventId;

    @NotNull(message = "executionStrategy is required")
    private ExecutionStrategy executionStrategy;

    public ExecutionPlanRequest() {}

    public ExecutionPlanRequest(Long eventId, ExecutionStrategy executionStrategy) {
        this.eventId = eventId;
        this.executionStrategy = executionStrategy;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(ExecutionStrategy executionStrategy) { this.executionStrategy = executionStrategy; }
}
