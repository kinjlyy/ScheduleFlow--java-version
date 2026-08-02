// src/pages/ConstraintsPage.jsx
import React, { useState } from 'react';
import { Card, Alert, Divider, FieldLabel, Input, Select, Button, SectionTitle } from '../components/UI.jsx';
import StepFooter from '../components/StepFooter.jsx';
import styles from './ConstraintsPage.module.css';

const ROOM_TYPE_OPTIONS = [
  { value: 'CLASSROOM', label: 'Classroom' },
  { value: 'LABORATORY', label: 'Laboratory' },
  { value: 'SEMINAR_HALL', label: 'Seminar Hall' },
  { value: 'AUDITORIUM', label: 'Auditorium' },
];

export default function ConstraintsPage({
  daysPerWeek, setDaysPerWeek,
  periodsPerDay, setPeriodsPerDay,
  maxPerSection,
  teacherMaxLectures, setTeacherMax,
  allTeachers,
  roomAllocationStrategy, setRoomAllocationStrategy,
  manageRooms, setManageRooms,
  rooms = [],
  roomSummary,
  onAddRoom, onUpdateRoom, onDeleteRoom,
  onOpenManageRooms,
  onNext,
}) {
  // Local state for Room Form (Add / Edit)
  const [editingId, setEditingId] = useState(null);
  const [roomNumber, setRoomNumber] = useState('');
  const [maximumCapacity, setMaximumCapacity] = useState('60');
  const [roomType, setRoomType] = useState('CLASSROOM');
  const [hasProjector, setHasProjector] = useState(false);
  const [hasAc, setHasAc] = useState(false);
  const [hasComputers, setHasComputers] = useState(false);
  const [active, setActive] = useState(true);
  const [formError, setFormError] = useState('');

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
  }

  function handleFormSubmit(e) {
    e.preventDefault();
    if (!roomNumber.trim()) {
      setFormError('Room number is required.');
      return;
    }
    const cap = parseInt(maximumCapacity, 10);
    if (isNaN(cap) || cap < 1) {
      setFormError('Maximum capacity must be at least 1.');
      return;
    }

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

  // Calculate live summary stats from local rooms state if backend summary is null/loading
  const summaryStats = roomSummary || {
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
    <div className={styles.page}>
      <div className={styles.step_badge}>Step 1 of 4</div>
      <SectionTitle>Set Constraints &amp; Strategy</SectionTitle>
      <p className={styles.intro}>
        Define schedule dimensions, global room allocation strategy, and optional room infrastructure before building section details.
      </p>

      <Card style={{ maxWidth: 760 }}>
        {/* Schedule Dimensions */}
        <div className={styles.section_label}>Schedule Dimensions</div>
        <div className={styles.row2}>
          <div className={styles.field}>
            <FieldLabel>Days Per Week</FieldLabel>
            <Input
              type="number" min={1} max={7}
              value={daysPerWeek}
              onChange={e => setDaysPerWeek(e.target.value)}
            />
          </div>
          <div className={styles.field}>
            <FieldLabel>Periods Per Day</FieldLabel>
            <Input
              type="number" min={1} max={12}
              value={periodsPerDay}
              onChange={e => setPeriodsPerDay(e.target.value)}
            />
          </div>
        </div>

        <div className={styles.max_box}>
          <div className={styles.max_calc}>
            <span className={styles.max_num}>{maxPerSection}</span>
            <span className={styles.max_label}>Max lectures per section per week</span>
          </div>
          <div className={styles.max_formula}>
            {daysPerWeek} days × {periodsPerDay} periods = {maxPerSection} slots
          </div>
        </div>

        <Divider />

        {/* ── Room Allocation Strategy ── */}
        <div className={styles.section_label}>Room Allocation Strategy</div>
        <p className={styles.sub_hint}>
          Select how room scheduling should be handled across the institution.
        </p>

        <div className={styles.strategy_grid}>
          <div
            className={`${styles.strategy_card} ${roomAllocationStrategy === 'DYNAMIC_ALLOCATION' ? styles.strategy_active : ''}`}
            onClick={() => setRoomAllocationStrategy('DYNAMIC_ALLOCATION')}
          >
            <span className={styles.strategy_title}>
              ⚡ Dynamic Allocation
            </span>
            <span className={styles.strategy_desc}>
              Rooms are automatically assigned by the scheduling algorithm.
            </span>
            <span className={styles.strategy_badge}>Best for Universities</span>
          </div>

          <div
            className={`${styles.strategy_card} ${roomAllocationStrategy === 'FIXED_CLASSROOM' ? styles.strategy_active : ''}`}
            onClick={() => setRoomAllocationStrategy('FIXED_CLASSROOM')}
          >
            <span className={styles.strategy_title}>
              🏫 Fixed Classroom
            </span>
            <span className={styles.strategy_desc}>
              Every section stays in its assigned classroom. Teachers move.
            </span>
            <span className={styles.strategy_badge}>Best for Schools</span>
          </div>

          <div
            className={`${styles.strategy_card} ${roomAllocationStrategy === 'HYBRID' ? styles.strategy_active : ''}`}
            onClick={() => setRoomAllocationStrategy('HYBRID')}
          >
            <span className={styles.strategy_title}>
              🔄 Hybrid Mode
            </span>
            <span className={styles.strategy_desc}>
              Some sections use fixed classrooms while others use automatic allocation.
            </span>
            <span className={styles.strategy_badge}>Flexible Option</span>
          </div>
        </div>

        <Divider />

        {/* ── Room Management (Optional) ── */}
        <div className={styles.toggle_box}>
          <div className={styles.toggle_label}>
            Do you want to manage rooms in the timetable?
          </div>
          <div className={styles.btn_group}>
            <Button
              variant={manageRooms ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setManageRooms(true)}
            >
              Yes
            </Button>
            <Button
              variant={!manageRooms ? 'primary' : 'outline'}
              size="sm"
              onClick={() => setManageRooms(false)}
            >
              No
            </Button>
          </div>
        </div>

        {manageRooms && (
          <div>
            <div style={{ margin: '14px 0 16px 0', padding: '14px', background: '#f8fafc', border: '1px solid #e2e8f0', borderRadius: '8px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div>
                <strong style={{ fontSize: '0.95rem', color: '#1e293b' }}>Manage Rooms Module</strong>
                <div style={{ fontSize: '0.85rem', color: '#64748b', marginTop: 2 }}>
                  Open the dedicated room management page to add, edit, or delete rooms in Resource Service.
                </div>
              </div>
              <Button
                variant="primary"
                size="sm"
                onClick={onOpenManageRooms}
                style={{ background: '#0284c7', borderColor: '#0284c7', fontWeight: 600 }}
              >
                🏢 Add / Edit Rooms (Manage Rooms Page) →
              </Button>
            </div>

            {/* Room Summary Dashboard */}
            <div className={styles.section_label} style={{ marginTop: 14 }}>Room Summary Dashboard</div>
            <div className={styles.dash_grid}>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.totalRooms}</span>
                <span className={styles.dash_lbl}>Total Rooms</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val} style={{ color: '#10b981' }}>{summaryStats.activeRooms}</span>
                <span className={styles.dash_lbl}>Active Rooms</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val} style={{ color: '#ef4444' }}>{summaryStats.inactiveRooms}</span>
                <span className={styles.dash_lbl}>Inactive Rooms</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.laboratories}</span>
                <span className={styles.dash_lbl}>Laboratories</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.seminarHalls}</span>
                <span className={styles.dash_lbl}>Seminar Halls</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.auditoriums}</span>
                <span className={styles.dash_lbl}>Auditoriums</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.projectorEnabledRooms}</span>
                <span className={styles.dash_lbl}>Projector Rooms</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.largestCapacity}</span>
                <span className={styles.dash_lbl}>Max Room Cap</span>
              </div>
            </div>

            {/* Room Form */}
            <form onSubmit={handleFormSubmit} className={styles.room_form}>
              <div className={styles.section_label} style={{ marginBottom: 4 }}>
                {editingId ? 'Edit Room' : 'Add New Room'}
              </div>

              {formError && <Alert type="danger">{formError}</Alert>}

              <div className={styles.form_row}>
                <div className={styles.field} style={{ flex: 1 }}>
                  <FieldLabel>Room Number / Name</FieldLabel>
                  <Input
                    value={roomNumber}
                    onChange={e => setRoomNumber(e.target.value)}
                    placeholder="e.g. 101, Lab-A"
                  />
                </div>

                <div className={styles.field} style={{ flex: 1 }}>
                  <FieldLabel>Maximum Capacity</FieldLabel>
                  <Input
                    type="number"
                    min={1}
                    value={maximumCapacity}
                    onChange={e => setMaximumCapacity(e.target.value)}
                  />
                </div>

                <div className={styles.field} style={{ flex: 1 }}>
                  <FieldLabel>Room Type</FieldLabel>
                  <Select
                    value={roomType}
                    onChange={e => setRoomType(e.target.value)}
                    options={ROOM_TYPE_OPTIONS}
                  />
                </div>
              </div>

              <div className={styles.checkbox_group}>
                <label className={styles.checkbox_item}>
                  <input
                    type="checkbox"
                    checked={hasProjector}
                    onChange={e => setHasProjector(e.target.checked)}
                  />
                  Has Projector
                </label>
                <label className={styles.checkbox_item}>
                  <input
                    type="checkbox"
                    checked={hasAc}
                    onChange={e => setHasAc(e.target.checked)}
                  />
                  Has AC
                </label>
                <label className={styles.checkbox_item}>
                  <input
                    type="checkbox"
                    checked={hasComputers}
                    onChange={e => setHasComputers(e.target.checked)}
                  />
                  Has Computers
                </label>
                <label className={styles.checkbox_item}>
                  <input
                    type="checkbox"
                    checked={active}
                    onChange={e => setActive(e.target.checked)}
                  />
                  Active Status
                </label>
              </div>

              <div style={{ display: 'flex', gap: 8, marginTop: 4 }}>
                <Button type="submit" variant="primary" size="sm">
                  {editingId ? 'Save Changes' : 'Add Room'}
                </Button>
                {editingId && (
                  <Button variant="outline" size="sm" onClick={resetForm}>
                    Cancel
                  </Button>
                )}
              </div>
            </form>

            {/* Room List Table */}
            <div className={styles.room_table_wrap}>
              <table className={styles.room_table}>
                <thead>
                  <tr>
                    <th>Room #</th>
                    <th>Type</th>
                    <th>Max Cap</th>
                    <th>Status</th>
                    <th>Features</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rooms.length === 0 ? (
                    <tr>
                      <td colSpan={6} style={{ textAlign: 'center', color: 'var(--muted)', padding: 16 }}>
                        No rooms created yet. Add your first room above!
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
                            <span className={styles.status_pill_active}>Active</span>
                          ) : (
                            <span className={styles.status_pill_inactive}>Inactive</span>
                          )}
                        </td>
                        <td>
                          {r.hasProjector && <span className={styles.feature_badge}>📽️ Projector</span>}
                          {r.hasAc && <span className={styles.feature_badge}>❄️ AC</span>}
                          {r.hasComputers && <span className={styles.feature_badge}>💻 Computers</span>}
                          {!r.hasProjector && !r.hasAc && !r.hasComputers && <span style={{ color: 'var(--muted)' }}>—</span>}
                        </td>
                        <td>
                          <div style={{ display: 'flex', gap: 6 }}>
                            <Button size="sm" variant="outline" onClick={() => handleEditClick(r)}>
                              Edit
                            </Button>
                            <Button size="sm" variant="danger" onClick={() => onDeleteRoom(r.id)}>
                              Delete
                            </Button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        )}

        <Divider />

        {/* Teacher caps */}
        <div className={styles.section_label}>Teacher Weekly Lecture Caps</div>
        <p className={styles.sub_hint}>
          Each teacher's cap is auto-set to <strong>{maxPerSection}</strong>. Lower it here to
          restrict how many lectures a teacher can take per week.
        </p>

        {allTeachers.length === 0 ? (
          <div className={styles.no_teachers}>
            <span className={styles.no_teachers_icon}>👩‍🏫</span>
            <span>No teachers added yet — they'll appear here after you set up sections in Step 2.</span>
          </div>
        ) : (
          <div className={styles.teacher_rows}>
            {allTeachers.map(t => {
              const val = teacherMaxLectures[t] ?? maxPerSection;
              const isAtMax = val >= maxPerSection;
              const isReduced = val < maxPerSection;
              return (
                <div key={t} className={styles.teacher_row}>
                  <span className={styles.teacher_name}>{t}</span>
                  <div className={styles.teacher_input_wrap}>
                    <Input
                      type="number"
                      min={1}
                      max={maxPerSection}
                      value={val}
                      onChange={e => setTeacherMax(t, e.target.value)}
                      style={{ width: 90 }}
                    />
                  </div>
                  <span className={styles.teacher_unit}>/ {maxPerSection} max</span>
                  {isReduced && (
                    <span className={styles.reduced_badge}>
                      reduced to {Math.round((val / maxPerSection) * 100)}%
                    </span>
                  )}
                  {isAtMax && (
                    <span className={styles.max_badge}>full capacity</span>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </Card>

      <StepFooter
        onNext={onNext}
        nextLabel="Next: Setup Sections"
        showPrev={false}
      />
    </div>
  );
}
