// src/pages/ManageRoomsPage.jsx
// Standalone room management page — extracted from ConstraintsPage.
// Can be reached from Dashboard or from TT Builder (Step 1).
// `onContinue` takes the user back to wherever they came from.
import React, { useState } from 'react';
import styles from './ManageRoomsPage.module.css';

const ROOM_TYPE_OPTIONS = [
  { value: 'CLASSROOM',    label: 'Classroom' },
  { value: 'LABORATORY',   label: 'Laboratory' },
  { value: 'SEMINAR_HALL', label: 'Seminar Hall' },
  { value: 'AUDITORIUM',   label: 'Auditorium' },
];

export default function ManageRoomsPage({
  rooms = [],
  roomSummary,
  onAddRoom,
  onUpdateRoom,
  onDeleteRoom,
  onContinue,       // callback → back to caller (Dashboard or TT Builder)
  continueLabel = 'Continue →',
}) {
  const [editingId, setEditingId]         = useState(null);
  const [roomNumber, setRoomNumber]       = useState('');
  const [maximumCapacity, setMaximumCapacity] = useState('60');
  const [roomType, setRoomType]           = useState('CLASSROOM');
  const [hasProjector, setHasProjector]   = useState(false);
  const [hasAc, setHasAc]                 = useState(false);
  const [hasComputers, setHasComputers]   = useState(false);
  const [active, setActive]               = useState(true);
  const [formError, setFormError]         = useState('');

  function resetForm() {
    setEditingId(null);
    setRoomNumber('');
    setMaximumCapacity('60');
    setRoomType('CLASSROOM');
    setHasProjector(false);
    setHasAc(false);
    setHasComputers(false);
    setActive(true);
    setFormError('');
  }

  function handleEditClick(room) {
    setEditingId(room.id);
    setRoomNumber(room.roomNumber);
    setMaximumCapacity(String(room.maximumCapacity));
    setRoomType(room.roomType || 'CLASSROOM');
    setHasProjector(Boolean(room.hasProjector));
    setHasAc(Boolean(room.hasAc));
    setHasComputers(Boolean(room.hasComputers));
    setActive(room.active !== undefined ? Boolean(room.active) : true);
    setFormError('');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function handleFormSubmit(e) {
    e.preventDefault();
    if (!roomNumber.trim()) { setFormError('Room number is required.'); return; }
    const cap = parseInt(maximumCapacity, 10);
    if (isNaN(cap) || cap < 1) { setFormError('Maximum capacity must be at least 1.'); return; }

    const payload = {
      roomNumber: roomNumber.trim(),
      maximumCapacity: cap,
      roomType,
      hasProjector,
      hasAc,
      hasComputers,
      active,
    };

    if (editingId) {
      onUpdateRoom(editingId, payload);
    } else {
      onAddRoom(payload);
    }
    resetForm();
  }

  // Summary stats (fallback to local calculation)
  const stats = roomSummary || {
    totalRooms: rooms.length,
    activeRooms: rooms.filter(r => r.active !== false).length,
    inactiveRooms: rooms.filter(r => r.active === false).length,
    classrooms: rooms.filter(r => r.roomType === 'CLASSROOM').length,
    laboratories: rooms.filter(r => r.roomType === 'LABORATORY').length,
    seminarHalls: rooms.filter(r => r.roomType === 'SEMINAR_HALL').length,
    auditoriums: rooms.filter(r => r.roomType === 'AUDITORIUM').length,
    projectorEnabledRooms: rooms.filter(r => r.hasProjector).length,
    largestCapacity: rooms.length > 0 ? Math.max(...rooms.map(r => r.maximumCapacity || 0)) : 0,
  };

  return (
    <div className={styles.root}>
      {/* ── Header ── */}
      <div className={styles.header}>
        <div>
          <h1 className={styles.title}>🏠 Manage Rooms</h1>
          <p className={styles.subtitle}>
            Add, edit and manage rooms stored in the Resource Service database
          </p>
        </div>
      </div>

      {/* ── Summary Stats ── */}
      <div className={styles.stats_grid}>
        {[
          { val: stats.totalRooms,            lbl: 'Total Rooms',    color: 'var(--teal)' },
          { val: stats.activeRooms,           lbl: 'Active',         color: '#10b981' },
          { val: stats.inactiveRooms,         lbl: 'Inactive',       color: '#ef4444' },
          { val: stats.classrooms,            lbl: 'Classrooms',     color: 'var(--teal)' },
          { val: stats.laboratories,          lbl: 'Labs',           color: '#6366f1' },
          { val: stats.seminarHalls,          lbl: 'Seminar Halls',  color: '#d97706' },
          { val: stats.auditoriums,           lbl: 'Auditoriums',    color: '#db2777' },
          { val: stats.projectorEnabledRooms, lbl: 'With Projector', color: 'var(--teal)' },
        ].map(s => (
          <div key={s.lbl} className={styles.stat_card}>
            <span className={styles.stat_val} style={{ color: s.color }}>{s.val}</span>
            <span className={styles.stat_lbl}>{s.lbl}</span>
          </div>
        ))}
      </div>

      {/* ── Add / Edit Room Form ── */}
      <div className={styles.form_card}>
        <h2 className={styles.form_title}>{editingId ? '✏️ Edit Room' : '➕ Add New Room'}</h2>

        {formError && (
          <div className={styles.form_error}>{formError}</div>
        )}

        <form onSubmit={handleFormSubmit} className={styles.form_body}>
          <div className={styles.form_row}>
            <div className={styles.form_group}>
              <label>Room Number / Name *</label>
              <input
                value={roomNumber}
                onChange={e => setRoomNumber(e.target.value)}
                placeholder="e.g. 101, Lab-A"
              />
            </div>
            <div className={styles.form_group}>
              <label>Maximum Capacity *</label>
              <input
                type="number" min={1}
                value={maximumCapacity}
                onChange={e => setMaximumCapacity(e.target.value)}
              />
            </div>
            <div className={styles.form_group}>
              <label>Room Type *</label>
              <select value={roomType} onChange={e => setRoomType(e.target.value)}>
                {ROOM_TYPE_OPTIONS.map(o => (
                  <option key={o.value} value={o.value}>{o.label}</option>
                ))}
              </select>
            </div>
          </div>

          <div className={styles.checkbox_row}>
            {[
              { label: '📽️ Has Projector',  state: hasProjector,  setter: setHasProjector },
              { label: '❄️ Has AC',          state: hasAc,          setter: setHasAc },
              { label: '💻 Has Computers',  state: hasComputers,  setter: setHasComputers },
              { label: '✅ Active',          state: active,         setter: setActive },
            ].map(item => (
              <label key={item.label} className={styles.check_label}>
                <input
                  type="checkbox"
                  checked={item.state}
                  onChange={e => item.setter(e.target.checked)}
                />
                {item.label}
              </label>
            ))}
          </div>

          <div className={styles.form_actions}>
            <button type="submit" className={styles.btn_primary}>
              {editingId ? 'Save Changes' : 'Add Room'}
            </button>
            {editingId && (
              <button type="button" className={styles.btn_outline} onClick={resetForm}>
                Cancel Edit
              </button>
            )}
          </div>
        </form>
      </div>

      {/* ── Room List Table ── */}
      <div className={styles.table_card}>
        <h2 className={styles.form_title}>📋 Room List</h2>
        <div className={styles.table_wrap}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Room #</th>
                <th>Type</th>
                <th>Capacity</th>
                <th>Status</th>
                <th>Features</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rooms.length === 0 ? (
                <tr>
                  <td colSpan={6} className={styles.empty_row}>
                    No rooms added yet. Use the form above to add your first room.
                  </td>
                </tr>
              ) : (
                rooms.map(r => (
                  <tr key={r.id}>
                    <td><strong>{r.roomNumber}</strong></td>
                    <td>{r.roomType || 'CLASSROOM'}</td>
                    <td>{r.maximumCapacity}</td>
                    <td>
                      {r.active !== false ? (
                        <span className={styles.pill_active}>Active</span>
                      ) : (
                        <span className={styles.pill_inactive}>Inactive</span>
                      )}
                    </td>
                    <td>
                      {r.hasProjector && <span className={styles.feature}>📽️ Projector</span>}
                      {r.hasAc && <span className={styles.feature}>❄️ AC</span>}
                      {r.hasComputers && <span className={styles.feature}>💻 Computers</span>}
                      {!r.hasProjector && !r.hasAc && !r.hasComputers && (
                        <span className={styles.text_muted}>—</span>
                      )}
                    </td>
                    <td>
                      <div className={styles.row_actions}>
                        <button className={styles.btn_edit} onClick={() => handleEditClick(r)}>Edit</button>
                        <button
                          className={styles.btn_delete}
                          onClick={() => {
                            if (window.confirm(`Delete room "${r.roomNumber}"?`)) onDeleteRoom(r.id);
                          }}
                        >
                          Delete
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* ── Continue Footer ── */}
      <div className={styles.footer}>
        <div className={styles.footer_info}>
          <span className={styles.footer_count}>
            {rooms.length} room{rooms.length !== 1 ? 's' : ''} saved in Resource Service
          </span>
        </div>
        <button className={styles.btn_continue} onClick={onContinue}>
          {continueLabel}
        </button>
      </div>
    </div>
  );
}
