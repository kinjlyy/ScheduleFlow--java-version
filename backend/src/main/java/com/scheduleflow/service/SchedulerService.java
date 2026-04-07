package com.scheduleflow.service;

import com.scheduleflow.dto.*;
import com.scheduleflow.dto.TimetableResponseDTO.PeriodCell;
import com.scheduleflow.dto.TimetableResponseDTO.TimetableStats;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * SchedulerService — Graph Coloring-based timetable scheduler.
 *
 * KEY DISTRIBUTION RULE (updated):
 *   For every subject, spread its lectures across ALL days first before
 *   repeating any day.  Example: 8 lectures over 5 days → Mon,Tue,Wed,Thu,Fri
 *   get 1 lecture each (5 placed), then the remaining 3 go to Mon,Tue,Wed.
 *   This prevents "all Maths on Monday" clustering.
 *
 * ALGORITHM:
 *   1. Build LectureNodes, one per lecture occurrence.
 *   2. Build a conflict graph (same teacher OR same section → edge).
 *   3. DSatur graph-coloring assigns a (day,period) slot to each node,
 *      using a "day-spread-first" slot ordering so that each subject
 *      visits every day before revisiting any day.
 *   4. Build the result grid and fill empties with FREE.
 */
@Service
public class SchedulerService {

    private static final String[] DAYS = {
        "Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"
    };

    // ── Public entry point ────────────────────────────────────────────────────

    public TimetableResponseDTO generate(TimetableRequestDTO request) {
        int days    = Math.min(request.getDaysPerWeek(), 7);
        int periods = request.getPeriodsPerDay();
        int maxPerSection = days * periods;
        Map<String, Integer> teacherMax = request.getTeacherMaxLectures() != null
                ? request.getTeacherMaxLectures() : new HashMap<>();

        List<String> warnings = new ArrayList<>();

        // STEP 1 – build nodes
        List<LectureNode> nodes = buildLectureNodes(
                request.getSections(), maxPerSection, teacherMax, warnings);

        // STEP 2 – build conflict graph
        buildConflictEdges(nodes);

        // STEP 3 – DSatur coloring with day-spread slot ordering
        colorGraph(nodes, days, periods, teacherMax, warnings);

        // STEP 4 – fill result grid
        Map<String, Map<String, List<PeriodCell>>> timetable =
                buildGrid(request.getSections(), nodes, days, periods);

        // STEP 5 – stats
        TimetableStats stats = computeStats(
                timetable, request.getSections(), days, periods, nodes, warnings);

        TimetableResponseDTO response = new TimetableResponseDTO();
        response.setTimetable(timetable);
        response.setWarnings(warnings);
        response.setStats(stats);
        return response;
    }

    // ── STEP 1: build lecture nodes ───────────────────────────────────────────

    private List<LectureNode> buildLectureNodes(
            List<SectionDTO> sections, int maxPerSection,
            Map<String, Integer> teacherMax, List<String> warnings) {

        List<LectureNode> nodes = new ArrayList<>();
        Map<String, Integer> teacherRequested = new HashMap<>();

        for (SectionDTO sec : sections) {
            if (sec.getMappings() == null) continue;

            int sectionTotal = sec.getMappings().stream()
                    .mapToInt(SubjectMappingDTO::getLecturesPerWeek).sum();
            if (sectionTotal > maxPerSection) {
                warnings.add(String.format(
                    "Section '%s': requested %d lectures but max is %d. Excess dropped.",
                    sec.getName(), sectionTotal, maxPerSection));
            }

            int placed = 0;
            for (SubjectMappingDTO m : sec.getMappings()) {
                String teacher  = m.getTeacher();
                int    requested = m.getLecturesPerWeek();

                int tMax      = teacherMax.getOrDefault(teacher, 999);
                int alreadyReq = teacherRequested.getOrDefault(teacher, 0);
                int canTake   = Math.max(0, tMax - alreadyReq);
                if (requested > canTake) {
                    warnings.add(String.format(
                        "Teacher '%s': requested %d for '%s/%s' but cap allows only %d more.",
                        teacher, requested, sec.getName(), m.getSubject(), canTake));
                    requested = canTake;
                }

                int toAdd = Math.min(requested, maxPerSection - placed);
                for (int i = 0; i < toAdd; i++) {
                    // lectureIndex within this subject (0-based) used for day-spread ordering
                    nodes.add(new LectureNode(
                            sec.getId(), sec.getName(), m.getSubject(), teacher, i));
                }
                teacherRequested.merge(teacher, toAdd, Integer::sum);
                placed += toAdd;
            }
        }
        return nodes;
    }

    // ── STEP 2: build conflict edges ──────────────────────────────────────────

    private void buildConflictEdges(List<LectureNode> nodes) {
        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                LectureNode a = nodes.get(i), b = nodes.get(j);
                // Conflict: same teacher (if not empty) OR same section (can't be in same slot)
                boolean teacherConflict = !a.teacher.isEmpty() && a.teacher.equals(b.teacher);
                boolean sectionConflict = a.sectionId.equals(b.sectionId);
                
                if (teacherConflict || sectionConflict) {
                    a.neighbors.add(j);
                    b.neighbors.add(i);
                }
            }
        }
    }

    // ── STEP 3: DSatur graph coloring ─────────────────────────────────────────

    private void colorGraph(List<LectureNode> nodes,
                             int days, int periods,
                             Map<String, Integer> teacherMax,
                             List<String> warnings) {

        int n = nodes.size();
        List<Set<Integer>> saturation = new ArrayList<>();
        for (int i = 0; i < n; i++) saturation.add(new HashSet<>());

        Set<Integer> uncolored = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) uncolored.add(i);

        // Global occupancy tracking (across all sections / teachers)
        Map<String, Set<Integer>> teacherOccupied = new HashMap<>(); // teacher -> occupied slots
        Map<String, Set<Integer>> sectionOccupied = new HashMap<>(); // sectionId -> occupied slots

        while (!uncolored.isEmpty()) {
            int chosen = pickHighestSaturation(uncolored, saturation, nodes);
            LectureNode node = nodes.get(chosen);

            // ── Day-spread slot ordering ─────────────────────────────────────
            // Priority: put lecture index i on day (i % days) first, then wrap.
            // This ensures subject lecture #0 → day 0, #1 → day 1, … #days → day 0 again.
            // Within each preferred day, try periods 0..periods-1 in order.
            List<Integer> orderedSlots = getDaySpreadSlots(
                    days, periods, node.lectureIndex,
                    node.sectionId, sectionOccupied);

            int assignedSlot = -1;
            for (int slot : orderedSlots) {
                if (saturation.get(chosen).contains(slot)) continue;
                Set<Integer> tSlots = teacherOccupied.computeIfAbsent(node.teacher, k -> new HashSet<>());
                Set<Integer> sSlots = sectionOccupied.computeIfAbsent(node.sectionId, k -> new HashSet<>());
                if (!tSlots.contains(slot) && !sSlots.contains(slot)) {
                    assignedSlot = slot;
                    break;
                }
            }

            if (assignedSlot == -1) {
                warnings.add(String.format(
                    "Could not schedule '%s' for section '%s' (teacher: %s). No valid slot.",
                    node.subject, node.sectionName, node.teacher));
            } else {
                node.assignedSlot   = assignedSlot;
                node.assignedDay    = assignedSlot / periods;
                node.assignedPeriod = assignedSlot % periods;

                teacherOccupied.computeIfAbsent(node.teacher, k -> new HashSet<>()).add(assignedSlot);
                sectionOccupied.computeIfAbsent(node.sectionId, k -> new HashSet<>()).add(assignedSlot);

                for (int nb : node.neighbors) saturation.get(nb).add(assignedSlot);
            }
            uncolored.remove(chosen);
        }
    }

    /**
     * Day-spread slot ordering.
     *
     * For lecture index i of a subject:
     *   preferredDay = i % days   (round-robin across days)
     *
     * We build the candidate list as:
     *   [preferredDay p0..pN-1,  preferredDay+1 p0..pN-1,  …  all days]
     *
     * Then within each day we sort by earliest available period.
     * Days that are already full (all periods taken for this section) get
     * pushed to the back so we don't waste slots.
     *
     * Result: lectures spread evenly across days; only when a day is "full"
     * for this section does the algorithm spill to another.
     */
    private List<Integer> getDaySpreadSlots(int days, int periods,
                                              int lectureIndex,
                                              String sectionId,
                                              Map<String, Set<Integer>> sectionOccupied) {

        Set<Integer> secSlots = sectionOccupied.getOrDefault(sectionId, Collections.emptySet());

        // Count how many periods are taken per day for this section
        int[] dayLoad = new int[days];
        for (int slot : secSlots) dayLoad[slot / periods]++;

        int preferredDay = lectureIndex % days;

        // Build day order: start at preferredDay, wrap around
        List<Integer> dayOrder = new ArrayList<>();
        for (int offset = 0; offset < days; offset++) {
            int d = (preferredDay + offset) % days;
            dayOrder.add(d);
        }

        // Sort: days with fewer lectures first (prefer emptier days for spreading)
        // But keep the round-robin preferred day first if it still has capacity
        dayOrder.sort((a, b) -> {
            boolean aHasRoom = dayLoad[a] < periods;
            boolean bHasRoom = dayLoad[b] < periods;
            if (aHasRoom && !bHasRoom) return -1;
            if (!aHasRoom && bHasRoom) return  1;
            // Both have room: prefer the one with fewer lectures (spread evenly)
            int cmp = Integer.compare(dayLoad[a], dayLoad[b]);
            if (cmp != 0) return cmp;
            // Tie: keep original round-robin offset order. 
            // Since dayOrder was built in the preferred order, we can just return 0 
            // for a stable sort to preserve that order.
            return 0;
        });

        // Build final slot list: within each day, prefer earlier periods
        List<Integer> result = new ArrayList<>();
        for (int d : dayOrder) {
            for (int p = 0; p < periods; p++) {
                result.add(d * periods + p);
            }
        }
        return result;
    }

    private int pickHighestSaturation(Set<Integer> uncolored,
                                       List<Set<Integer>> saturation,
                                       List<LectureNode> nodes) {
        int best = -1, bestSat = -1, bestDeg = -1;
        for (int idx : uncolored) {
            int sat = saturation.get(idx).size();
            int deg = nodes.get(idx).neighbors.size();
            if (sat > bestSat || (sat == bestSat && deg > bestDeg)) {
                best = idx; bestSat = sat; bestDeg = deg;
            }
        }
        return best;
    }

    // ── STEP 4: build result grid ─────────────────────────────────────────────

    private Map<String, Map<String, List<PeriodCell>>> buildGrid(
            List<SectionDTO> sections, List<LectureNode> nodes, int days, int periods) {

        Map<String, Map<String, List<PeriodCell>>> result = new LinkedHashMap<>();
        for (SectionDTO sec : sections) {
            Map<String, List<PeriodCell>> secGrid = new LinkedHashMap<>();
            for (int d = 0; d < days; d++) {
                List<PeriodCell> row = new ArrayList<>(Collections.nCopies(periods, null));
                secGrid.put(DAYS[d], row);
            }
            result.put(sec.getId(), secGrid);
        }

        for (LectureNode node : nodes) {
            if (node.assignedSlot < 0) continue;
            Map<String, List<PeriodCell>> secGrid = result.get(node.sectionId);
            if (secGrid == null) continue;
            List<PeriodCell> row = secGrid.get(DAYS[node.assignedDay]);
            if (row != null && node.assignedPeriod < row.size())
                row.set(node.assignedPeriod, new PeriodCell(node.subject, node.teacher));
        }

        // Fill gaps with FREE
        result.forEach((id, grid) -> grid.forEach((day, row) -> {
            for (int p = 0; p < row.size(); p++)
                if (row.get(p) == null) row.set(p, PeriodCell.freeCell());
        }));

        return result;
    }

    // ── STEP 5: statistics ────────────────────────────────────────────────────

    private TimetableStats computeStats(
            Map<String, Map<String, List<PeriodCell>>> timetable,
            List<SectionDTO> sections, int days, int periods,
            List<LectureNode> nodes, List<String> warnings) {

        int scheduled = 0, free = 0;
        for (Map<String, List<PeriodCell>> g : timetable.values())
            for (List<PeriodCell> row : g.values())
                for (PeriodCell c : row) { if (c.isFree()) free++; else scheduled++; }

        Map<String, Integer> teacherLoad = new LinkedHashMap<>();
        for (LectureNode node : nodes)
            if (node.assignedSlot >= 0) teacherLoad.merge(node.teacher, 1, Integer::sum);

        TimetableStats stats = new TimetableStats();
        stats.setTotalSections(sections.size());
        stats.setTotalScheduledLectures(scheduled);
        stats.setTotalFreePeriods(free);
        stats.setWarningCount(warnings.size());
        stats.setTeacherLoadMap(teacherLoad);
        return stats;
    }

    // ── Internal node ─────────────────────────────────────────────────────────

    private static class LectureNode {
        String sectionId, sectionName, subject, teacher;
        int    lectureIndex; // 0-based index within this subject (drives day-spread)
        Set<Integer> neighbors = new HashSet<>();
        int assignedSlot = -1, assignedDay = -1, assignedPeriod = -1;

        LectureNode(String sectionId, String sectionName,
                    String subject, String teacher, int lectureIndex) {
            this.sectionId    = sectionId;
            this.sectionName  = sectionName;
            this.subject      = subject;
            this.teacher      = teacher;
            this.lectureIndex = lectureIndex;
        }
    }
}
