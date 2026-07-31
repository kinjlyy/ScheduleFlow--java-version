// src/pages/EventsPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import {
  getAllEvents, createEvent, updateEvent, deleteEvent, getEventById,
  reserveRoom, cancelReservation, checkAvailability, getReservations,
  generateImpactAnalysis, generateExecutionPlan, executeStrategy, getExecutionHistory,
  getAllTimetables, getActiveTimetable
} from '../api/eventApi.js';
import { fetchRooms } from '../api/roomApi.js';
import styles from './EventsPage.module.css';

const EVENT_CATEGORIES = ['GENERAL', 'ROOM_RESERVATION', 'ACADEMIC_EVENT'];
const EVENT_TYPES      = ['LECTURE', 'EXAM', 'WORKSHOP', 'SEMINAR', 'HOLIDAY', 'OTHER'];
const EVENT_STATUSES   = ['DRAFT', 'SCHEDULED', 'IMPACT_ANALYZED', 'READY_FOR_EXECUTION', 'EXECUTING', 'COMPLETED', 'FAILED', 'CANCELLED'];
const LOCATION_TYPES   = ['CLASSROOM', 'LAB', 'SEMINAR_HALL', 'AUDITORIUM', 'OTHER'];
const EXECUTION_STRATEGIES = [
  { id: 'CANCEL_ALL', label: 'Cancel All Affected Lectures' },
  { id: 'RESCHEDULE_AND_CANCEL', label: 'Reschedule Lectures (Cancel Non-Reschedulable)' }
];

const EMPTY_EVENT_FORM = {
  title: '', description: '', eventType: 'LECTURE', eventCategory: 'GENERAL',
  date: '', startPeriod: 1, endPeriod: 2, locationId: '', locationType: 'CLASSROOM',
  timetableId: '', organizer: '', createdBy: 'Admin'
};

const EMPTY_RESERVATION_FORM = {
  title: '', description: '', eventType: 'SEMINAR', locationId: '',
  locationType: 'CLASSROOM', date: '', startPeriod: 1, endPeriod: 2,
  organizer: '', createdBy: 'Admin'
};

export default function EventsPage({ token, onBack, onTimetableRefreshed }) {
  const [activeTab, setActiveTab] = useState('events'); // 'events' | 'reservations' | 'availability' | 'impact'

  // Data states
  const [events, setEvents]             = useState([]);
  const [reservations, setReservations] = useState([]);
  const [rooms, setRooms]               = useState([]);
  const [timetables, setTimetables]     = useState([]);
  const [loading, setLoading]           = useState(false);
  const [error, setError]               = useState(null);

  // Event list filters
  const [filterCategory, setFilterCategory] = useState('');
  const [filterStatus, setFilterStatus]     = useState('');
  const [filterDate, setFilterDate]         = useState('');
  const [search, setSearch]                 = useState('');

  // Event modal states
  const [showEventModal, setShowEventModal] = useState(false);
  const [editingEvent, setEditingEvent]     = useState(null);
  const [eventForm, setEventForm]           = useState(EMPTY_EVENT_FORM);
  const [savingEvent, setSavingEvent]       = useState(false);

  // Reservation modal states
  const [showResModal, setShowResModal] = useState(false);
  const [resForm, setResForm]           = useState(EMPTY_RESERVATION_FORM);
  const [savingRes, setSavingRes]       = useState(false);

  // Availability check states
  const [availDate, setAvailDate]         = useState(new Date().toISOString().slice(0, 10));
  const [availStart, setAvailStart]       = useState(1);
  const [availEnd, setAvailEnd]           = useState(2);
  const [availResult, setAvailResult]     = useState(null);
  const [checkingAvail, setCheckingAvail] = useState(false);

  // Impact Analysis & Execution Workflow state
  const [impactEventId, setImpactEventId]           = useState('');
  const [impactTimetableId, setImpactTimetableId]   = useState('');
  const [impactDate, setImpactDate]                 = useState('');
  const [impactStart, setImpactStart]               = useState(1);
  const [impactEnd, setImpactEnd]                   = useState(2);
  const [impactLocationId, setImpactLocationId]     = useState('');
  const [impactResult, setImpactResult]             = useState(null);
  const [execPlanResult, setExecPlanResult]         = useState(null);
  const [chosenStrategy, setChosenStrategy]         = useState('RESCHEDULE_AND_CANCEL');
  const [executing, setExecuting]                   = useState(false);
  const [execResult, setExecResult]                 = useState(null);
  const [analyzing, setAnalyzing]                   = useState(false);

  // History modal state
  const [historyEvent, setHistoryEvent] = useState(null);
  const [historyData, setHistoryData]   = useState(null);

  // ── Load initial data ──────────────────────────────────────────────────
  const loadData = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const [evList, resList, roomList, ttList, activeTt] = await Promise.all([
        getAllEvents(token).catch(() => []),
        getReservations({}, token).catch(() => []),
        fetchRooms(token).catch(() => []),
        getAllTimetables(token).catch(() => []),
        getActiveTimetable(token).catch(() => null)
      ]);
      setEvents(evList);
      setReservations(resList);
      setRooms(roomList);
      setTimetables(ttList);
      if (activeTt?.id) {
        setImpactTimetableId(String(activeTt.id));
      }
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => { loadData(); }, [loadData]);

  // ── Event CRUD handlers ────────────────────────────────────────────────
  function openCreateEvent() {
    setEditingEvent(null);
    setEventForm(EMPTY_EVENT_FORM);
    setShowEventModal(true);
  }

  function openEditEvent(ev) {
    setEditingEvent(ev);
    setEventForm({
      title: ev.title || '',
      description: ev.description || '',
      eventType: ev.eventType || 'LECTURE',
      eventCategory: ev.eventCategory || 'GENERAL',
      date: ev.date || '',
      startPeriod: ev.startPeriod || 1,
      endPeriod: ev.endPeriod || 2,
      locationId: ev.locationId || '',
      locationType: ev.locationType || 'CLASSROOM',
      timetableId: ev.timetableId || '',
      organizer: ev.organizer || '',
      createdBy: ev.createdBy || 'Admin'
    });
    setShowEventModal(true);
  }

  async function handleSaveEvent(e) {
    e.preventDefault();
    setSavingEvent(true);
    try {
      const payload = {
        ...eventForm,
        locationId: eventForm.locationId ? Number(eventForm.locationId) : null,
        timetableId: eventForm.timetableId ? Number(eventForm.timetableId) : null,
        startPeriod: Number(eventForm.startPeriod),
        endPeriod: Number(eventForm.endPeriod)
      };
      if (editingEvent) {
        await updateEvent(editingEvent.id, payload, token);
      } else {
        await createEvent(payload, token);
      }
      setShowEventModal(false);
      loadData();
    } catch (err) {
      alert('Save Error: ' + err.message);
    } finally {
      setSavingEvent(false);
    }
  }

  async function handleDeleteEvent(id) {
    if (!window.confirm(`Delete Event #${id}?`)) return;
    try {
      await deleteEvent(id, token);
      loadData();
    } catch (err) {
      alert('Delete Error: ' + err.message);
    }
  }

  // ── Reservation handlers ───────────────────────────────────────────────
  async function handleSaveReservation(e) {
    e.preventDefault();
    setSavingRes(true);
    try {
      const payload = {
        ...resForm,
        locationId: Number(resForm.locationId),
        startPeriod: Number(resForm.startPeriod),
        endPeriod: Number(resForm.endPeriod)
      };
      await reserveRoom(payload, token);
      setShowResModal(false);
      loadData();
    } catch (err) {
      alert('Reservation Error: ' + err.message);
    } finally {
      setSavingRes(false);
    }
  }

  async function handleCancelReservation(id) {
    if (!window.confirm('Cancel this room reservation?')) return;
    try {
      await cancelReservation(id, token);
      loadData();
    } catch (err) {
      alert('Cancel Error: ' + err.message);
    }
  }

  // ── Availability check handler ─────────────────────────────────────────
  async function handleCheckAvailability(e) {
    if (e) e.preventDefault();
    setCheckingAvail(true);
    try {
      const res = await checkAvailability(availDate, Number(availStart), Number(availEnd), token);
      setAvailResult(res);
    } catch (err) {
      alert('Availability Check Error: ' + err.message);
    } finally {
      setCheckingAvail(false);
    }
  }

  function quickReserve(roomInfo) {
    setResForm({
      ...EMPTY_RESERVATION_FORM,
      locationId: roomInfo.room.id,
      date: availDate,
      startPeriod: availStart,
      endPeriod: availEnd
    });
    setShowResModal(true);
  }

  // ── Impact Analysis & Execution Workflow ────────────────────────────────
  async function handleRunImpactAnalysis() {
    setAnalyzing(true);
    setImpactResult(null);
    setExecPlanResult(null);
    setExecResult(null);
    try {
      const payload = {};
      if (impactEventId) payload.eventId = Number(impactEventId);
      if (impactTimetableId) payload.timetableId = Number(impactTimetableId);
      if (impactDate) payload.date = impactDate;
      if (impactStart) payload.startPeriod = Number(impactStart);
      if (impactEnd) payload.endPeriod = Number(impactEnd);
      if (impactLocationId) payload.locationId = Number(impactLocationId);

      const res = await generateImpactAnalysis(payload, token);
      setImpactResult(res);
    } catch (err) {
      alert('Impact Analysis Error: ' + err.message);
    } finally {
      setAnalyzing(false);
    }
  }

  async function handleGeneratePlan() {
    if (!impactResult?.eventId && !impactEventId) {
      alert('An Event ID is required to generate an execution plan.');
      return;
    }
    setAnalyzing(true);
    try {
      const eventId = impactResult?.eventId || Number(impactEventId);
      const res = await generateExecutionPlan({
        eventId,
        executionStrategy: chosenStrategy
      }, token);
      setExecPlanResult(res);
    } catch (err) {
      alert('Execution Plan Error: ' + err.message);
    } finally {
      setAnalyzing(false);
    }
  }

  async function handleExecuteStrategy() {
    const eventId = execPlanResult?.eventId || impactResult?.eventId || Number(impactEventId);
    if (!eventId) {
      alert('Event ID required for execution.');
      return;
    }
    if (!window.confirm(`Execute strategy '${chosenStrategy}' for Event #${eventId}?`)) return;

    setExecuting(true);
    try {
      const res = await executeStrategy(eventId, {
        executionStrategy: chosenStrategy,
        executedBy: 'Admin'
      }, token);
      setExecResult(res);
      loadData();
      if (onTimetableRefreshed) onTimetableRefreshed();
    } catch (err) {
      alert('Execution Error: ' + err.message);
    } finally {
      setExecuting(false);
    }
  }

  async function openExecutionHistory(ev) {
    setHistoryEvent(ev);
    setHistoryData(null);
    try {
      const res = await getExecutionHistory(ev.id, token);
      setHistoryData(res);
    } catch (err) {
      setHistoryData({ error: err.message });
    }
  }

  // Filtered Events
  const filteredEvents = events.filter(ev => {
    const mCat    = !filterCategory || ev.eventCategory === filterCategory;
    const mStat   = !filterStatus || ev.status === filterStatus;
    const mDate   = !filterDate || ev.date === filterDate;
    const mSearch = !search ||
      ev.title?.toLowerCase().includes(search.toLowerCase()) ||
      ev.organizer?.toLowerCase().includes(search.toLowerCase());
    return mCat && mStat && mDate && mSearch;
  });

  return (
    <div className={styles.root}>
      {/* Top Bar */}
      <div className={styles.topbar}>
        <button className={styles.back_btn} onClick={onBack}>← Dashboard</button>
        <div>
          <h1 className={styles.title}>📅 Manage Events &amp; Reservations</h1>
          <p className={styles.subtitle}>Full orchestration across Event Service, Resource Service &amp; Timetable Service</p>
        </div>
        <div className={styles.top_actions}>
          <button className={styles.primary_btn} onClick={openCreateEvent}>+ New Event</button>
          <button className={styles.secondary_btn} onClick={() => setShowResModal(true)}>+ Reserve Room</button>
        </div>
      </div>

      {/* Tabs */}
      <div className={styles.tab_bar}>
        <button className={`${styles.tab} ${activeTab === 'events' ? styles.active_tab : ''}`} onClick={() => setActiveTab('events')}>
          📋 All Events ({events.length})
        </button>
        <button className={`${styles.tab} ${activeTab === 'reservations' ? styles.active_tab : ''}`} onClick={() => setActiveTab('reservations')}>
          🏢 Room Reservations ({reservations.length})
        </button>
        <button className={`${styles.tab} ${activeTab === 'availability' ? styles.active_tab : ''}`} onClick={() => setActiveTab('availability')}>
          🔍 Room Availability Check
        </button>
        <button className={`${styles.tab} ${activeTab === 'impact' ? styles.active_tab : ''}`} onClick={() => setActiveTab('impact')}>
          ⚡ Impact Analysis &amp; Execution
        </button>
      </div>

      {/* Loading / Error Banner */}
      {loading && <div className={styles.loading_box}>Loading Event Service data…</div>}
      {error && <div className={styles.error_box}>⚠ {error}</div>}

      {/* ── TAB 1: ALL EVENTS ──────────────────────────────────────────────── */}
      {activeTab === 'events' && !loading && (
        <div className={styles.tab_content}>
          {/* Filters */}
          <div className={styles.filter_row}>
            <input className={styles.input_search} placeholder="Search title, organizer..." value={search} onChange={e => setSearch(e.target.value)} />
            <select className={styles.select_filter} value={filterCategory} onChange={e => setFilterCategory(e.target.value)}>
              <option value="">All Categories</option>
              {EVENT_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
            <select className={styles.select_filter} value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
              <option value="">All Statuses</option>
              {EVENT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
            <input type="date" className={styles.input_date} value={filterDate} onChange={e => setFilterDate(e.target.value)} />
            <button className={styles.btn_reset} onClick={() => { setFilterCategory(''); setFilterStatus(''); setFilterDate(''); setSearch(''); }}>Clear</button>
          </div>

          {filteredEvents.length === 0 ? (
            <div className={styles.empty_box}>No events found matching your criteria.</div>
          ) : (
            <div className={styles.table_wrapper}>
              <table className={styles.table}>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Title &amp; Type</th>
                    <th>Category</th>
                    <th>Date &amp; Slot</th>
                    <th>Location / Timetable</th>
                    <th>Organizer</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredEvents.map(ev => (
                    <tr key={ev.id}>
                      <td><strong>#{ev.id}</strong></td>
                      <td>
                        <div className={styles.cell_title}>{ev.title}</div>
                        <span className={styles.badge_type}>{ev.eventType}</span>
                      </td>
                      <td><span className={styles.badge_cat}>{ev.eventCategory}</span></td>
                      <td>
                        <div>📅 {ev.date || '—'}</div>
                        <small>Periods {ev.startPeriod} - {ev.endPeriod}</small>
                      </td>
                      <td>
                        {ev.locationId ? <div>Room #{ev.locationId} ({ev.locationType})</div> : null}
                        {ev.timetableId ? <small>Timetable #{ev.timetableId}</small> : null}
                        {!ev.locationId && !ev.timetableId && <span className={styles.text_muted}>None</span>}
                      </td>
                      <td>{ev.organizer || '—'}</td>
                      <td><span className={`${styles.badge_status} ${styles['status_' + (ev.status || '').toLowerCase()]}`}>{ev.status}</span></td>
                      <td>
                        <div className={styles.action_btns}>
                          <button className={styles.btn_edit} onClick={() => openEditEvent(ev)}>Edit</button>
                          <button className={styles.btn_history} onClick={() => openExecutionHistory(ev)}>History</button>
                          <button className={styles.btn_delete} onClick={() => handleDeleteEvent(ev.id)}>Delete</button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ── TAB 2: ROOM RESERVATIONS ────────────────────────────────────────── */}
      {activeTab === 'reservations' && !loading && (
        <div className={styles.tab_content}>
          <div className={styles.tab_header}>
            <h3>Active &amp; Historical Room Reservations</h3>
            <button className={styles.primary_btn} onClick={() => setShowResModal(true)}>+ New Room Reservation</button>
          </div>

          {reservations.length === 0 ? (
            <div className={styles.empty_box}>No room reservations found. Create one using the button above.</div>
          ) : (
            <div className={styles.grid_cards}>
              {reservations.map(res => {
                const ev = res.event || res;
                const room = res.room;
                return (
                  <div key={ev.id || res.id} className={styles.res_card}>
                    <div className={styles.res_card_top}>
                      <span className={styles.badge_cat}>ROOM RESERVATION</span>
                      <span className={`${styles.badge_status} ${styles['status_' + (ev.status || '').toLowerCase()]}`}>{ev.status}</span>
                    </div>
                    <h4>{ev.title}</h4>
                    <div className={styles.res_info}>
                      <div>📍 <strong>Room:</strong> {room ? `${room.roomNumber} (${room.roomType}, Cap: ${room.maximumCapacity})` : `Location ID #${ev.locationId}`}</div>
                      <div>📅 <strong>Date:</strong> {ev.date}</div>
                      <div>🕐 <strong>Periods:</strong> {ev.startPeriod} - {ev.endPeriod}</div>
                      <div>👤 <strong>Organizer:</strong> {ev.organizer || 'N/A'}</div>
                    </div>
                    {ev.status !== 'CANCELLED' && (
                      <button className={styles.btn_delete} onClick={() => handleCancelReservation(ev.id)}>Cancel Reservation</button>
                    )}
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ── TAB 3: ROOM AVAILABILITY CHECK ──────────────────────────────────── */}
      {activeTab === 'availability' && (
        <div className={styles.tab_content}>
          <form className={styles.avail_form} onSubmit={handleCheckAvailability}>
            <h3>🔍 Real-Time Room Availability Check</h3>
            <div className={styles.form_row}>
              <div className={styles.form_group}>
                <label>Date *</label>
                <input type="date" required value={availDate} onChange={e => setAvailDate(e.target.value)} />
              </div>
              <div className={styles.form_group}>
                <label>Start Period (1-10) *</label>
                <input type="number" min="1" max="10" required value={availStart} onChange={e => setAvailStart(e.target.value)} />
              </div>
              <div className={styles.form_group}>
                <label>End Period (1-10) *</label>
                <input type="number" min="1" max="10" required value={availEnd} onChange={e => setAvailEnd(e.target.value)} />
              </div>
              <button type="submit" className={styles.primary_btn} disabled={checkingAvail}>
                {checkingAvail ? 'Checking…' : 'Check Availability'}
              </button>
            </div>
          </form>

          {availResult && (
            <div className={styles.avail_results}>
              <div className={styles.avail_section}>
                <h4 className={styles.text_success}>✅ Available Rooms ({availResult.availableRooms?.length || 0})</h4>
                {availResult.availableRooms?.length === 0 ? (
                  <p className={styles.text_muted}>No rooms available for slot {availResult.startPeriod}-{availResult.endPeriod} on {availResult.date}.</p>
                ) : (
                  <div className={styles.grid_cards}>
                    {availResult.availableRooms?.map(info => (
                      <div key={info.room?.id} className={styles.avail_card_ok}>
                        <h5>{info.room?.roomNumber}</h5>
                        <p>Type: {info.room?.roomType} | Cap: {info.room?.maximumCapacity}</p>
                        <button className={styles.secondary_btn} onClick={() => quickReserve(info)}>Quick Reserve</button>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className={styles.avail_section}>
                <h4 className={styles.text_danger}>❌ Reserved / Occupied Rooms ({availResult.reservedRooms?.length || 0})</h4>
                {availResult.reservedRooms?.length === 0 ? (
                  <p className={styles.text_muted}>No conflicts! All rooms are free.</p>
                ) : (
                  <div className={styles.grid_cards}>
                    {availResult.reservedRooms?.map(info => (
                      <div key={info.room?.id} className={styles.avail_card_busy}>
                        <h5>{info.room?.roomNumber}</h5>
                        <p>Type: {info.room?.roomType} | Cap: {info.room?.maximumCapacity}</p>
                        <div className={styles.occupied_list}>
                          <strong>Occupied Slots:</strong>
                          {info.occupiedPeriods?.map(op => (
                            <div key={op.eventId} className={styles.occ_tag}>
                              Event #{op.eventId} ({op.title}): Periods {op.startPeriod}-{op.endPeriod} [{op.status}]
                            </div>
                          ))}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}

      {/* ── TAB 4: IMPACT ANALYSIS & EXECUTION WORKFLOW ─────────────────────── */}
      {activeTab === 'impact' && (
        <div className={styles.tab_content}>
          <div className={styles.workflow_card}>
            <h3>⚡ Step 1: Run Read-Only Impact Analysis</h3>
            <p className={styles.subtitle}>Calculates affected lectures in TIMETABLE-SERVICE without modifying data.</p>
            <div className={styles.form_grid}>
              <div className={styles.form_group}>
                <label>Select Existing Event (Optional)</label>
                <select value={impactEventId} onChange={e => {
                  setImpactEventId(e.target.value);
                  const selected = events.find(ev => String(ev.id) === e.target.value);
                  if (selected) {
                    if (selected.timetableId) setImpactTimetableId(selected.timetableId);
                    if (selected.date) setImpactDate(selected.date);
                    if (selected.startPeriod) setImpactStart(selected.startPeriod);
                    if (selected.endPeriod) setImpactEnd(selected.endPeriod);
                    if (selected.locationId) setImpactLocationId(selected.locationId);
                  }
                }}>
                  <option value="">-- Custom Input --</option>
                  {events.map(ev => <option key={ev.id} value={ev.id}>#{ev.id} - {ev.title} ({ev.date})</option>)}
                </select>
              </div>

              <div className={styles.form_group}>
                <label>Target Timetable</label>
                <select value={impactTimetableId} onChange={e => setImpactTimetableId(e.target.value)}>
                  <option value="">-- Active Timetable --</option>
                  {timetables.map(tt => <option key={tt.id} value={tt.id}>Timetable #{tt.id} ({tt.sectionName} {tt.active ? '[ACTIVE]' : ''})</option>)}
                </select>
              </div>

              <div className={styles.form_group}>
                <label>Event Date *</label>
                <input type="date" value={impactDate} onChange={e => setImpactDate(e.target.value)} />
              </div>

              <div className={styles.form_group}>
                <label>Start Period *</label>
                <input type="number" min="1" max="10" value={impactStart} onChange={e => setImpactStart(e.target.value)} />
              </div>

              <div className={styles.form_group}>
                <label>End Period *</label>
                <input type="number" min="1" max="10" value={impactEnd} onChange={e => setImpactEnd(e.target.value)} />
              </div>

              <div className={styles.form_group}>
                <label>Room / Location</label>
                <select value={impactLocationId} onChange={e => setImpactLocationId(e.target.value)}>
                  <option value="">All Rooms</option>
                  {rooms.map(r => <option key={r.id} value={r.id}>{r.roomNumber} ({r.roomType})</option>)}
                </select>
              </div>
            </div>

            <button className={styles.primary_btn} onClick={handleRunImpactAnalysis} disabled={analyzing}>
              {analyzing ? 'Analyzing Impact…' : 'Run Impact Analysis'}
            </button>
          </div>

          {/* Impact Results */}
          {impactResult && (
            <div className={styles.workflow_card}>
              <div className={styles.impact_header}>
                <h4>Impact Analysis Results</h4>
                <span className={styles.badge_cat}>Status: {impactResult.newStatus || 'IMPACT_ANALYZED'}</span>
              </div>
              <p>Total Affected Lectures: <strong>{impactResult.impact?.totalAffectedLectures || 0}</strong></p>

              {impactResult.impact?.affectedLectures?.length > 0 && (
                <div className={styles.table_wrapper}>
                  <table className={styles.table}>
                    <thead>
                      <tr>
                        <th>Lecture ID</th>
                        <th>Subject</th>
                        <th>Section</th>
                        <th>Teacher</th>
                        <th>Slot</th>
                        <th>Reschedulable?</th>
                      </tr>
                    </thead>
                    <tbody>
                      {impactResult.impact.affectedLectures.map(lec => (
                        <tr key={lec.id}>
                          <td>#{lec.id}</td>
                          <td><strong>{lec.subject}</strong></td>
                          <td>{lec.section}</td>
                          <td>{lec.teacher}</td>
                          <td>Day {lec.dayOfWeek}, Period {lec.periodSlot}</td>
                          <td>
                            {lec.reschedulable ? (
                              <span className={styles.badge_ok}>YES</span>
                            ) : (
                              <span className={styles.badge_warn}>NO (Lab / Fixed)</span>
                            )}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Step 2: Generate Execution Plan */}
              <div className={styles.step_box}>
                <h4>⚡ Step 2: Choose Execution Strategy &amp; Generate Plan</h4>
                <div className={styles.form_row}>
                  <select value={chosenStrategy} onChange={e => setChosenStrategy(e.target.value)} className={styles.select_filter}>
                    {EXECUTION_STRATEGIES.map(s => <option key={s.id} value={s.id}>{s.label}</option>)}
                  </select>
                  <button className={styles.secondary_btn} onClick={handleGeneratePlan} disabled={analyzing}>
                    Generate Plan
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Execution Plan Results */}
          {execPlanResult && (
            <div className={styles.workflow_card}>
              <h4>Plan Details</h4>
              <p><strong>Summary:</strong> {execPlanResult.summary}</p>
              <div>To Reschedule: <strong>{execPlanResult.lecturesToReschedule?.length || 0}</strong></div>
              <div>To Cancel: <strong>{execPlanResult.lecturesToCancel?.length || 0}</strong></div>

              {execPlanResult.warnings?.length > 0 && (
                <div className={styles.warning_box}>
                  <strong>Warnings:</strong>
                  <ul>{execPlanResult.warnings.map((w, idx) => <li key={idx}>{w}</li>)}</ul>
                </div>
              )}

              {/* Step 3: Execute */}
              <div className={styles.step_box}>
                <h4>⚡ Step 3: Execute Timetable Modification</h4>
                <p>This will execute orchestration with TIMETABLE-SERVICE and publish execution results.</p>
                <button className={styles.btn_execute} onClick={handleExecuteStrategy} disabled={executing}>
                  {executing ? 'Executing Orchestration…' : '🚀 Execute Strategy Now'}
                </button>
              </div>
            </div>
          )}

          {/* Execution Output */}
          {execResult && (
            <div className={styles.exec_success_box}>
              <h3>🎉 Execution Result: {execResult.status}</h3>
              <p><strong>Summary:</strong> {execResult.executionSummary}</p>
              <p>Duration: {execResult.durationMs} ms | Rescheduled: {execResult.rescheduledCount} | Cancelled: {execResult.cancelledCount}</p>
              {execResult.warnings?.length > 0 && (
                <div>
                  <strong>Execution Notes:</strong>
                  <ul>{execResult.warnings.map((w, i) => <li key={i}>{w}</li>)}</ul>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* ── CREATE / EDIT EVENT MODAL ────────────────────────────────────── */}
      {showEventModal && (
        <div className={styles.modal_overlay} onClick={() => setShowEventModal(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h3>{editingEvent ? `Edit Event #${editingEvent.id}` : 'Create New Event'}</h3>
            <form onSubmit={handleSaveEvent} className={styles.modal_form}>
              <div className={styles.form_group}>
                <label>Title *</label>
                <input required value={eventForm.title} onChange={e => setEventForm({ ...eventForm, title: e.target.value })} />
              </div>
              <div className={styles.form_row}>
                <div className={styles.form_group}>
                  <label>Category *</label>
                  <select value={eventForm.eventCategory} onChange={e => setEventForm({ ...eventForm, eventCategory: e.target.value })}>
                    {EVENT_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div className={styles.form_group}>
                  <label>Event Type *</label>
                  <select value={eventForm.eventType} onChange={e => setEventForm({ ...eventForm, eventType: e.target.value })}>
                    {EVENT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>
              <div className={styles.form_row}>
                <div className={styles.form_group}>
                  <label>Date *</label>
                  <input type="date" required value={eventForm.date} onChange={e => setEventForm({ ...eventForm, date: e.target.value })} />
                </div>
                <div className={styles.form_group}>
                  <label>Start Period *</label>
                  <input type="number" min="1" max="10" required value={eventForm.startPeriod} onChange={e => setEventForm({ ...eventForm, startPeriod: e.target.value })} />
                </div>
                <div className={styles.form_group}>
                  <label>End Period *</label>
                  <input type="number" min="1" max="10" required value={eventForm.endPeriod} onChange={e => setEventForm({ ...eventForm, endPeriod: e.target.value })} />
                </div>
              </div>
              <div className={styles.form_row}>
                <div className={styles.form_group}>
                  <label>Location / Room</label>
                  <select value={eventForm.locationId} onChange={e => setEventForm({ ...eventForm, locationId: e.target.value })}>
                    <option value="">No Specific Room</option>
                    {rooms.map(r => <option key={r.id} value={r.id}>{r.roomNumber} ({r.roomType})</option>)}
                  </select>
                </div>
                <div className={styles.form_group}>
                  <label>Target Timetable</label>
                  <select value={eventForm.timetableId} onChange={e => setEventForm({ ...eventForm, timetableId: e.target.value })}>
                    <option value="">None / Default</option>
                    {timetables.map(tt => <option key={tt.id} value={tt.id}>Timetable #{tt.id} ({tt.sectionName})</option>)}
                  </select>
                </div>
              </div>
              <div className={styles.form_group}>
                <label>Organizer</label>
                <input value={eventForm.organizer} onChange={e => setEventForm({ ...eventForm, organizer: e.target.value })} />
              </div>
              <div className={styles.form_group}>
                <label>Description</label>
                <textarea rows={3} value={eventForm.description} onChange={e => setEventForm({ ...eventForm, description: e.target.value })} />
              </div>
              <div className={styles.modal_actions}>
                <button type="button" className={styles.btn_reset} onClick={() => setShowEventModal(false)}>Cancel</button>
                <button type="submit" className={styles.primary_btn} disabled={savingEvent}>
                  {savingEvent ? 'Saving…' : 'Save Event'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── ROOM RESERVATION MODAL ────────────────────────────────────────── */}
      {showResModal && (
        <div className={styles.modal_overlay} onClick={() => setShowResModal(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h3>Reserve a Room</h3>
            <form onSubmit={handleSaveReservation} className={styles.modal_form}>
              <div className={styles.form_group}>
                <label>Reservation Title *</label>
                <input required value={resForm.title} onChange={e => setResForm({ ...resForm, title: e.target.value })} placeholder="e.g. Guest Lecture" />
              </div>
              <div className={styles.form_group}>
                <label>Room Location *</label>
                <select required value={resForm.locationId} onChange={e => setResForm({ ...resForm, locationId: e.target.value })}>
                  <option value="">Select Room...</option>
                  {rooms.map(r => <option key={r.id} value={r.id}>{r.roomNumber} - {r.roomType} (Cap: {r.maximumCapacity})</option>)}
                </select>
              </div>
              <div className={styles.form_row}>
                <div className={styles.form_group}>
                  <label>Date *</label>
                  <input type="date" required value={resForm.date} onChange={e => setResForm({ ...resForm, date: e.target.value })} />
                </div>
                <div className={styles.form_group}>
                  <label>Start Period *</label>
                  <input type="number" min="1" max="10" required value={resForm.startPeriod} onChange={e => setResForm({ ...resForm, startPeriod: e.target.value })} />
                </div>
                <div className={styles.form_group}>
                  <label>End Period *</label>
                  <input type="number" min="1" max="10" required value={resForm.endPeriod} onChange={e => setResForm({ ...resForm, endPeriod: e.target.value })} />
                </div>
              </div>
              <div className={styles.form_group}>
                <label>Organizer</label>
                <input value={resForm.organizer} onChange={e => setResForm({ ...resForm, organizer: e.target.value })} placeholder="e.g. Prof. Smith" />
              </div>
              <div className={styles.modal_actions}>
                <button type="button" className={styles.btn_reset} onClick={() => setShowResModal(false)}>Cancel</button>
                <button type="submit" className={styles.primary_btn} disabled={savingRes}>
                  {savingRes ? 'Reserving…' : 'Confirm Reservation'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── EXECUTION HISTORY MODAL ────────────────────────────────────────── */}
      {historyEvent && (
        <div className={styles.modal_overlay} onClick={() => setHistoryEvent(null)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h3>Execution History — Event #{historyEvent.id}</h3>
            {historyData ? (
              historyData.error ? (
                <div className={styles.error_box}>{historyData.error}</div>
              ) : (
                <div className={styles.history_box}>
                  <div><strong>Status:</strong> {historyData.status}</div>
                  <div><strong>Strategy:</strong> {historyData.executionStrategy || 'N/A'}</div>
                  <div><strong>Executed By:</strong> {historyData.executedBy || 'N/A'}</div>
                  <div><strong>Started At:</strong> {historyData.executionStartedAt || 'N/A'}</div>
                  <div><strong>Completed At:</strong> {historyData.executionCompletedAt || 'N/A'}</div>
                  <div><strong>Summary:</strong> {historyData.executionSummary || 'N/A'}</div>
                  <div><strong>Result Details:</strong> {historyData.executionResult || 'N/A'}</div>
                </div>
              )
            ) : (
              <div>Loading execution history…</div>
            )}
            <button className={styles.btn_reset} onClick={() => setHistoryEvent(null)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
}
