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

        // STEP 3.5 – Compaction: Slide lectures to earlier slots to fix gaps
        compactTimetable(nodes, days, periods);

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

        // Global occupancy tracking
        Map<String, Set<Integer>> teacherOccupied = new HashMap<>(); 
        Map<String, Set<Integer>> sectionOccupied = new HashMap<>();

        // --- PASS 1: Mandatory Variety (First 5 lectures of each subject: Mon-Fri) ---
        // This handles "At least 1 lecture everyday" priority.
        Set<Integer> uncoloredVariety = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            if (nodes.get(i).lectureIndex < days) uncoloredVariety.add(i);
        }
        processColoringPass(uncoloredVariety, nodes, saturation, days, periods, teacherOccupied, sectionOccupied, warnings);

        // --- PASS 2: Repeats (6th+ lectures) ---
        Set<Integer> uncoloredRepeats = new LinkedHashSet<>();
        for (int i = 0; i < n; i++) {
            if (nodes.get(i).assignedSlot == -1) uncoloredRepeats.add(i);
        }
        processColoringPass(uncoloredRepeats, nodes, saturation, days, periods, teacherOccupied, sectionOccupied, warnings);
    }

    private void processColoringPass(Set<Integer> uncolored, List<LectureNode> nodes, 
                                     List<Set<Integer>> saturation, int days, int periods, 
                                     Map<String, Set<Integer>> teacherOccupied, 
                                     Map<String, Set<Integer>> sectionOccupied, 
                                     List<String> warnings) {
        
        while (!uncolored.isEmpty()) {
            int chosen = pickHighestSaturation(uncolored, saturation, nodes);
            LectureNode node = nodes.get(chosen);

            List<Integer> orderedSlots = getDaySpreadSlots(
                    days, periods, node.lectureIndex,
                    node.sectionId, node.subject, sectionOccupied);

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
                                              String subject,
                                              Map<String, Set<Integer>> sectionOccupied) {

        Set<Integer> secSlots = sectionOccupied.getOrDefault(sectionId, Collections.emptySet());
        int[] dayLoad = new int[days];
        for (int slot : secSlots) dayLoad[slot / periods]++;

        // 1. Build day list
        List<Integer> dayOrder = new ArrayList<>();
        for (int d = 0; d < days; d++) dayOrder.add(d);

        // 2. Variety Priority: Prefer days that have NOT been used for this section yet
        dayOrder.sort((a, b) -> {
            int loadA = dayLoad[a];
            int loadB = dayLoad[b];
            if (loadA != loadB) return Integer.compare(loadA, loadB);
            
            // Tie: Use round-robin based on lectureIndex
            int pref = lectureIndex % days;
            int distA = (a - pref + days) % days;
            int distB = (b - pref + days) % days;
            return Integer.compare(distA, distB);
        });

        // 3. THE P1 RACE: Try Period 0 across all preferred days first, then Period 1...
        List<Integer> finalOrder = new ArrayList<>();
        for (int pIdx = 0; pIdx < periods; pIdx++) {
            for (int d : dayOrder) {
                finalOrder.add(d * periods + pIdx);
            }
        }
        return finalOrder;
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

    // ── STEP 3.5: Compaction (Hole-filling) ──────────────────────────────────

    // ── STEP 3.5: Compaction (Global Multi-Day P1 Optimizer) ────────────────

    private void compactTimetable(List<LectureNode> nodes, int days, int periods) {
        // Ultimate Optimizer: Pulls EVERY lecture to the earliest weekly slot building-wide
        // (Mon P1 > Tue P1 > ... > Mon P2 > Tue P2 > ...)
        // Priority order: Periods first, then Days. This forces maximum building-wide P1 density.
        
        boolean globallyMoved = true;
        int passCount = 0;
        while (globallyMoved && passCount++ < 50) {
            globallyMoved = false;
            
            // Build current building-wide state
            Map<String, Set<Integer>> teacherOccupied = new HashMap<>(); 
            Map<String, Set<Integer>> sectionOccupied = new HashMap<>(); 
            Map<String, Map<String, Set<Integer>>> sectionSubjectDays = new HashMap<>();

            for (LectureNode n : nodes) {
                if (n.assignedSlot >= 0) {
                    teacherOccupied.computeIfAbsent(n.teacher, k -> new HashSet<>()).add(n.assignedSlot);
                    sectionOccupied.computeIfAbsent(n.sectionId, k -> new HashSet<>()).add(n.assignedSlot);
                    sectionSubjectDays.computeIfAbsent(n.sectionId, k -> new HashMap<>())
                                      .computeIfAbsent(n.subject, k -> new HashSet<>()).add(n.assignedDay);
                }
            }

            // Create global priority order: P0 (all days), P1 (all days)...
            List<Integer> bestToWorstSlots = new ArrayList<>();
            for (int pIdx = 0; pIdx < periods; pIdx++) {
                for (int dIdx = 0; dIdx < days; dIdx++) {
                    bestToWorstSlots.add(dIdx * periods + pIdx);
                }
            }

            for (LectureNode n : nodes) {
                if (n.assignedSlot == -1) continue;

                int currentSlot = n.assignedSlot;
                int currentPriorityIndex = bestToWorstSlots.indexOf(currentSlot);

                // Try to find a slot with a lower PRIORITY index
                for (int i = 0; i < currentPriorityIndex; i++) {
                    int newSlot = bestToWorstSlots.get(i);
                    int newDay = newSlot / periods;

                    Set<Integer> tBusy = teacherOccupied.getOrDefault(n.teacher, Collections.emptySet());
                    Set<Integer> sBusy = sectionOccupied.getOrDefault(n.sectionId, Collections.emptySet());
                    if (tBusy.contains(newSlot) || sBusy.contains(newSlot)) continue;

                    // Variety: 1 per day check
                    if (newDay != n.assignedDay) {
                        Set<Integer> sjDays = sectionSubjectDays.get(n.sectionId).get(n.subject);
                        if (sjDays != null && sjDays.contains(newDay)) continue;

                        // MOVE BUILDING-WIDE!
                        teacherOccupied.get(n.teacher).remove(currentSlot);
                        teacherOccupied.get(n.teacher).add(newSlot);
                        sectionOccupied.get(n.sectionId).remove(currentSlot);
                        sectionOccupied.get(n.sectionId).add(newSlot);
                        sjDays.remove(n.assignedDay);
                        sjDays.add(newDay);
                    } else {
                        teacherOccupied.get(n.teacher).remove(currentSlot);
                        teacherOccupied.get(n.teacher).add(newSlot);
                        sectionOccupied.get(n.sectionId).remove(currentSlot);
                        sectionOccupied.get(n.sectionId).add(newSlot);
                    }

                    n.assignedSlot = newSlot;
                    n.assignedDay = newDay;
                    n.assignedPeriod = newSlot % periods;
                    globallyMoved = true;
                    break;
                }
            }
        }
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
