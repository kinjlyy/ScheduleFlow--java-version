import React, { useState, useEffect, useCallback } from 'react';
import {
  getAllEvents, createEvent, updateEvent, deleteEvent,
  getExecutionHistory, getAllTimetables, checkAvailability
} from '../api/eventApi.js';
import { fetchRooms } from '../api/roomApi.js';
import styles from './EventsPage.module.css';

const EVENT_CATEGORIES = ['GENERAL', 'ROOM_RESERVATION', 'ACADEMIC_EVENT'];
const EVENT_TYPES      = ['LECTURE', 'EXAM', 'WORKSHOP', 'SEMINAR', 'HOLIDAY', 'OTHER'];
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
  syncWithTimetable: false
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

  // Rooms fetched based on selected timetable & slot availability
  const [modalRooms, setModalRooms]         = useState([]);
  const [loadingRooms, setLoadingRooms]     = useState(false);

  // History modal state
  const [historyEvent, setHistoryEvent] = useState(null);
  const [historyData, setHistoryData]   = useState(null);

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

  // ── Fetch available rooms based on timetable, date & periods ─────────────
  const fetchAvailableRoomsForForm = useCallback(async (ttId, date, startP, endP) => {
    setLoadingRooms(true);
    setModalRooms([]);
    try {
      if (!ttId) {
        // "None" timetable selected -> fetch all rooms from Resource Service
        const allRooms = await fetchRooms(token).catch(() => []);
        const activeRooms = Array.isArray(allRooms) ? allRooms.filter(r => r.active !== false) : [];
        setModalRooms(activeRooms.length > 0 ? activeRooms : DEFAULT_FALLBACK_ROOMS);
      } else {
        // Timetable selected -> query availability endpoint for specified slot if date is present
        if (date && startP && endP) {
          const avail = await checkAvailability(date, Number(startP), Number(endP), ttId, token).catch(() => null);
          if (avail && Array.isArray(avail.availableRooms) && avail.availableRooms.length > 0) {
            const availList = avail.availableRooms.map(item => item.room || item);
            setModalRooms(availList);
          } else {
            const allRooms = await fetchRooms(token).catch(() => []);
            const activeRooms = Array.isArray(allRooms) ? allRooms.filter(r => r.active !== false) : [];
            setModalRooms(activeRooms.length > 0 ? activeRooms : DEFAULT_FALLBACK_ROOMS);
          }
        } else {
          // If date/periods not set yet, list all active rooms from Resource Service
          const allRooms = await fetchRooms(token).catch(() => []);
          const activeRooms = Array.isArray(allRooms) ? allRooms.filter(r => r.active !== false) : [];
          setModalRooms(activeRooms.length > 0 ? activeRooms : DEFAULT_FALLBACK_ROOMS);
        }
      }
    } catch (err) {
      console.warn('Could not fetch available rooms:', err.message);
      setModalRooms(DEFAULT_FALLBACK_ROOMS);
    } finally {
      setLoadingRooms(false);
    }
  }, [token]);

  // ── Event CRUD handlers ────────────────────────────────────────────────
  function openCreateEvent() {
    setEditingEvent(null);
    setEventForm(EMPTY_EVENT_FORM);
    setModalRooms([]);
    setShowEventModal(true);
    fetchAvailableRoomsForForm('', EMPTY_EVENT_FORM.date, EMPTY_EVENT_FORM.startPeriod, EMPTY_EVENT_FORM.endPeriod);
  }

  function openEditEvent(ev) {
    setEditingEvent(ev);
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
    try {
      const payload = {
        ...eventForm,
        locationId: eventForm.locationId ? Number(eventForm.locationId) : null,
        timetableId: eventForm.timetableId ? Number(eventForm.timetableId) : null,
        startPeriod: Number(eventForm.startPeriod),
        endPeriod: Number(eventForm.endPeriod)
      };
      // Remove UI-only field before sending to backend
      delete payload.syncWithTimetable;

      if (editingEvent) {
        await updateEvent(editingEvent.id, payload, token);
      } else {
        await createEvent(payload, token);
      }

      // If sync was requested, notify parent to refresh timetable view
      if (eventForm.syncWithTimetable && onTimetableRefreshed) {
        onTimetableRefreshed();
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

              {/* TITLE */}
              <div className={styles.form_group}>
                <label>TITLE *</label>
                <input
                  required
                  value={eventForm.title}
                  onChange={e => setEventForm({ ...eventForm, title: e.target.value })}
                />
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

              {/* TARGET TIMETABLE — ask first */}
              <div className={styles.form_group}>
                <label>TARGET TIMETABLE</label>
                <select
                  value={eventForm.timetableId}
                  onChange={e => {
                    const ttId = e.target.value;
                    setEventForm({ ...eventForm, timetableId: ttId, locationId: '' });
                    fetchAvailableRoomsForForm(ttId, eventForm.date, eventForm.startPeriod, eventForm.endPeriod);
                  }}
                >
                  <option value="">None / All Rooms (From Resource Service)</option>
                  {timetables.map(tt => (
                    <option key={tt.id} value={tt.id}>
                      Timetable #{tt.id} ({tt.sectionName}{tt.active ? ' [ACTIVE]' : ''})
                    </option>
                  ))}
                </select>
                <small className={styles.text_muted}>
                  {eventForm.timetableId
                    ? 'Showing available rooms for this timetable slot.'
                    : 'Showing all active rooms from Resource Service.'}
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
                    onChange={e => {
                      const d = e.target.value;
                      setEventForm({ ...eventForm, date: d });
                      fetchAvailableRoomsForForm(eventForm.timetableId, d, eventForm.startPeriod, eventForm.endPeriod);
                    }}
                  />
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
                {!loadingRooms && modalRooms.length === 0 && (
                  <small className={styles.text_muted}>No available rooms found for this slot.</small>
                )}
              </div>

              {/* ORGANIZER */}
              <div className={styles.form_group}>
                <label>ORGANIZER</label>
                <input
                  value={eventForm.organizer}
                  onChange={e => setEventForm({ ...eventForm, organizer: e.target.value })}
                />
              </div>

              {/* DESCRIPTION */}
              <div className={styles.form_group}>
                <label>DESCRIPTION</label>
                <textarea
                  rows={3}
                  value={eventForm.description}
                  onChange={e => setEventForm({ ...eventForm, description: e.target.value })}
                />
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
                  onClick={() => setShowEventModal(false)}
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
