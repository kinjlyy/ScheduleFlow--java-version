package com.scheduleflow.event.dto;

import com.scheduleflow.event.enums.EventStatus;
import com.scheduleflow.event.enums.ExecutionStrategy;
import java.util.List;

public class ExecutionPlanResponse {

    private Long eventId;
    private ExecutionStrategy executionStrategy;
    private String summary;
    private ImpactAnalysisResponse impactAnalysis;
    private List<ImpactedLectureResponse> lecturesToReschedule;
    private List<ImpactedLectureResponse> lecturesToCancel;
    private List<String> warnings;
    private EventStatus status;

    public ExecutionPlanResponse() {}

    public ExecutionPlanResponse(Long eventId, ExecutionStrategy executionStrategy, String summary,
                                 ImpactAnalysisResponse impactAnalysis, List<ImpactedLectureResponse> lecturesToReschedule,
                                 List<ImpactedLectureResponse> lecturesToCancel, List<String> warnings, EventStatus status) {
        this.eventId = eventId;
        this.executionStrategy = executionStrategy;
        this.summary = summary;
        this.impactAnalysis = impactAnalysis;
        this.lecturesToReschedule = lecturesToReschedule;
        this.lecturesToCancel = lecturesToCancel;
        this.warnings = warnings;
        this.status = status;
    }

    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }

    public ExecutionStrategy getExecutionStrategy() { return executionStrategy; }
    public void setExecutionStrategy(ExecutionStrategy executionStrategy) { this.executionStrategy = executionStrategy; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public ImpactAnalysisResponse getImpactAnalysis() { return impactAnalysis; }
    public void setImpactAnalysis(ImpactAnalysisResponse impactAnalysis) { this.impactAnalysis = impactAnalysis; }

    public List<ImpactedLectureResponse> getLecturesToReschedule() { return lecturesToReschedule; }
    public void setLecturesToReschedule(List<ImpactedLectureResponse> lecturesToReschedule) { this.lecturesToReschedule = lecturesToReschedule; }

    public List<ImpactedLectureResponse> getLecturesToCancel() { return lecturesToCancel; }
    public void setLecturesToCancel(List<ImpactedLectureResponse> lecturesToCancel) { this.lecturesToCancel = lecturesToCancel; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public EventStatus getStatus() { return status; }
    public void setStatus(EventStatus status) { this.status = status; }
}
