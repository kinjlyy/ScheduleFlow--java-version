package com.scheduleflow.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TimetableExecutionResultResponse {

    private String status;
    private String summary;
    private int rescheduledCount;
    private int cancelledCount;
    private List<Long> rescheduledLectureIds;
    private List<Long> cancelledLectureIds;
    private List<String> warnings;

    public TimetableExecutionResultResponse() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public int getRescheduledCount() { return rescheduledCount; }
    public void setRescheduledCount(int rescheduledCount) { this.rescheduledCount = rescheduledCount; }

    public int getCancelledCount() { return cancelledCount; }
    public void setCancelledCount(int cancelledCount) { this.cancelledCount = cancelledCount; }

    public List<Long> getRescheduledLectureIds() { return rescheduledLectureIds; }
    public void setRescheduledLectureIds(List<Long> rescheduledLectureIds) { this.rescheduledLectureIds = rescheduledLectureIds; }

    public List<Long> getCancelledLectureIds() { return cancelledLectureIds; }
    public void setCancelledLectureIds(List<Long> cancelledLectureIds) { this.cancelledLectureIds = cancelledLectureIds; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
