package com.scheduleflow.dto;

import java.util.List;

public class TimetableExecutionResultDTO {

    private String status;
    private String summary;
    private int rescheduledCount;
    private int cancelledCount;
    private List<Long> rescheduledLectureIds;
    private List<Long> cancelledLectureIds;
    private List<String> warnings;

    public TimetableExecutionResultDTO() {}

    public TimetableExecutionResultDTO(String status, String summary, int rescheduledCount,
                                       int cancelledCount, List<Long> rescheduledLectureIds,
                                       List<Long> cancelledLectureIds, List<String> warnings) {
        this.status = status;
        this.summary = summary;
        this.rescheduledCount = rescheduledCount;
        this.cancelledCount = cancelledCount;
        this.rescheduledLectureIds = rescheduledLectureIds;
        this.cancelledLectureIds = cancelledLectureIds;
        this.warnings = warnings;
    }

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
