package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.ExecutionStrategy;

public class ExecutionRequest {

    private ExecutionStrategy executionStrategy;
    private String executedBy;

    public ExecutionRequest() {}

    public ExecutionRequest(ExecutionStrategy executionStrategy, String executedBy) {
        this.executionStrategy = executionStrategy;
        this.executedBy = executedBy;
    }

    public ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(ExecutionStrategy executionStrategy) { this.executionStrategy = executionStrategy; }

    public String getExecutedBy() { return executedBy; }
    public void setExecutedBy(String executedBy) { this.executedBy = executedBy; }
}
