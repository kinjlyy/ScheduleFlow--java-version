package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.ExecutionStrategy;
import java.time.LocalDateTime;

public class ExecutionHistoryResponse {

    private Long eventId;
    private EventStatus status;
    private ExecutionStrategy executionStrategy;
    private String executedBy;
    private LocalDateTime executionStartedAt;
    private LocalDateTime executionCompletedAt;
    private String executionSummary;
    private String executionResult;

    public ExecutionHistoryResponse() {}

    public ExecutionHistoryResponse(Long eventId, EventStatus status, ExecutionStrategy executionStrategy,
                                    String executedBy, LocalDateTime executionStartedAt,
                                    LocalDateTime executionCompletedAt, String executionSummary,
                                    String executionResult) {
        this.eventId = eventId;
        this.status = status;
        this.executionStrategy = executionStrategy;
        this.executedBy = executedBy;
        this.executionStartedAt = executionStartedAt;
        this.executionCompletedAt = executionCompletedAt;
        this.executionSummary = executionSummary;
        this.executionResult = executionResult;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(ExecutionStrategy executionStrategy) { this.executionStrategy = executionStrategy; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    public LocalDateTime getExecutionStartedAt() { return executionStartedAt; }
    public void setExecutionStartedAt(LocalDateTime executionStartedAt) { this.executionStartedAt = executionStartedAt; }

    public LocalDateTime getExecutionCompletedAt() { return executionCompletedAt; }
    public void setExecutionCompletedAt(LocalDateTime executionCompletedAt) { this.executionCompletedAt = executionCompletedAt; }

    public String getExecutionSummary() { return executionSummary; }
    public void setExecutionSummary(String executionSummary) { this.executionSummary = executionSummary; }

    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }
}
