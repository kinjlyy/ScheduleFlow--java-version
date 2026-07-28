package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.ExecutionStrategy;
import java.time.LocalDateTime;
import java.util.List;

public class ExecutionResponse {

    private Long eventId;
    private ExecutionStrategy executionStrategy;
    private String executedBy;
    private LocalDateTime executedAt;
    private long durationMs;
    private EventStatus status;
    private String summary;
    private int rescheduledCount;
    private int cancelledCount;
    private List<String> warnings;

    public ExecutionResponse() {}

    public ExecutionResponse(Long eventId, ExecutionStrategy executionStrategy, String executedBy,
                             LocalDateTime executedAt, long durationMs, EventStatus status,
                             String summary, int rescheduledCount, int cancelledCount, List<String> warnings) {
        this.eventId = eventId;
        this.executionStrategy = executionStrategy;
        this.executedBy = executedBy;
        this.executedAt = executedAt;
        this.durationMs = durationMs;
        this.status = status;
        this.summary = summary;
        this.rescheduledCount = rescheduledCount;
        this.cancelledCount = cancelledCount;
        this.warnings = warnings;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(ExecutionStrategy executionStrategy) { this.executionStrategy = executionStrategy; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getRescheduledCount() { return rescheduledCount; }
    public void setRescheduledCount(int rescheduledCount) { this.rescheduledCount = rescheduledCount; }

    public int getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
