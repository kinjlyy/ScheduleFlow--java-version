package com.scheduleflow.dto;

import java.util.List;
import java.util.Map;

public class TimetableResponseDTO {

    // sectionId -> day -> list of period cells
    private Map<String, Map<String, List<PeriodCell>>> timetable;
    private List<String> warnings;
    private TimetableStats stats;

    public TimetableResponseDTO() {}

    public Map<String, Map<String, List<PeriodCell>>> getTimetable() { return timetable; }
    public void setTimetable(Map<String, Map<String, List<PeriodCell>>> timetable) { this.timetable = timetable; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }

    public TimetableStats getStats() { return stats; }
    public void setStats(TimetableStats stats) { this.stats = stats; }

    // ---- Inner classes ----

    public static class PeriodCell {
        private String subject;
        private String teacher;
        private boolean free;

        public PeriodCell() {}

        public PeriodCell(String subject, String teacher) {
            this.subject = subject;
            this.teacher = teacher;
            this.free = false;
        }

        public static PeriodCell freeCell() {
            PeriodCell c = new PeriodCell();
            c.free = true;
            c.subject = "FREE";
            c.teacher = "";
            return c;
        }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getTeacher() { return teacher; }
        public void setTeacher(String teacher) { this.teacher = teacher; }

        public boolean isFree() { return free; }
        public void setFree(boolean free) { this.free = free; }
    }

    public static class TimetableStats {
        private int totalSections;
        private int totalScheduledLectures;
        private int totalFreePeriods;
        private int warningCount;
        private Map<String, Integer> teacherLoadMap; // teacher -> total lectures assigned

        public TimetableStats() {}

        public int getTotalSections() { return totalSections; }
        public void setTotalSections(int totalSections) { this.totalSections = totalSections; }

        public int getTotalScheduledLectures() { return totalScheduledLectures; }
        public void setTotalScheduledLectures(int totalScheduledLectures) { this.totalScheduledLectures = totalScheduledLectures; }

        public int getTotalFreePeriods() { return totalFreePeriods; }
        public void setTotalFreePeriods(int totalFreePeriods) { this.totalFreePeriods = totalFreePeriods; }

        public int getWarningCount() { return warningCount; }
        public void setWarningCount(int warningCount) { this.warningCount = warningCount; }

        public Map<String, Integer> getTeacherLoadMap() { return teacherLoadMap; }
        public void setTeacherLoadMap(Map<String, Integer> teacherLoadMap) { this.teacherLoadMap = teacherLoadMap; }
    }
}
