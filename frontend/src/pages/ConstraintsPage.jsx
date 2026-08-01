// src/pages/ConstraintsPage.jsx
import React from 'react';
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
  onGoToManageRooms,   // navigate to ManageRoomsPage (from TT builder context)
  onNext,
}) {
  // Calculate live summary stats
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
            {/* Room Summary Stats */}
            <div className={styles.section_label} style={{ marginTop: 14 }}>Room Summary</div>
            <div className={styles.dash_grid}>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.totalRooms}</span>
                <span className={styles.dash_lbl}>Total Rooms</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val} style={{ color: '#10b981' }}>{summaryStats.activeRooms}</span>
                <span className={styles.dash_lbl}>Active</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.laboratories}</span>
                <span className={styles.dash_lbl}>Labs</span>
              </div>
              <div className={styles.dash_card}>
                <span className={styles.dash_val}>{summaryStats.classrooms}</span>
                <span className={styles.dash_lbl}>Classrooms</span>
              </div>
            </div>

            {/* Existing rooms — read-only preview */}
            {rooms.length > 0 && (
              <div className={styles.room_table_wrap}>
                <table className={styles.room_table}>
                  <thead>
                    <tr>
                      <th>Room #</th>
                      <th>Type</th>
                      <th>Capacity</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {rooms.map(r => (
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
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            {/* Action buttons */}
            <div className={styles.room_actions_row}>
              <Button variant="outline" size="sm" onClick={onGoToManageRooms}>
                🏠 + Add / Edit Rooms
              </Button>
              <Button variant="primary" size="sm" onClick={onNext}>
                Continue TT Generation →
              </Button>
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
