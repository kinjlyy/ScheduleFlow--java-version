// src/pages/SetupPage.jsx
import React from 'react';
import SectionPanel from '../components/SectionPanel.jsx';
import StepFooter from '../components/StepFooter.jsx';
import { Button } from '../components/UI.jsx';
import styles from './SetupPage.module.css';

export default function SetupPage({
  sections,
  onAddSection,
  onCopySection,
  onRemoveSection,
  onUpdateSection,
  onAddSubject,
  onRemoveSubject,
  onAddTeacher,
  onRemoveTeacher,
  onUpdateMapping,
  maxPerSection,
  teacherMaxLectures,
  validation,
  globalTeacherUsed,
  globalTeacherExceeds,
  onNext,
  onPrev,
}) {
  const { sectionErrors, hasErrors } = validation;

  if (sections.length === 0) {
    return (
      <div className={styles.page}>
        <div className={styles.empty}>
          <div className={styles.empty_icon}>Calendar</div>
          <div className={styles.empty_title}>No sections yet</div>
          <p className={styles.empty_sub}>Click below to create your first section.</p>
          <Button variant="primary" size="lg" onClick={onAddSection}>+ Add Section</Button>
        </div>
        <StepFooter
          onPrev={onPrev}
          onNext={onNext}
          nextLabel="Next: Review"
          nextDisabled={true}
          nextWarning="Add at least one section first"
        />
      </div>
    );
  }

  // Build next button warning message
  let nextWarning = null;
  if (hasErrors) {
    const errCount = Object.values(sectionErrors).filter(
      e => e.totalExceeds || Object.keys(e.teacherExceeds).length > 0
    ).length;
    nextWarning = `${errCount} section${errCount > 1 ? 's have' : ' has'} constraint violations`;
  }

  return (
    <div className={styles.page}>
      {/* Step badge */}
      <div className={styles.step_badge}>Step 2 of 4</div>
      <div className={styles.page_header}>
        <div>
          <h2 className={styles.page_title}>Setup Sections</h2>
          <p className={styles.page_sub}>
            Add subjects, teachers, and lecture counts for each section.
            All values are checked against your constraints in real-time.
          </p>
        </div>
        <div className={styles.header_actions}>
          <Button variant="outline" onClick={onAddSection}>+ Add Blank Section</Button>
        </div>
      </div>

      {/* Global teacher cap violations */}
      {Object.keys(globalTeacherExceeds).length > 0 && (
        <div className={styles.global_error}>
          <div className={styles.global_error_title}>Global Teacher Cap Violations</div>
          {Object.entries(globalTeacherExceeds).map(([t, { used, cap }]) => (
            <div key={t} className={styles.global_error_row}>
              <strong>{t}</strong> is assigned <strong>{used}</strong> lectures across all sections,
              but their weekly cap is <strong>{cap}</strong>.
              Please reduce their assignments by <strong>{used - cap}</strong>.
            </div>
          ))}
        </div>
      )}

      {/* Constraint summary bar */}
      <div className={styles.summary_bar}>
        <div className={styles.summary_item}>
          <span className={styles.summary_label}>Max per section</span>
          <span className={styles.summary_val}>{maxPerSection}</span>
        </div>
        <div className={styles.summary_divider} />
        <div className={styles.summary_item}>
          <span className={styles.summary_label}>Sections</span>
          <span className={styles.summary_val}>{sections.length}</span>
        </div>
        <div className={styles.summary_divider} />
        <div className={styles.summary_item}>
          <span className={styles.summary_label}>Status</span>
          <span className={`${styles.summary_status} ${hasErrors ? styles.status_error : styles.status_ok}`}>
            {hasErrors ? '⚠ Violations found' : '✓ All valid'}
          </span>
        </div>
      </div>

      {/* Section panels */}
      <div className={styles.panels}>
        {sections.map(sec => (
          <SectionPanel
            key={sec.id}
            section={sec}
            onUpdateSection={onUpdateSection}
            onAddSubject={onAddSubject}
            onRemoveSubject={onRemoveSubject}
            onAddTeacher={onAddTeacher}
            onRemoveTeacher={onRemoveTeacher}
            onUpdateMapping={onUpdateMapping}
            onRemove={onRemoveSection}
            maxPerSection={maxPerSection}
            teacherMaxLectures={teacherMaxLectures}
            sectionValidation={sectionErrors[sec.id]}
            globalTeacherUsed={globalTeacherUsed}
            globalTeacherExceeds={globalTeacherExceeds}
          />
        ))}
      </div>

      <div className={styles.add_row}>
        <div className={styles.add_controls}>
          <Button variant="outline" onClick={onAddSection}>+ Add Blank Section</Button>
          {sections.length > 0 && (
            <div className={styles.copy_wrap}>
              <span className={styles.copy_label}>Or copy from:</span>
              <select 
                className={styles.copy_select}
                onChange={(e) => {
                  if (e.target.value) {
                    onCopySection(e.target.value);
                    e.target.value = ''; // reset after copy
                  }
                }}
                defaultValue=""
              >
                <option value="" disabled>-- Select Section --</option>
                {sections.map(s => (
                  <option key={s.id} value={s.id}>{s.name || s.id}</option>
                ))}
              </select>
            </div>
          )}
        </div>
      </div>

      <StepFooter
        onPrev={onPrev}
        onNext={onNext}
        prevLabel="Back to Constraints"
        nextLabel="Next: Review"
        nextDisabled={hasErrors}
        nextWarning={nextWarning}
      />
    </div>
  );
}

