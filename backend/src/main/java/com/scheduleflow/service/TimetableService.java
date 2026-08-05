package com.scheduleflow.service;

import com.scheduleflow.dto.*;
import com.scheduleflow.exception.ResourceNotFoundException;
import com.scheduleflow.model.*;
import com.scheduleflow.repository.LectureRepository;
import com.scheduleflow.repository.TimetableRepository;
import com.scheduleflow.scheduler.RoomProvider;
import com.scheduleflow.util.TimetableConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * TimetableService — Manages timetable versioning, persistence, and lifecycle.
 *
 * Delegates scheduling logic entirely to SchedulerService.
 * Wraps generation in a single transaction with archive-on-success semantics.
 */
@Service
public class TimetableService {

    private static final Logger log = LoggerFactory.getLogger(TimetableService.class);
    private static final String[] DAYS = TimetableConstants.DAYS;

    private final SchedulerService schedulerService;
    private final TimetableRepository timetableRepository;
    private final LectureRepository lectureRepository;
    private final RoomProvider roomProvider;

    public TimetableService(SchedulerService schedulerService,
                            TimetableRepository timetableRepository,
                            LectureRepository lectureRepository,
                            RoomProvider roomProvider) {
        this.schedulerService = schedulerService;
        this.timetableRepository = timetableRepository;
        this.lectureRepository = lectureRepository;
        this.roomProvider = roomProvider;
    }

    // ── Generate and Persist ──────────────────────────────────────────────────

    @Transactional
    public TimetableResponseDTO generateAndPersistTimetable(TimetableRequestDTO request) {
        // 1. Create timetable record in GENERATING status
        Timetable timetable = new Timetable();
        timetable.setName("Timetable " + LocalDateTime.now().toString());
        timetable.setSemester("Current");
        timetable.setAcademicYear(String.valueOf(LocalDateTime.now().getYear()));
        timetable.setGeneratedAt(LocalDateTime.now());
        timetable.setStatus(TimetableStatus.GENERATING);
        timetable.setCreatedAt(LocalDateTime.now());
        timetable = timetableRepository.save(timetable);

        // 2. Run the scheduling engine (unchanged)
        TimetableResponseDTO response = schedulerService.generate(request);

        // 3. If the scheduler returned results, persist lectures
        if (response.getTimetable() != null && !response.getTimetable().isEmpty()) {
            // Build room lookup maps (id -> Room and roomNumber -> Room) from active database rooms
            List<Room> dbRooms = roomProvider.findAllActiveRooms();
            Map<Long, Room> roomLookupById = new HashMap<>();
            Map<String, Room> roomLookupByNumber = new HashMap<>();
            if (dbRooms != null) {
                for (Room r : dbRooms) {
                    if (r.getId() != null) roomLookupById.put(r.getId(), r);
                    if (r.getRoomNumber() != null) roomLookupByNumber.put(r.getRoomNumber().trim().toUpperCase(), r);
                }
            }

            // Build section mapping lookup for lectureType
            Map<String, Map<String, SubjectMappingDTO>> sectionMappingLookup = new HashMap<>();
            if (request.getSections() != null) {
                for (SectionDTO sec : request.getSections()) {
                    Map<String, SubjectMappingDTO> subjMap = new HashMap<>();
                    if (sec.getMappings() != null) {
                        for (SubjectMappingDTO m : sec.getMappings()) {
                            subjMap.put(m.getSubject(), m);
                        }
                    }
                    sectionMappingLookup.put(sec.getId(), subjMap);
                }
            }

            // Build section fixedRoomId lookup
            Map<String, Long> sectionFixedRoom = new HashMap<>();
            if (request.getSections() != null) {
                for (SectionDTO sec : request.getSections()) {
                    if (sec.getFixedRoomId() != null) {
                        sectionFixedRoom.put(sec.getId(), sec.getFixedRoomId());
                    }
                }
            }

            LocalDateTime now = LocalDateTime.now();
            List<Lecture> lectures = new ArrayList<>();

            for (Map.Entry<String, Map<String, List<TimetableResponseDTO.PeriodCell>>> secEntry
                    : response.getTimetable().entrySet()) {
                String sectionId = secEntry.getKey();

                for (Map.Entry<String, List<TimetableResponseDTO.PeriodCell>> dayEntry
                        : secEntry.getValue().entrySet()) {
                    String day = dayEntry.getKey();
                    List<TimetableResponseDTO.PeriodCell> cells = dayEntry.getValue();

                    for (int slot = 0; slot < cells.size(); slot++) {
                        TimetableResponseDTO.PeriodCell cell = cells.get(slot);
                        if (cell.isFree()) continue;

                        // Determine lectureType from mapping
                        LectureType lt = LectureType.THEORY;
                        Map<String, SubjectMappingDTO> subjMap = sectionMappingLookup.get(sectionId);
                        if (subjMap != null && subjMap.containsKey(cell.getSubject())) {
                            LectureType mapped = subjMap.get(cell.getSubject()).getLectureType();
                            if (mapped != null) lt = mapped;
                        }

                        // Determine room (from cell roomId, fixedRoom, or roomNumber)
                        Room room = null;
                        if (cell.getRoomId() != null) {
                            room = roomLookupById.get(cell.getRoomId());
                        }
                        if (room == null && cell.getRoomNumber() != null) {
                            room = roomLookupByNumber.get(cell.getRoomNumber().trim().toUpperCase());
                        }
                        if (room == null && sectionFixedRoom.containsKey(sectionId)) {
                            room = roomLookupById.get(sectionFixedRoom.get(sectionId));
                        }

                        // FAIL-FAST VALIDATION: Remove unsafe fallback to cell.getRoomId()
                        if (room == null) {
                            throw new IllegalStateException(String.format(
                                    "Failed to resolve database Room primary key for section '%s', subject '%s', roomNumber '%s', cellRoomId '%s'. Room must exist in database.",
                                    sectionId, cell.getSubject(), cell.getRoomNumber(), cell.getRoomId()));
                        }

                        Long roomId = room.getId(); // ALWAYS the real database primary key
                        String roomNumber = room.getRoomNumber();

                        Lecture lecture = new Lecture(
                                timetable,
                                sectionId,
                                cell.getSubject(),
                                cell.getTeacher(),
                                roomId,
                                roomNumber,
                                day,
                                slot,
                                lt,
                                now
                        );
                        lectures.add(lecture);
                    }
                }
            }

            lectureRepository.saveAll(lectures);

            // 4. Archive ALL previous ACTIVE timetables safely
            List<Timetable> previousActiveList = timetableRepository.findByStatus(TimetableStatus.ACTIVE);
            for (Timetable prev : previousActiveList) {
                if (!prev.getId().equals(timetable.getId())) {
                    prev.setStatus(TimetableStatus.ARCHIVED);
                    timetableRepository.save(prev);
                }
            }

            // 5. Mark new timetable as ACTIVE
            timetable.setStatus(TimetableStatus.ACTIVE);
            timetable.setGeneratedAt(LocalDateTime.now());
            timetableRepository.save(timetable);

            response.setTimetableId(timetable.getId());
        } else {
            // Generation produced no results — mark as failed and leave previous active unchanged
            timetableRepository.delete(timetable);
        }

        return response;
    }

    // ── Timetable Version Queries ─────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TimetableDTO> getAllTimetables() {
        return timetableRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toTimetableDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TimetableDTO getTimetableById(Long id) {
        Timetable t = timetableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", id));
        return toTimetableDTO(t);
    }

    @Transactional(readOnly = true)
    public TimetableDTO getActiveTimetable() {
        Timetable t = timetableRepository.findFirstByStatusOrderByCreatedAtDesc(TimetableStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("No active timetable found. Generate one first."));
        return toTimetableDTO(t);
    }

    // ── Lecture Queries (scoped to a timetable version) ───────────────────────

    @Transactional(readOnly = true)
    public List<LectureDTO> getLecturesByTimetableId(Long timetableId) {
        return lectureRepository.findByTimetableId(timetableId).stream()
                .map(this::toLectureDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getLecturesBySection(Long timetableId, String sectionId) {
        return lectureRepository.findByTimetableIdAndSectionId(timetableId, sectionId).stream()
                .map(this::toLectureDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getLecturesByTeacher(Long timetableId, String teacherId) {
        return lectureRepository.findByTimetableIdAndTeacherId(timetableId, teacherId).stream()
                .map(this::toLectureDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getLecturesByRoom(Long timetableId, Long roomId) {
        return lectureRepository.findByTimetableIdAndRoomId(timetableId, roomId).stream()
                .map(this::toLectureDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getLecturesByDay(Long timetableId, String day) {
        return lectureRepository.findByTimetableIdAndDay(timetableId, day).stream()
                .map(this::toLectureDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns the distinct room IDs occupied by a specific timetable on a given day
     * within the requested period range.
     *
     * <p>Periods are 1-indexed (Period 1 = startPeriod 1).
     * Internally converted to 0-indexed lecture slots before querying.
     *
     * @param timetableId the timetable to query
     * @param day         uppercase day name, e.g. "MONDAY"
     * @param startPeriod 1-indexed start period (inclusive)
     * @param endPeriod   1-indexed end period (inclusive)
     * @return list of distinct room IDs occupied in that slot range
     */
    @Transactional(readOnly = true)
    public List<Long> getOccupiedRoomIds(Long timetableId, String day, int startPeriod, int endPeriod) {
        // lectureSlot is 0-indexed: Period 1 = slot 0, Period 2 = slot 1 …
        int startSlot = startPeriod - 1;
        int endSlot   = endPeriod   - 1;
        log.info("TimetableService.getOccupiedRoomIds: timetableId={}, day={}, periods={}-{} → slots={}-{}",
                timetableId, day, startPeriod, endPeriod, startSlot, endSlot);
        List<Long> occupiedRoomIds = lectureRepository.findOccupiedRoomIds(
                timetableId, day.toUpperCase(), startSlot, endSlot);
        log.info("TimetableService.getOccupiedRoomIds result for TT #{}: {}", timetableId, occupiedRoomIds);
        return occupiedRoomIds;
    }

    // ── Active timetable lecture queries (convenience for existing APIs) ───────

    @Transactional(readOnly = true)
    public List<LectureDTO> getActiveLectures() {
        Optional<Timetable> active = timetableRepository.findFirstByStatusOrderByCreatedAtDesc(TimetableStatus.ACTIVE);
        if (active.isEmpty()) return Collections.emptyList();
        return getLecturesByTimetableId(active.get().getId());
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getActiveLecturesBySection(String sectionId) {
        Optional<Timetable> active = timetableRepository.findFirstByStatusOrderByCreatedAtDesc(TimetableStatus.ACTIVE);
        if (active.isEmpty()) return Collections.emptyList();
        return getLecturesBySection(active.get().getId(), sectionId);
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getActiveLecturesByTeacher(String teacherId) {
        Optional<Timetable> active = timetableRepository.findFirstByStatusOrderByCreatedAtDesc(TimetableStatus.ACTIVE);
        if (active.isEmpty()) return Collections.emptyList();
        return getLecturesByTeacher(active.get().getId(), teacherId);
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getActiveLecturesByRoom(Long roomId) {
        Optional<Timetable> active = timetableRepository.findFirstByStatusOrderByCreatedAtDesc(TimetableStatus.ACTIVE);
        if (active.isEmpty()) return Collections.emptyList();
        return getLecturesByRoom(active.get().getId(), roomId);
    }

    @Transactional(readOnly = true)
    public List<LectureDTO> getActiveLecturesByDay(String day) {
        Optional<Timetable> active = timetableRepository.findFirstByStatusOrderByCreatedAtDesc(TimetableStatus.ACTIVE);
        if (active.isEmpty()) return Collections.emptyList();
        return getLecturesByDay(active.get().getId(), day);
    }

    // ── DTO Converters ────────────────────────────────────────────────────────

    // ── Phase 7C Impact Analysis & Orchestrated Execution ─────────────────────

    @Transactional(readOnly = true)
    public TimetableImpactResponse calculateImpact(Long timetableId, java.time.LocalDate date,
                                                   Integer startPeriod, Integer endPeriod,
                                                   Long locationId) {
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", timetableId));

        String day = date != null ? date.getDayOfWeek().name() : "MONDAY";
        List<Lecture> dayLectures = lectureRepository.findByTimetableIdAndDay(timetable.getId(), day);

        List<Lecture> impacted = dayLectures.stream()
                .filter(l -> l.getLectureSlot() >= startPeriod && l.getLectureSlot() <= endPeriod)
                .filter(l -> locationId == null || (l.getRoomId() != null && l.getRoomId().equals(locationId)))
                .toList();

        List<ImpactedLectureDTO> impactedDTOs = impacted.stream()
                .map(l -> new ImpactedLectureDTO(
                        l.getId(),
                        l.getSubjectId(),
                        l.getTeacherId(),
                        l.getSectionId(),
                        l.getDay(),
                        l.getLectureSlot(),
                        l.getRoomNumber(),
                        l.getLectureType() != LectureType.LAB
                ))
                .toList();

        List<String> teachers = impacted.stream().map(Lecture::getTeacherId).distinct().toList();
        List<String> sections = impacted.stream().map(Lecture::getSectionId).distinct().toList();
        List<String> rooms = impacted.stream().map(Lecture::getRoomNumber).filter(Objects::nonNull).distinct().toList();
        List<String> subjects = impacted.stream().map(Lecture::getSubjectId).distinct().toList();

        int reschedulableCount = (int) impactedDTOs.stream().filter(ImpactedLectureDTO::isReschedulable).count();
        int nonReschedulableCount = impactedDTOs.size() - reschedulableCount;

        List<String> conflicts = new ArrayList<>();
        if (impacted.size() > 0) {
            conflicts.add(impacted.size() + " lectures conflict with event timeframe on " + day + " periods " + startPeriod + "-" + endPeriod);
        }

        String summary = String.format("Impact Analysis: %d lectures affected (%d reschedulable, %d non-reschedulable) across %d sections and %d teachers.",
                impacted.size(), reschedulableCount, nonReschedulableCount, sections.size(), teachers.size());

        return new TimetableImpactResponse(
                timetableId, impactedDTOs, teachers, sections, rooms, subjects,
                impacted.size(), reschedulableCount, nonReschedulableCount, conflicts, summary
        );
    }

    @Transactional
    public TimetableExecutionResultDTO executeEventImpact(Long timetableId, TimetableExecutionRequest request) {
        // ── STEP 1: Load active timetable & clone into a new timetable version ──
        Timetable sourceTimetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", timetableId));

        String eventTitleTag = (request.getEventTitle() != null && !request.getEventTitle().isBlank())
                ? request.getEventTitle() : "Event";
        Timetable copy = new Timetable();
        copy.setName("TT" + timetableId + " (" + eventTitleTag + ")");
        copy.setSemester(sourceTimetable.getSemester());
        copy.setAcademicYear(sourceTimetable.getAcademicYear());
        copy.setGeneratedAt(LocalDateTime.now());
        copy.setGeneratedBy(request.getExecutedBy() != null ? request.getExecutedBy() : "system");
        copy.setStatus(TimetableStatus.GENERATING);
        copy.setCreatedAt(LocalDateTime.now());
        copy = timetableRepository.save(copy);

        // Clone all lectures from source into the copy timetable
        List<Lecture> sourceLectures = lectureRepository.findByTimetableId(sourceTimetable.getId());
        List<Lecture> clonedLectures = new ArrayList<>();
        for (Lecture src : sourceLectures) {
            Lecture clone = new Lecture(
                    copy,
                    src.getSectionId(),
                    src.getSubjectId(),
                    src.getTeacherId(),
                    src.getRoomId(),
                    src.getRoomNumber(),
                    src.getDay(),
                    src.getLectureSlot(),
                    src.getLectureType(),
                    LocalDateTime.now()
            );
            clonedLectures.add(clone);
        }
        clonedLectures = lectureRepository.saveAll(clonedLectures);

        // Map original lecture ID to cloned lecture
        Map<Long, Lecture> originalToClone = new HashMap<>();
        for (int i = 0; i < sourceLectures.size(); i++) {
            originalToClone.put(sourceLectures.get(i).getId(), clonedLectures.get(i));
        }

        // Event timing parameters
        String eventDay = request.getDate() != null ? request.getDate().getDayOfWeek().name() : "MONDAY";
        int startPeriod = request.getStartPeriod() != null ? request.getStartPeriod() : 1;
        int endPeriod = request.getEndPeriod() != null ? request.getEndPeriod() : startPeriod;
        int startSlot = Math.max(0, startPeriod - 1);
        int endSlot = Math.max(startSlot, endPeriod - 1);

        // ── STEP 2: Identify every lecture that overlaps with the event period ──
        List<Long> affectedIds = request.getAffectedLectureIds() != null ? request.getAffectedLectureIds() : List.of();
        List<Lecture> displacedLectures;
        if (!affectedIds.isEmpty()) {
            displacedLectures = affectedIds.stream()
                    .map(originalToClone::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            displacedLectures = clonedLectures.stream()
                    .filter(l -> l.getDay().equalsIgnoreCase(eventDay)
                              && l.getLectureSlot() >= startSlot
                              && l.getLectureSlot() <= endSlot)
                    .collect(Collectors.toList());
        }

        // ── STEPS 3–7: Build occupancy maps and attempt to RESCHEDULE each displaced lecture ──
        // Build set of lectures NOT displaced in the copy timetable
        Set<Long> displacedIds = displacedLectures.stream().map(Lecture::getId).collect(Collectors.toSet());
        List<Lecture> nonDisplacedLectures = clonedLectures.stream()
                .filter(l -> !displacedIds.contains(l.getId()))
                .collect(Collectors.toList());

        // Dynamic Occupancy Sets for tracking conflicts: "DAY:SLOT:KEY"
        Set<String> sectionOccupancy = new HashSet<>();
        Set<String> teacherOccupancy = new HashSet<>();
        Set<String> roomOccupancy    = new HashSet<>();

        for (Lecture l : nonDisplacedLectures) {
            String dayKey = l.getDay().toUpperCase();
            int slot = l.getLectureSlot();
            sectionOccupancy.add(dayKey + ":" + slot + ":" + l.getSectionId());
            if (l.getTeacherId() != null) {
                teacherOccupancy.add(dayKey + ":" + slot + ":" + l.getTeacherId());
            }
            if (l.getRoomId() != null) {
                roomOccupancy.add(dayKey + ":" + slot + ":" + l.getRoomId());
            }
        }

        // Also reserve event slot across all sections so no rescheduled lecture goes into the event slot
        Set<String> distinctSections = clonedLectures.stream().map(Lecture::getSectionId).collect(Collectors.toSet());
        if (distinctSections.isEmpty()) distinctSections.add("Section 1");

        for (String secId : distinctSections) {
            for (int s = startSlot; s <= endSlot; s++) {
                sectionOccupancy.add(eventDay.toUpperCase() + ":" + s + ":" + secId);
            }
        }

        // Ordered candidate slots AFTER the event (Priority: 1. Same day after event, 2. Subsequent days)
        List<DaySlot> candidateSlots = buildCandidateSlotsAfterEvent(eventDay, endSlot);

        List<Long> rescheduledIds = new ArrayList<>();
        List<Long> cancelledIds   = new ArrayList<>();
        List<String> warnings     = new ArrayList<>();

        for (Lecture l : displacedLectures) {
            if (l.getLectureType() == LectureType.LAB) {
                warnings.add("Lab lecture " + l.getSubjectId() + " (Section " + l.getSectionId() + ") cannot be rescheduled — cancelled.");
                cancelledIds.add(l.getId());
                lectureRepository.delete(l);
                continue;
            }

            boolean rescheduled = false;
            for (DaySlot candidate : candidateSlots) {
                String cDay = candidate.day.toUpperCase();
                int cSlot   = candidate.slot;

                String secKey  = cDay + ":" + cSlot + ":" + l.getSectionId();
                String tchrKey = l.getTeacherId() != null ? cDay + ":" + cSlot + ":" + l.getTeacherId() : null;
                String roomKey = l.getRoomId() != null    ? cDay + ":" + cSlot + ":" + l.getRoomId()    : null;

                // Constraint Check (Step 4): Section free, Teacher free, Room free
                boolean secFree  = !sectionOccupancy.contains(secKey);
                boolean tchrFree = tchrKey == null || !teacherOccupancy.contains(tchrKey);
                boolean roomFree = roomKey == null || !roomOccupancy.contains(roomKey);

                if (secFree && tchrFree && roomFree) {
                    // STEP 5: MOVE the lecture to valid slot
                    l.setDay(candidate.day);
                    l.setLectureSlot(cSlot);
                    lectureRepository.save(l);

                    // Update occupancy state
                    sectionOccupancy.add(secKey);
                    if (tchrKey != null) teacherOccupancy.add(tchrKey);
                    if (roomKey != null) roomOccupancy.add(roomKey);

                    rescheduledIds.add(l.getId());
                    rescheduled = true;
                    log.info("Rescheduled displaced lecture {} ({}) to {} slot {}", l.getId(), l.getSubjectId(), candidate.day, cSlot);
                    break;
                }
            }

            if (!rescheduled) {
                // STEP 7: Mark CANCELLED only after exhausting all future slots
                warnings.add("No valid free slot after event for " + l.getSubjectId() + " (Section " + l.getSectionId() + ") — cancelled.");
                cancelledIds.add(l.getId());
                lectureRepository.delete(l);
            }
        }

        // ── STEP 8: Insert the EVENT lectures into the requested time slot ──
        String eventTitle = request.getEventTitle() != null ? request.getEventTitle() : "Event #" + request.getEventId();
        String organizer  = request.getExecutedBy() != null  ? request.getExecutedBy()  : "EVENT";

        for (String secId : distinctSections) {
            for (int s = startSlot; s <= endSlot; s++) {
                Lecture eventLec = new Lecture();
                eventLec.setTimetable(copy);
                eventLec.setSectionId(secId);
                eventLec.setSubjectId("EVENT");
                eventLec.setTeacherId(organizer);
                eventLec.setRoomId(request.getLocationId());
                eventLec.setRoomNumber(request.getRoomNumber());
                eventLec.setDay(eventDay);
                eventLec.setLectureSlot(s);
                eventLec.setLectureType(LectureType.EVENT);
                eventLec.setEventId(request.getEventId());
                eventLec.setEventName(eventTitle);
                eventLec.setCreatedAt(LocalDateTime.now());
                lectureRepository.save(eventLec);
            }
        }

        // Activate copy, archive source
        copy.setStatus(TimetableStatus.ACTIVE);
        timetableRepository.save(copy);
        sourceTimetable.setStatus(TimetableStatus.ARCHIVED);
        timetableRepository.save(sourceTimetable);

        String summary = String.format(
                "Event execution complete. New TT #%d created (copy of TT #%d). %d rescheduled, %d cancelled. Original TT #%d archived.",
                copy.getId(), timetableId, rescheduledIds.size(), cancelledIds.size(), timetableId);

        return new TimetableExecutionResultDTO(
                "SUCCESS", summary, rescheduledIds.size(), cancelledIds.size(),
                rescheduledIds, cancelledIds, warnings
        );
    }

    private static class DaySlot {
        final String day;
        final int slot;
        DaySlot(String day, int slot) {
            this.day = day;
            this.slot = slot;
        }
    }

    private List<DaySlot> buildCandidateSlotsAfterEvent(String eventDay, int endSlot) {
        List<DaySlot> candidates = new ArrayList<>();
        int totalSlots = 6;

        int eventDayIdx = -1;
        for (int i = 0; i < DAYS.length; i++) {
            if (DAYS[i].equalsIgnoreCase(eventDay)) {
                eventDayIdx = i;
                break;
            }
        }
        if (eventDayIdx == -1) eventDayIdx = 0;

        // 1. Same day, slots strictly AFTER endSlot
        for (int slot = endSlot + 1; slot < totalSlots; slot++) {
            candidates.add(new DaySlot(DAYS[eventDayIdx], slot));
        }

        // 2. Subsequent days of the week in chronological order
        for (int offset = 1; offset < DAYS.length; offset++) {
            int dayIdx = (eventDayIdx + offset) % DAYS.length;
            for (int slot = 0; slot < totalSlots; slot++) {
                candidates.add(new DaySlot(DAYS[dayIdx], slot));
            }
        }

        return candidates;
    }

    private TimetableDTO toTimetableDTO(Timetable t) {
        return new TimetableDTO(
                t.getId(), t.getName(), t.getSemester(), t.getAcademicYear(),
                t.getGeneratedAt(), t.getGeneratedBy(), t.getStatus(), t.getCreatedAt()
        );
    }

    private LectureDTO toLectureDTO(Lecture l) {
        return new LectureDTO(
                l.getId(),
                l.getTimetable().getId(),
                l.getSectionId(),
                l.getSubjectId(),
                l.getTeacherId(),
                l.getRoomId(),
                l.getRoomNumber(),
                l.getDay(),
                l.getLectureSlot(),
                l.getLectureType(),
                l.getEventId(),
                l.getEventName()
        );
    }
}
