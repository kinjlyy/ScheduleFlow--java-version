// src/pages/ReviewPage.jsx
import React from 'react';
import { Card, Tag, SectionTitle } from '../components/UI.jsx';
import StepFooter from '../components/StepFooter.jsx';
import styles from './ReviewPage.module.css';

export default function ReviewPage({ sections, validation, onNext, onPrev }) {
  const { sectionErrors, hasErrors } = validation || { sectionErrors: {}, hasErrors: false };

  if (sections.length === 0) {
    return (
      <div className={styles.page}>
        <div className={styles.empty}>
          <p>No sections to review. Go back and add sections first.</p>
        </div>
        <StepFooter
          onPrev={onPrev}
          onNext={onNext}
          prevLabel="← Back to Setup"
          nextLabel="Generate Timetable →"
          nextDisabled={true}
        />
      </div>
    );
  }

  return (
    <div className={styles.page}>
      <div className={styles.step_badge}>Step 3 of 4</div>
      <SectionTitle>📋 Review All Sections</SectionTitle>
      <p className={styles.intro}>
        Confirm everything looks correct before generating the timetable.
        {hasErrors && ' Fix all constraint violations in Setup before proceeding.'}
      </p>

      <div className={styles.grid}>
        {sections.map(sec => {
          const errs = sectionErrors[sec.id] || {};
          const hasErr = errs.totalExceeds || Object.keys(errs.teacherExceeds || {}).length > 0;
          return (
            <Card key={sec.id} className={`${styles.sec_card} ${hasErr ? styles.card_err : ''}`}>
              <div className={styles.card_header}>
                <span className={styles.sec_name}>{sec.name || '(unnamed)'}</span>
                <div className={styles.card_badges}>
                  <span className={styles.cap_badge}>Cap: {sec.capacity || '—'}</span>
                  {hasErr
                    ? <span className={styles.err_badge}>⚠ Violations</span>
                    : <span className={styles.ok_badge}>✓ OK</span>
                  }
                </div>
              </div>

              {/* Budget bar */}
              {errs.totalUsed !== undefined && (
                <div className={styles.mini_budget}>
                  <div className={styles.mini_bar_track}>
                    <div
                      className={styles.mini_bar_fill}
                      style={{
                        width: `${Math.min(100, Math.round((errs.totalUsed / (errs.totalUsed + 1)) * 100))}%`,
                        background: errs.totalExceeds ? 'var(--danger)' : 'var(--teal)'
                      }}
                    />
                  </div>
                  <span className={`${styles.mini_count} ${errs.totalExceeds ? styles.over : ''}`}>
                    {errs.totalUsed} lectures/week
                  </span>
                </div>
              )}

              <div className={styles.sub_label}>Subjects</div>
              <div className={styles.tag_row}>
                {sec.subjects.length > 0
                  ? sec.subjects.map(s => <Tag key={s} label={s} />)
                  : <span className={styles.none}>None</span>}
              </div>

              <div className={styles.sub_label}>Teachers</div>
              <div className={styles.tag_row}>
                {sec.teachers.length > 0
                  ? sec.teachers.map(t => {
                      const tOver = !!(errs.teacherExceeds?.[t]);
                      return <Tag key={t} label={tOver ? `⚠ ${t}` : t} />;
                    })
                  : <span className={styles.none}>None</span>}
              </div>

              {sec.subjects.length > 0 && (
                <>
                  <div className={styles.sub_label}>Subject–Teacher Mapping</div>
                  <table className={styles.map_table}>
                    <thead>
                      <tr><th>Subject</th><th>Teacher</th><th>Lec/wk</th></tr>
                    </thead>
                    <tbody>
                      {sec.subjects.map(subj => {
                        const m = sec.mapping[subj] || {};
                        return (
                          <tr key={subj}>
                            <td>{subj}</td>
                            <td>{m.teacher || <span className={styles.none}>—</span>}</td>
                            <td>{m.lecturesPerWeek ?? '—'}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </>
              )}
            </Card>
          );
        })}
      </div>

      <StepFooter
        onPrev={onPrev}
        onNext={onNext}
        prevLabel="← Back to Setup"
        nextLabel="⚡ Generate Timetable →"
        nextDisabled={hasErrors}
        nextWarning={hasErrors ? 'Fix constraint violations before generating' : null}
      />
    </div>
  );
}

