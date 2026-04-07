// src/components/StepFooter.jsx
// Sticky bottom navigation bar shown on every step page
import React from 'react';
import styles from './StepFooter.module.css';

export default function StepFooter({
  onNext,
  onPrev,
  nextLabel = 'Next →',
  prevLabel = '← Back',
  showPrev = true,
  showNext = true,
  nextDisabled = false,
  nextWarning = null,   // optional warning string shown near the Next button
  children,             // optional extra content in the middle
}) {
  return (
    <div className={styles.footer}>
      <div className={styles.left}>
        {showPrev && (
          <button className={styles.prev_btn} onClick={onPrev} type="button">
            {prevLabel}
          </button>
        )}
      </div>

      <div className={styles.center}>{children}</div>

      <div className={styles.right}>
        {nextWarning && (
          <span className={styles.next_warning}>⚠️ {nextWarning}</span>
        )}
        {showNext && (
          <button
            className={`${styles.next_btn} ${nextDisabled ? styles.next_disabled : ''}`}
            onClick={nextDisabled ? undefined : onNext}
            type="button"
            title={nextDisabled ? nextWarning || 'Fix errors before continuing' : ''}
          >
            {nextLabel}
          </button>
        )}
      </div>
    </div>
  );
}
