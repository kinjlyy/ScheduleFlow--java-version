// src/components/SectionPanel.jsx
import React, { useState } from 'react';
import styles from './SectionPanel.module.css';
import { Button, Input, Select, Tag, Divider, FieldLabel, Alert } from './UI.jsx';

export default function SectionPanel({
  section,
  onUpdateSection,
  onAddSubject,
  onRemoveSubject,
  onAddTeacher,
  onRemoveTeacher,
  onUpdateMapping,
  onRemove,
  // Validation props from App
  maxPerSection,
  teacherMaxLectures,
  sectionValidation, // { totalUsed, totalExceeds }
  globalTeacherUsed,
  globalTeacherExceeds,
}) {
  const [subjInput, setSubjInput] = useState('');
  const [teachInput, setTeachInput] = useState('');

  function handleAddSubject() {
    if (!subjInput.trim()) return;
    onAddSubject(section.id, subjInput.trim());
    setSubjInput('');
  }

  function handleAddTeacher() {
    if (!teachInput.trim()) return;
    onAddTeacher(section.id, teachInput.trim());
    setTeachInput('');
  }

  const teacherOptions = section.teachers.map(t => ({ value: t, label: t }));
  const totalUsed = sectionValidation?.totalUsed ?? 0;
  const totalExceeds = sectionValidation?.totalExceeds ?? false;
  const pct = maxPerSection > 0 ? Math.min(100, Math.round((totalUsed / maxPerSection) * 100)) : 0;
  const barColor = totalExceeds ? '#e05252' : pct >= 80 ? '#d97706' : 'var(--teal)';

  return (
    <div className={`${styles.panel} ${totalExceeds ? styles.panel_error : ''}`} id={`section-panel-${section.id}`}>
      {/* Header */}
      <div className={styles.header}>
        <div className={styles.title_row}>
          <span className={styles.title}>
            {section.name ? `Section ${section.name}` : 'New Section'}
          </span>
          {totalExceeds && <span className={styles.error_badge}>Over Limit</span>}
          {!totalExceeds && totalUsed > 0 && <span className={styles.ok_badge}>Within Limits</span>}
        </div>
        <Button variant="danger" size="icon" onClick={() => onRemove(section.id)} title="Delete section">Delete</Button>
      </div>

      {/* Name + Capacity */}
      <div className={styles.row2}>
        <div className={styles.field}>
          <FieldLabel>Section Name</FieldLabel>
          <Input
            value={section.name}
            onChange={e => onUpdateSection(section.id, 'name', e.target.value)}
            placeholder="e.g. A"
          />
        </div>
        <div className={styles.field}>
          <FieldLabel>Capacity</FieldLabel>
          <Input
            type="number"
            value={section.capacity}
            onChange={e => onUpdateSection(section.id, 'capacity', e.target.value)}
            placeholder="e.g. 60"
            min={1}
          />
        </div>
      </div>

      <Divider />

      {/* Subjects */}
      <FieldLabel>Subjects</FieldLabel>
      <div className={styles.tags}>
        {section.subjects.map(s => (
          <Tag key={s} label={s} onRemove={() => onRemoveSubject(section.id, s)} />
        ))}
      </div>
      <div className={styles.adder}>
        <Input
          value={subjInput}
          onChange={e => setSubjInput(e.target.value)}
          placeholder="Subject name"
          onKeyDown={e => e.key === 'Enter' && handleAddSubject()}
        />
        <Button variant="primary" size="sm" onClick={handleAddSubject}>Add</Button>
      </div>

      <Divider />

      {/* Teachers */}
      <FieldLabel>Teachers</FieldLabel>
      <div className={styles.tags}>
        {section.teachers.map(t => {
          const exceeded = !!globalTeacherExceeds[t];
          return (
            <span key={t} className={`${styles.teacher_tag_wrap}`}>
              <Tag
                label={`${t}${exceeded ? ' (Limit Exceeded)' : ''}`}
                onRemove={() => onRemoveTeacher(section.id, t)}
              />
            </span>
          );
        })}
      </div>
      <div className={styles.adder}>
        <Input
          value={teachInput}
          onChange={e => setTeachInput(e.target.value)}
          placeholder="Teacher name"
          onKeyDown={e => e.key === 'Enter' && handleAddTeacher()}
        />
        <Button variant="primary" size="sm" onClick={handleAddTeacher}>Add</Button>
      </div>

      {/* Mapping table */}
      {section.subjects.length > 0 && (
        <>
          <Divider />

          {/* ── Live lecture budget meter ── */}
          <div className={styles.budget_row}>
            <FieldLabel>Weekly Lecture Budget</FieldLabel>
            <span className={`${styles.budget_count} ${totalExceeds ? styles.budget_over : ''}`}>
              {totalUsed} / {maxPerSection}
            </span>
          </div>
          <div className={styles.budget_bar_track}>
            <div
              className={styles.budget_bar_fill}
              style={{ width: `${pct}%`, background: barColor }}
            />
          </div>
          {totalExceeds && (
            <div className={styles.budget_error}>
              ⛔ Error: Total lectures ({totalUsed}) exceed the max allowed ({maxPerSection}).
              Reduce lecture counts below.
            </div>
          )}
          {!totalExceeds && pct >= 80 && totalUsed > 0 && (
            <div className={styles.budget_warn}>
              ⚠️ Warning: Approaching limit — {maxPerSection - totalUsed} slot{maxPerSection - totalUsed !== 1 ? 's' : ''} remaining.
            </div>
          )}

          <FieldLabel style={{ marginTop: 14 }}>Map Subjects ➜ Teachers &amp; Lectures/Week</FieldLabel>
          {section.teachers.length === 0 && (
            <Alert type="warn">Add at least one teacher to assign subjects.</Alert>
          )}

          {/* Per-teacher cap violations */}
          {/* Global teacher cap violations related to this section */}
          {section.teachers.filter(t => globalTeacherExceeds[t]).map(t => {
            const { used, cap } = globalTeacherExceeds[t];
            return (
              <div key={t} className={styles.teacher_cap_error}>
                ⛔ Limit Reached: <strong>{t}</strong> (Global): assigned {used} lectures but their cap is {cap}.
              </div>
            );
          })}

          <div className={styles.map_wrap}>
            <table className={styles.map_table}>
              <thead>
                <tr>
                  <th>Subject</th>
                  <th>Teacher</th>
                  <th>Lectures / Week</th>
                  <th>Teacher Used / Cap</th>
                </tr>
              </thead>
              <tbody>
                {section.subjects.map(subj => {
                  const m = section.mapping[subj] || { teacher: '', lecturesPerWeek: 4 };
                  const isOver = m.teacher && globalTeacherExceeds[m.teacher];
                  return (
                    <tr key={subj} className={isOver ? styles.row_error : ''}>
                      <td className={styles.subj_cell}>{subj}</td>
                      <td>
                        <Select
                          value={m.teacher}
                          onChange={e => onUpdateMapping(section.id, subj, 'teacher', e.target.value)}
                          options={teacherOptions}
                          placeholder="-- Teacher --"
                        />
                      </td>
                      <td>
                        <Input
                          type="number"
                          min={1}
                          max={maxPerSection}
                          value={m.lecturesPerWeek}
                          onChange={e => onUpdateMapping(section.id, subj, 'lecturesPerWeek', +e.target.value)}
                          style={{ width: '90px' }}
                        />
                      </td>
                      <td>
                        {m.teacher ? (
                          <span className={`${styles.cap_pill} ${globalTeacherExceeds[m.teacher] ? styles.cap_pill_over : ''}`}>
                            {globalTeacherUsed[m.teacher] || 0} / {teacherMaxLectures[m.teacher] ?? maxPerSection}
                          </span>
                        ) : (
                          <span className={styles.no_teacher_hint}>—</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}
    </div>
  );
}

