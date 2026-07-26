// src/pages/ReviewPage.jsx
import React from 'react';
import { Card, SectionTitle, Divider } from '../components/UI.jsx';
import StepFooter from '../components/StepFooter.jsx';
import styles from './ReviewPage.module.css';

export default function ReviewPage({
  sections,
  rooms = [],
  roomAllocationStrategy = 'DYNAMIC_ALLOCATION',
  validation,
  onNext,
  onPrev,
  maxPerSection,
}) {
  const { sectionErrors, globalTeacherExceeds, hasErrors } = validation || {
    sectionErrors: {},
    globalTeacherExceeds: {},
    hasErrors: false,
  };

  if (sections.length === 0) {
    return (
      <div className={styles.page}>
        <div className={styles.empty}>
          <p>No sections to review. Go back and add sections first.</p>
        </div>
        <StepFooter
          onPrev={onPrev}
          onNext={onNext}
          prevLabel="Back to Setup"
          nextLabel="Next: Generate"
          nextDisabled={true}
        />
      </div>
    );
  }

  const roomMap = new Map(rooms.map(r => [r.id, r]));

  return (
    <div className={styles.page}>
      <div className={styles.step_badge}>Step 3 of 4</div>
      <SectionTitle>Review All Sections</SectionTitle>
      <p className={styles.intro}>
        Final check before generation. Ensure all sections are valid, room allocations are configured, and teacher loads are within their caps.
      </p>

      {/* Summary Stats */}
      <div className={styles.summary_row}>
        <div className={styles.stat_card}>
          <span className={styles.stat_label}>Total Sections</span>
          <span className={styles.stat_val}>{sections.length}</span>
        </div>
        <div className={styles.stat_card}>
          <span className={styles.stat_label}>Allocation Strategy</span>
          <span className={styles.stat_val} style={{ fontSize: '1rem', color: 'var(--teal)' }}>
            {roomAllocationStrategy.replace('_', ' ')}
          </span>
        </div>
        <div className={styles.stat_card}>
          <span className={styles.stat_label}>Validation Status</span>
          <span className={`${styles.stat_val} ${hasErrors ? styles.val_error : styles.val_ok}`}>
            {hasErrors ? 'Action Required' : 'Ready to Generate'}
          </span>
        </div>
      </div>

      <div className={styles.grid}>
        {sections.map(sec => {
          const errs = sectionErrors[sec.id] || {};
          const isOver = errs.totalExceeds || Object.keys(errs.teacherExceeds || {}).length > 0;
          const fixedRoom = sec.fixedRoomId ? roomMap.get(sec.fixedRoomId) : null;

          return (
            <Card key={sec.id} className={isOver ? styles.card_error : ''}>
              <div className={styles.card_header}>
                <h3 className={styles.sec_name}>Section {sec.name || sec.id}</h3>
                {isOver ? (
                  <span className={styles.badge_error}>Action Required</span>
                ) : (
                  <span className={styles.badge_ok}>Valid</span>
                )}
              </div>

              <div className={styles.sec_stats}>
                <span>Capacity: {sec.capacity || '—'}</span>
                <span>Fixed Room: {fixedRoom ? `${fixedRoom.roomNumber} (${fixedRoom.roomType})` : 'Auto Allocate'}</span>
                <span>Subjects: {sec.subjects.length}</span>
              </div>

              <Divider />

              <div className={styles.mapping_list}>
                {sec.subjects.map(subj => {
                  const m = sec.mapping[subj] || {};
                  const teacherErr = globalTeacherExceeds?.[m.teacher];
                  return (
                    <div key={subj} className={styles.mapping_item} style={{ flexDirection: 'column', alignItems: 'flex-start', gap: 4 }}>
                      <div style={{ display: 'flex', width: '100%', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span className={styles.subj_name}>{subj}</span>
                        <div className={styles.teacher_info}>
                          <span className={`${styles.t_name} ${teacherErr ? styles.t_error : ''}`}>
                            {m.teacher || 'Unassigned'}
                          </span>
                          <span className={styles.t_lec}>{m.lecturesPerWeek} lec/wk</span>
                        </div>
                      </div>
                      <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', fontSize: '0.72rem', color: 'var(--muted)' }}>
                        <span style={{ background: 'var(--bg2)', padding: '1px 6px', borderRadius: 4 }}>
                          Type: {m.lectureType || 'THEORY'}
                        </span>
                        <span style={{ background: 'var(--bg2)', padding: '1px 6px', borderRadius: 4 }}>
                          Proj: {m.projectorRequired ? 'Yes' : 'No'}
                        </span>
                        <span style={{ background: 'var(--bg2)', padding: '1px 6px', borderRadius: 4 }}>
                          Pref: {m.preferredRoomType || 'ANY'}
                        </span>
                        <span style={{ background: 'var(--bg2)', padding: '1px 6px', borderRadius: 4 }}>
                          Movable: {m.movable !== false ? 'Yes' : 'No'}
                        </span>
                      </div>
                    </div>
                  );
                })}
              </div>

              {errs.totalExceeds && (
                <div className={styles.err_banner}>
                  ⛔ Error: Budget exceeded ({errs.totalUsed}/{maxPerSection})
                </div>
              )}
            </Card>
          );
        })}
      </div>

      <StepFooter
        onPrev={onPrev}
        onNext={onNext}
        prevLabel="← Back to Setup"
        nextLabel="Next: Generate →"
        nextDisabled={hasErrors}
        nextWarning={hasErrors ? "Fix violations before generating" : null}
      />
    </div>
  );
}
