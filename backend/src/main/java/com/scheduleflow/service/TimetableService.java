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
        // ── 1. Load the source (original) timetable ─────────────────────────
        Timetable sourceTimetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", timetableId));

        // ── 2. Create a COPY — original is NEVER modified ───────────────────
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

        // ── 3. Clone ALL lectures from source into the copy ──────────────────
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

        // Build a map: original lecture ID -> cloned lecture (for affected-ID lookup)
        Map<Long, Lecture> originalToClone = new HashMap<>();
        for (int i = 0; i < sourceLectures.size(); i++) {
            originalToClone.put(sourceLectures.get(i).getId(), clonedLectures.get(i));
        }

        // ── 4. Resolve which CLONED lectures correspond to affected IDs ──────
        List<Long> affectedIds = request.getAffectedLectureIds() != null ? request.getAffectedLectureIds() : List.of();
        List<Lecture> targetLectures = affectedIds.stream()
                .map(originalToClone::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        List<Long> rescheduledIds = new ArrayList<>();
        List<Long> cancelledIds   = new ArrayList<>();
        List<String> warnings     = new ArrayList<>();

        String strategy = request.getExecutionStrategy() != null ? request.getExecutionStrategy() : "CANCEL_ALL";

        // ── 5. Apply strategy on the COPY's lectures ─────────────────────────
        if ("CANCEL_ALL".equalsIgnoreCase(strategy)) {
            for (Lecture l : targetLectures) {
                cancelledIds.add(l.getId());
                lectureRepository.delete(l);
            }
        } else if ("RESCHEDULE_AND_CANCEL".equalsIgnoreCase(strategy)) {
            List<Lecture> allCopyLectures = lectureRepository.findByTimetableId(copy.getId());
            Set<String> takenSlots = allCopyLectures.stream()
                    .map(l -> l.getDay() + ":" + l.getLectureSlot() + ":" + l.getSectionId())
                    .collect(Collectors.toSet());

            for (Lecture l : targetLectures) {
                if (l.getLectureType() == LectureType.LAB) {
                    warnings.add("Lab lecture " + l.getSubjectId() + " (Section " + l.getSectionId() + ") cannot be rescheduled — cancelled.");
                    cancelledIds.add(l.getId());
                    lectureRepository.delete(l);
                    continue;
                }

                boolean rescheduled = false;
                for (String day : DAYS) {
                    for (int slot = 1; slot <= 6; slot++) {
                        String slotKey = day + ":" + slot + ":" + l.getSectionId();
                        if (!takenSlots.contains(slotKey)) {
                            l.setDay(day);
                            l.setLectureSlot(slot);
                            lectureRepository.save(l);
                            takenSlots.add(slotKey);
                            rescheduledIds.add(l.getId());
                            rescheduled = true;
                            break;
                        }
                    }
                    if (rescheduled) break;
                }

                if (!rescheduled) {
                    warnings.add("No free slot for " + l.getSubjectId() + " (Section " + l.getSectionId() + ") — cancelled.");
                    cancelledIds.add(l.getId());
                    lectureRepository.delete(l);
                }
            }
        }

        // ── 5.5 Insert Event entries into all sections on the COPY for event's slot ──
        if (request.getEventId() != null || request.getEventTitle() != null) {
            String eventDay = request.getDate() != null ? request.getDate().getDayOfWeek().name() : "MONDAY";
            int startPeriod = request.getStartPeriod() != null ? request.getStartPeriod() : 1;
            int endPeriod = request.getEndPeriod() != null ? request.getEndPeriod() : startPeriod;

            Set<String> distinctSections = clonedLectures.stream().map(Lecture::getSectionId).collect(Collectors.toSet());
            if (distinctSections.isEmpty()) {
                distinctSections.add("Section 1");
            }

            String eventTitle = request.getEventTitle() != null ? request.getEventTitle() : "Event #" + request.getEventId();
            String organizer = request.getExecutedBy() != null ? request.getExecutedBy() : "EVENT";

            for (String secId : distinctSections) {
                for (int p = startPeriod; p <= endPeriod; p++) {
                    int slot = p - 1; // 1-indexed period to 0-indexed slot
                    Lecture eventLec = new Lecture();
                    eventLec.setTimetable(copy);
                    eventLec.setSectionId(secId);
                    eventLec.setSubjectId("EVENT"); // Normalized: event title stored in eventName/eventId
                    eventLec.setTeacherId(organizer);
                    eventLec.setRoomId(request.getLocationId());
                    eventLec.setRoomNumber(request.getRoomNumber());
                    eventLec.setDay(eventDay);
                    eventLec.setLectureSlot(slot);
                    eventLec.setLectureType(LectureType.EVENT);
                    eventLec.setEventId(request.getEventId());
                    eventLec.setEventName(eventTitle);
                    eventLec.setCreatedAt(LocalDateTime.now());
                    lectureRepository.save(eventLec);
                }
            }
        }

        // ── 6. Activate copy, archive source (original untouched data-wise, status only changes) ──
        copy.setStatus(TimetableStatus.ACTIVE);
        timetableRepository.save(copy);
        sourceTimetable.setStatus(TimetableStatus.ARCHIVED);
        timetableRepository.save(sourceTimetable);

        String summary = String.format(
                "Execution complete — strategy: %s. New TT #%d created (copy of TT #%d). %d rescheduled, %d cancelled. Original TT #%d archived.",
                strategy, copy.getId(), timetableId, rescheduledIds.size(), cancelledIds.size(), timetableId);

        return new TimetableExecutionResultDTO(
                "SUCCESS", summary, rescheduledIds.size(), cancelledIds.size(),
                rescheduledIds, cancelledIds, warnings
        );
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
