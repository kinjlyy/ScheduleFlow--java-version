package com.scheduleflow.service;

import com.scheduleflow.dto.*;
import com.scheduleflow.exception.ResourceNotFoundException;
import com.scheduleflow.model.*;
import com.scheduleflow.repository.LectureRepository;
import com.scheduleflow.repository.TimetableRepository;
import com.scheduleflow.scheduler.RoomProvider;
import com.scheduleflow.util.TimetableConstants;
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
            // Build room lookup map (id -> Room domain model) via RoomProvider abstraction.
            // FeignRoomProvider calls OpenFeign to RESOURCE-SERVICE for active room data.
            Map<Long, Room> roomLookup = new HashMap<>();
            roomProvider.findAllActiveRooms().forEach(r -> roomLookup.put(r.getId(), r));

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

                        // Determine room (from cell roomId or fixedRoom)
                        Room room = null;
                        if (cell.getRoomId() != null) {
                            room = roomLookup.get(cell.getRoomId());
                        } else if (sectionFixedRoom.containsKey(sectionId)) {
                            room = roomLookup.get(sectionFixedRoom.get(sectionId));
                        }

                        Long roomId = room != null ? room.getId() : cell.getRoomId();
                        String roomNumber = room != null ? room.getRoomNumber() : cell.getRoomNumber();

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
        Timetable timetable = timetableRepository.findById(timetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Timetable", timetableId));

        List<Long> affectedIds = request.getAffectedLectureIds() != null ? request.getAffectedLectureIds() : List.of();
        List<Lecture> targetLectures = lectureRepository.findAllById(affectedIds).stream()
                .filter(l -> l.getTimetable().getId().equals(timetable.getId()))
                .toList();

        List<Long> rescheduledIds = new ArrayList<>();
        List<Long> cancelledIds = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        String strategy = request.getExecutionStrategy() != null ? request.getExecutionStrategy() : "CANCEL_ALL";

        if ("CANCEL_ALL".equalsIgnoreCase(strategy)) {
            for (Lecture l : targetLectures) {
                cancelledIds.add(l.getId());
                lectureRepository.delete(l);
            }
        } else if ("RESCHEDULE_AND_CANCEL".equalsIgnoreCase(strategy)) {
            // All existing lectures in this timetable version for collision checking
            List<Lecture> allLectures = lectureRepository.findByTimetableId(timetable.getId());
            Set<String> takenSlots = allLectures.stream()
                    .map(l -> l.getDay() + ":" + l.getLectureSlot() + ":" + l.getSectionId())
                    .collect(Collectors.toSet());

            for (Lecture l : targetLectures) {
                if (l.getLectureType() == LectureType.LAB) {
                    warnings.add("Laboratory lecture " + l.getSubjectId() + " (Section " + l.getSectionId() + ") cannot be rescheduled — cancelled.");
                    cancelledIds.add(l.getId());
                    lectureRepository.delete(l);
                    continue;
                }

                // Try finding an alternative slot (periods 1..6 on any weekday)
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
                    warnings.add("No free slot available for " + l.getSubjectId() + " (Section " + l.getSectionId() + ") — cancelled.");
                    cancelledIds.add(l.getId());
                    lectureRepository.delete(l);
                }
            }
        }

        String summary = String.format("Execution completed for strategy %s: %d rescheduled, %d cancelled.",
                strategy, rescheduledIds.size(), cancelledIds.size());

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
                l.getLectureType()
        );
    }
}
