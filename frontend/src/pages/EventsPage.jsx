import React, { useState, useEffect, useCallback } from 'react';
import {
  getAllEvents, createEvent, updateEvent, deleteEvent,
  getExecutionHistory, getAllTimetables, checkAvailability
} from '../api/eventApi.js';
import { fetchRooms } from '../api/roomApi.js';
import styles from './EventsPage.module.css';

// EventCategory — must match EventCategory.java exactly (GENERAL | ROOM_RESERVATION | TIMETABLE_EVENT)
const EVENT_CATEGORIES = ['GENERAL', 'ROOM_RESERVATION', 'TIMETABLE_EVENT'];
// EventType — must match EventType.java exactly (no HOLIDAY)
const EVENT_TYPES      = ['LECTURE', 'MEETING', 'SEMINAR', 'WORKSHOP', 'CEREMONY', 'EXAM', 'SPORTS', 'CULTURAL', 'OTHER'];
const EVENT_STATUSES   = ['DRAFT', 'SCHEDULED', 'IMPACT_ANALYZED', 'READY_FOR_EXECUTION', 'EXECUTING', 'COMPLETED', 'FAILED', 'CANCELLED'];

const DEFAULT_FALLBACK_ROOMS = [
  { id: 101, roomNumber: '101', roomType: 'CLASSROOM', maximumCapacity: 60, active: true },
  { id: 102, roomNumber: '102', roomType: 'CLASSROOM', maximumCapacity: 60, active: true },
  { id: 103, roomNumber: '103', roomType: 'CLASSROOM', maximumCapacity: 60, active: true },
  { id: 104, roomNumber: '104', roomType: 'CLASSROOM', maximumCapacity: 60, active: true },
  { id: 105, roomNumber: '105', roomType: 'CLASSROOM', maximumCapacity: 60, active: true },
  { id: 201, roomNumber: 'Lab 1', roomType: 'LABORATORY', maximumCapacity: 30, active: true },
  { id: 202, roomNumber: 'Lab 2', roomType: 'LABORATORY', maximumCapacity: 30, active: true },
  { id: 301, roomNumber: 'Seminar Hall 1', roomType: 'SEMINAR_HALL', maximumCapacity: 120, active: true },
];

const EMPTY_EVENT_FORM = {
  title: '', description: '', eventType: 'LECTURE', eventCategory: 'GENERAL',
  date: '', startPeriod: 1, endPeriod: 2, locationId: '', locationType: 'CLASSROOM',
  timetableId: '', organizer: '', createdBy: 'Admin',
  syncWithTimetable: false,
};

export default function EventsPage({ token, onBack, onTimetableRefreshed }) {
  // Data states
  const [events, setEvents]         = useState([]);
  const [timetables, setTimetables] = useState([]);
  const [loading, setLoading]       = useState(false);
  const [error, setError]           = useState(null);

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
  // Inline field validation errors — populated from backend 400 fieldErrors map
  const [fieldErrors, setFieldErrors]       = useState({});
  // Toast for unexpected 5xx errors only
  const [serverToast, setServerToast]       = useState(null);

  // Rooms fetched based on selected timetable & slot availability
  const [modalRooms, setModalRooms]         = useState([]);
  const [loadingRooms, setLoadingRooms]     = useState(false);

  // History modal state
  const [historyEvent, setHistoryEvent] = useState(null);
  const [historyData, setHistoryData]   = useState(null);

  // ── Toast auto-dismiss ────────────────────────────────────────────────────
  useEffect(() => {
    if (!serverToast) return;
    const t = setTimeout(() => setServerToast(null), 5000);
    return () => clearTimeout(t);
  }, [serverToast]);

  // ── Load initial data ──────────────────────────────────────────────────
  const loadData = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const [evList, ttList] = await Promise.all([
        getAllEvents(token).catch(() => []),
        getAllTimetables(token).catch(() => [])
      ]);
      setEvents(evList);
      setTimetables(ttList);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => { loadData(); }, [loadData]);

  // ── Fetch available rooms based on date & periods ─────────────────────────
  // Always queries the availability endpoint when date+periods are given.
  // The backend auto-detects the active timetable when timetableId is null.
  // NEVER falls back to all-rooms once a date is selected (that would show occupied rooms).
  const fetchAvailableRoomsForForm = useCallback(async (ttId, date, startP, endP) => {
    setLoadingRooms(true);
    setModalRooms([]);
    try {
      if (date && startP && endP) {
        // Date & periods known — query availability (timetableId may be null; backend uses active TT)
        const avail = await checkAvailability(
          date, Number(startP), Number(endP), ttId || null, token
        ).catch(() => null);
        if (avail && Array.isArray(avail.availableRooms)) {
          // Map to room objects and show ONLY available rooms (may be empty — that is correct)
          const availList = avail.availableRooms.map(item => item.room || item);
          setModalRooms(availList);
        } else {
          // Availability endpoint failed — show nothing rather than mislead user
          setModalRooms([]);
        }
      } else {
        // Date not selected yet — show all active rooms as initial hint
        const allRooms = await fetchRooms(token).catch(() => []);
        const activeRooms = Array.isArray(allRooms) ? allRooms.filter(r => r.active !== false) : [];
        setModalRooms(activeRooms);
      }
    } catch (err) {
      console.warn('Could not fetch available rooms:', err.message);
      setModalRooms([]);
    } finally {
      setLoadingRooms(false);
    }
  }, [token]);

  // ── Event CRUD handlers ────────────────────────────────────────────────
  function openCreateEvent() {
    setEditingEvent(null);
    setEventForm(EMPTY_EVENT_FORM);
    setFieldErrors({});
    setModalRooms([]);
    setShowEventModal(true);
    fetchAvailableRoomsForForm('', EMPTY_EVENT_FORM.date, EMPTY_EVENT_FORM.startPeriod, EMPTY_EVENT_FORM.endPeriod);
  }

  function openEditEvent(ev) {
    setEditingEvent(ev);
    setFieldErrors({});
    const form = {
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
      createdBy: ev.createdBy || 'Admin',
      syncWithTimetable: false
    };
    setEventForm(form);
    setShowEventModal(true);
    fetchAvailableRoomsForForm(form.timetableId, form.date, form.startPeriod, form.endPeriod);
  }

  async function handleSaveEvent(e) {
    e.preventDefault();
    setSavingEvent(true);
    setFieldErrors({});
    try {
      const payload = {
        ...eventForm,
        locationId: eventForm.locationId ? Number(eventForm.locationId) : null,
        timetableId: eventForm.timetableId ? Number(eventForm.timetableId) : null,
        startPeriod: Number(eventForm.startPeriod),
        endPeriod: Number(eventForm.endPeriod),
        syncWithTimetable: Boolean(eventForm.syncWithTimetable)
      };

      // Call API directly so we can inspect the response status before the
      // eventApi helper throws and discards structured JSON.
      const endpoint = editingEvent
        ? `${(await import('../api/eventApi.js')).EVENT_BASE}/${editingEvent.id}`
        : (await import('../api/eventApi.js')).EVENT_BASE;
      const method   = editingEvent ? 'PUT' : 'POST';
      const headers  = { 'Content-Type': 'application/json' };
      if (token) headers['Authorization'] = `Bearer ${token}`;

      const res = await fetch(endpoint, {
        method,
        headers,
        body: JSON.stringify(payload),
      });

      if (res.ok) {
        // Success path — unchanged
        if (eventForm.syncWithTimetable && onTimetableRefreshed) {
          onTimetableRefreshed();
        }
        setShowEventModal(false);
        loadData();
        return;
      }

      // ── Error path ──────────────────────────────────────────────────────
      let body = {};
      try { body = await res.json(); } catch (_) { /* non-JSON error body */ }

      if (res.status === 400 && body.fieldErrors) {
        // Bean Validation errors — display inline below each field
        setFieldErrors(body.fieldErrors);
      } else if (res.status === 400 && body.message) {
        // Single 400 message (e.g. ValidationException from service layer)
        setFieldErrors({ _form: body.message });
      } else {
        // 5xx or unexpected error — show toast, not alert
        const msg = body.message || `Server error (${res.status}). Please try again.`;
        setServerToast(msg);
      }
    } catch (err) {
      // Network-level failure
      setServerToast('Network error: ' + err.message);
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

  // Helper to clear a single field's error when the user edits it
  function updateField(patch) {
    setEventForm(f => ({ ...f, ...patch }));
    const key = Object.keys(patch)[0];
    if (key && fieldErrors[key]) {
      setFieldErrors(fe => { const n = { ...fe }; delete n[key]; return n; });
    }
  }

  return (
    <div className={styles.root}>
      {/* ── Server-error Toast (5xx only) ─────────────────────────────────── */}
      {serverToast && (
        <div className={styles.toast_error} role="alert">
          <span>⚠ {serverToast}</span>
          <button className={styles.toast_close} onClick={() => setServerToast(null)}>✕</button>
        </div>
      )}
      {/* Top Bar */}
      <div className={styles.topbar}>
        <button className={styles.back_btn} onClick={onBack}>← Dashboard</button>
        <div>
          <h1 className={styles.title}>📅 Manage Events</h1>
          <p className={styles.subtitle}>Create and manage your scheduled events</p>
        </div>
        <div className={styles.top_actions}>
          <button className={styles.primary_btn} onClick={openCreateEvent}>+ Add Event</button>
        </div>
      </div>

      {/* Loading / Error Banner */}
      {loading && <div className={styles.loading_box}>Loading Event Service data…</div>}
      {error && <div className={styles.error_box}>⚠ {error}</div>}

      {/* ── EVENTS LIST ──────────────────────────────────────────────────── */}
      {!loading && (
        <div className={styles.tab_content}>
          {/* Filters */}
          <div className={styles.filter_row}>
            <input
              className={styles.input_search}
              placeholder="Search title, organizer..."
              value={search}
              onChange={e => setSearch(e.target.value)}
            />
            <select
              className={styles.select_filter}
              value={filterCategory}
              onChange={e => setFilterCategory(e.target.value)}
            >
              <option value="">All Categories</option>
              {EVENT_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
            <select
              className={styles.select_filter}
              value={filterStatus}
              onChange={e => setFilterStatus(e.target.value)}
            >
              <option value="">All Statuses</option>
              {EVENT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
            <input
              type="date"
              className={styles.input_date}
              value={filterDate}
              onChange={e => setFilterDate(e.target.value)}
            />
            <button
              className={styles.btn_reset}
              onClick={() => { setFilterCategory(''); setFilterStatus(''); setFilterDate(''); setSearch(''); }}
            >
              Clear
            </button>
          </div>

          {filteredEvents.length === 0 ? (
            <div className={styles.empty_box}>
              No events found. Click <strong>+ Add Event</strong> to create one.
            </div>
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
                      <td>
                        <span className={`${styles.badge_status} ${styles['status_' + (ev.status || '').toLowerCase()]}`}>
                          {ev.status}
                        </span>
                      </td>
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

      {/* ── CREATE / EDIT EVENT MODAL ────────────────────────────────────── */}
      {showEventModal && (
        <div className={styles.modal_overlay} onClick={() => setShowEventModal(false)}>
          <div className={styles.modal} onClick={e => e.stopPropagation()}>
            <h3>{editingEvent ? `Edit Event #${editingEvent.id}` : 'Create New Event'}</h3>
            <form onSubmit={handleSaveEvent} className={styles.modal_form}>

              {/* Form-level error (single 400 message) */}
              {fieldErrors._form && (
                <div className={styles.form_error_banner}>{fieldErrors._form}</div>
              )}

              {/* TITLE */}
              <div className={styles.form_group}>
                <label>TITLE *</label>
                <input
                  required
                  value={eventForm.title}
                  className={fieldErrors.title ? styles.input_error : ''}
                  onChange={e => updateField({ title: e.target.value })}
                />
                {fieldErrors.title && <span className={styles.field_error}>{fieldErrors.title}</span>}
              </div>

              {/* CATEGORY + EVENT TYPE */}
              <div className={styles.form_row}>
                <div className={styles.form_group}>
                  <label>CATEGORY *</label>
                  <select
                    value={eventForm.eventCategory}
                    onChange={e => setEventForm({ ...eventForm, eventCategory: e.target.value })}
                  >
                    {EVENT_CATEGORIES.map(c => <option key={c} value={c}>{c}</option>)}
                  </select>
                </div>
                <div className={styles.form_group}>
                  <label>EVENT TYPE *</label>
                  <select
                    value={eventForm.eventType}
                    onChange={e => setEventForm({ ...eventForm, eventType: e.target.value })}
                  >
                    {EVENT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
              </div>

              {/* TARGET TIMETABLE — optional; availability always checks active timetable */}
              <div className={styles.form_group}>
                <label>TARGET TIMETABLE (optional)</label>
                <select
                  value={eventForm.timetableId}
                  onChange={e => {
                    const ttId = e.target.value;
                    setEventForm({ ...eventForm, timetableId: ttId, locationId: '' });
                    fetchAvailableRoomsForForm(ttId, eventForm.date, eventForm.startPeriod, eventForm.endPeriod);
                  }}
                >
                  <option value="">Auto (use Active Timetable)</option>
                  {timetables.map(tt => (
                    <option key={tt.id} value={tt.id}>
                      Timetable #{tt.id} ({tt.sectionName}{tt.active ? ' [ACTIVE]' : ''})
                    </option>
                  ))}
                </select>
                <small className={styles.text_muted}>
                  {eventForm.timetableId
                    ? 'Checking room availability against selected timetable.'
                    : 'Checking room availability against the currently ACTIVE timetable.'}
                </small>
              </div>

              {/* DATE + PERIODS */}
              <div className={styles.form_row}>
                <div className={styles.form_group}>
                  <label>DATE *</label>
                  <input
                    type="date"
                    required
                    value={eventForm.date}
                    className={fieldErrors.date ? styles.input_error : ''}
                    onChange={e => {
                      const d = e.target.value;
                      updateField({ date: d });
                      fetchAvailableRoomsForForm(eventForm.timetableId, d, eventForm.startPeriod, eventForm.endPeriod);
                    }}
                  />
                  {fieldErrors.date && <span className={styles.field_error}>{fieldErrors.date}</span>}
                </div>
                <div className={styles.form_group}>
                  <label>START PERIOD *</label>
                  <input
                    type="number" min="1" max="10" required
                    value={eventForm.startPeriod}
                    onChange={e => {
                      const sp = e.target.value;
                      setEventForm({ ...eventForm, startPeriod: sp });
                      fetchAvailableRoomsForForm(eventForm.timetableId, eventForm.date, sp, eventForm.endPeriod);
                    }}
                  />
                </div>
                <div className={styles.form_group}>
                  <label>END PERIOD *</label>
                  <input
                    type="number" min="1" max="10" required
                    value={eventForm.endPeriod}
                    onChange={e => {
                      const ep = e.target.value;
                      setEventForm({ ...eventForm, endPeriod: ep });
                      fetchAvailableRoomsForForm(eventForm.timetableId, eventForm.date, eventForm.startPeriod, ep);
                    }}
                  />
                </div>
              </div>

              {/* LOCATION / ROOM */}
              <div className={styles.form_group}>
                <label>LOCATION / ROOM</label>
                {loadingRooms ? (
                  <div className={styles.text_muted}>Checking available rooms…</div>
                ) : (
                  <select
                    value={eventForm.locationId}
                    onChange={e => setEventForm({ ...eventForm, locationId: e.target.value })}
                  >
                    <option value="">No Specific Room</option>
                    {modalRooms.map(r => (
                      <option key={r.id} value={r.id}>
                        🟢 {r.roomNumber} ({r.roomType}{r.maximumCapacity ? `, Cap: ${r.maximumCapacity}` : ''})
                      </option>
                    ))}
                  </select>
                )}
                {!loadingRooms && modalRooms.length === 0 && eventForm.date && (
                  <small className={styles.text_warning ?? styles.text_muted}>
                    ⚠ No available rooms for this date &amp; period — all rooms are occupied.
                  </small>
                )}
                {!loadingRooms && modalRooms.length === 0 && !eventForm.date && (
                  <small className={styles.text_muted}>Select a date to see available rooms.</small>
                )}
              </div>

              {/* ORGANIZER */}
              <div className={styles.form_group}>
                <label>ORGANIZER *</label>
                <input
                  value={eventForm.organizer}
                  className={fieldErrors.organizer ? styles.input_error : ''}
                  onChange={e => updateField({ organizer: e.target.value })}
                />
                {fieldErrors.organizer && <span className={styles.field_error}>{fieldErrors.organizer}</span>}
              </div>

              {/* DESCRIPTION */}
              <div className={styles.form_group}>
                <label>DESCRIPTION</label>
                <textarea
                  rows={3}
                  value={eventForm.description}
                  className={fieldErrors.description ? styles.input_error : ''}
                  onChange={e => updateField({ description: e.target.value })}
                />
                {fieldErrors.description && <span className={styles.field_error}>{fieldErrors.description}</span>}
              </div>

              {/* SYNC WITH TIMETABLE toggle — shown only when timetable is selected */}
              {eventForm.timetableId && (
                <div className={styles.sync_toggle_row}>
                  <label className={styles.sync_label}>
                    <input
                      type="checkbox"
                      checked={eventForm.syncWithTimetable}
                      onChange={e => setEventForm({ ...eventForm, syncWithTimetable: e.target.checked })}
                    />
                    <span>
                      Sync with Timetable
                      <small className={styles.text_muted}> — propagate this event to the timetable schedule</small>
                    </span>
                  </label>
                </div>
              )}

              <div className={styles.modal_actions}>
                <button
                  type="button"
                  className={styles.btn_reset}
                  onClick={() => { setShowEventModal(false); setFieldErrors({}); }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className={styles.primary_btn}
                  disabled={savingEvent}
                >
                  {savingEvent ? 'Saving…' : 'Save Event'}
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
