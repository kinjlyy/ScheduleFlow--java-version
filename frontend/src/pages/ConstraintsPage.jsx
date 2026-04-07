// src/pages/ConstraintsPage.jsx
import React from 'react';
import { Card, Alert, Divider, FieldLabel, Input, SectionTitle } from '../components/UI.jsx';
import StepFooter from '../components/StepFooter.jsx';
import styles from './ConstraintsPage.module.css';

export default function ConstraintsPage({
  daysPerWeek, setDaysPerWeek,
  periodsPerDay, setPeriodsPerDay,
  maxPerSection,
  teacherMaxLectures, setTeacherMax,
  allTeachers,
  onNext,
}) {
  return (
    <div className={styles.page}>
      {/* Step badge */}
      <div className={styles.step_badge}>Step 1 of 4</div>
      <SectionTitle>⚙️ Set Constraints First</SectionTitle>
      <p className={styles.intro}>
        Define your schedule boundaries before entering section data. These limits will be
        enforced in real-time as you build your timetable.
      </p>

      <Card style={{ maxWidth: 640 }}>
        {/* Days & Periods */}
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

        {/* Max lectures summary */}
        <div className={styles.max_box}>
          <div className={styles.max_calc}>
            <span className={styles.max_num}>{maxPerSection}</span>
            <span className={styles.max_label}>Max lectures per section per week</span>
          </div>
          <div className={styles.max_formula}>
            {daysPerWeek} days × {periodsPerDay} periods = {maxPerSection} slots
          </div>
        </div>

        <Alert type="info">
          All teacher weekly caps are automatically set to <strong>{maxPerSection}</strong> (the
          maximum possible). You can lower them below for individual teachers if needed.
        </Alert>

        <Divider />

        {/* Teacher caps */}
        <div className={styles.section_label}>Teacher Weekly Lecture Caps</div>
        <p className={styles.sub_hint}>
          Each teacher's cap is auto-set to <strong>{maxPerSection}</strong>. Lower it here to
          restrict how many lectures a teacher can take per week. Teachers added later will also
          inherit this value.
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
        nextLabel="Next: Setup Sections →"
        showPrev={false}
      />
    </div>
  );
}
