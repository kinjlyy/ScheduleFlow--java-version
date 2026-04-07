// src/components/TimetableGrid.jsx
import React from 'react';
import styles from './TimetableGrid.module.css';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'];

export default function TimetableGrid({ timetable, sectionId, periodsPerDay, daysPerWeek }) {
  const days = DAYS.slice(0, daysPerWeek);
  const periods = periodsPerDay;

  const secGrid = timetable?.[sectionId];
  if (!secGrid) return null;

  return (
    <div className={styles.wrap}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th className={styles.th}>DAY</th>
            {Array.from({ length: periods }, (_, i) => (
              <th key={i} className={styles.th}>PERIOD {i + 1}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {days.map((day, dayIdx) => {
            const row = secGrid[day] || [];
            return (
              <tr key={day} className={dayIdx % 2 === 0 ? styles.row_even : ''}>
                <td className={styles.day_cell}>{day}</td>
                {Array.from({ length: periods }, (_, p) => {
                  const cell = row[p];
                  const isFree = !cell || cell.free || cell.subject === 'FREE';
                  return (
                    <td key={p} className={isFree ? styles.free_cell : styles.period_cell}>
                      {isFree ? (
                        <span className={styles.free_label}>FREE</span>
                      ) : (
                        <>
                          <div className={styles.cell_subj}>{cell.subject}</div>
                          <div className={styles.cell_teacher}>{cell.teacher}</div>
                        </>
                      )}
                    </td>
                  );
                })}
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
