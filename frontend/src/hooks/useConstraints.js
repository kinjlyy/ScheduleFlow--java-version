// src/hooks/useConstraints.js
import { useState, useCallback } from 'react';

export function useConstraints() {
  const [daysPerWeek, setDaysPerWeekRaw] = useState(5);
  const [periodsPerDay, setPeriodsPerDayRaw] = useState(6);
  // teacher -> max lectures/week (defaults to maxPerSection when teacher first added)
  const [teacherMaxLectures, setTeacherMaxLectures] = useState({});

  const maxPerSection = daysPerWeek * periodsPerDay;

  // When days/periods change, update teacher maxes but PRESERVE custom overrides
  const setDaysPerWeek = useCallback((val) => {
    const v = Number(val);
    const oldMax = daysPerWeek * periodsPerDay;
    const newMax = v * periodsPerDay;
    setDaysPerWeekRaw(v);
    setTeacherMaxLectures(prev => {
      const next = { ...prev };
      Object.keys(next).forEach(t => {
        // If teacher was at old max, update to new max
        if (next[t] === oldMax) {
          next[t] = newMax;
        } else {
          // If teacher had custom limit, cap it at the new max
          next[t] = Math.min(next[t], newMax);
        }
      });
      return next;
    });
  }, [daysPerWeek, periodsPerDay]);

  const setPeriodsPerDay = useCallback((val) => {
    const v = Number(val);
    const oldMax = daysPerWeek * periodsPerDay;
    const newMax = daysPerWeek * v;
    setPeriodsPerDayRaw(v);
    setTeacherMaxLectures(prev => {
      const next = { ...prev };
      Object.keys(next).forEach(t => {
        if (next[t] === oldMax) {
          next[t] = newMax;
        } else {
          next[t] = Math.min(next[t], newMax);
        }
      });
      return next;
    });
  }, [daysPerWeek, periodsPerDay]);

  // Allow manual override of a single teacher's max (must be <= maxPerSection)
  const setTeacherMax = useCallback((teacher, value) => {
    const v = Math.min(Number(value), daysPerWeek * periodsPerDay);
    setTeacherMaxLectures(prev => ({ ...prev, [teacher]: v }));
  }, [daysPerWeek, periodsPerDay]);

  // Called whenever section teachers change — new teachers get maxPerSection as default
  const syncTeachers = useCallback((allTeachers, currentDays, currentPeriods) => {
    const cap = (currentDays ?? daysPerWeek) * (currentPeriods ?? periodsPerDay);
    setTeacherMaxLectures(prev => {
      const next = { ...prev };
      allTeachers.forEach(t => {
        // Only set default if teacher is new (not already customised)
        if (!(t in next)) next[t] = cap;
      });
      // Remove teachers that no longer exist
      Object.keys(next).forEach(t => {
        if (!allTeachers.includes(t)) delete next[t];
      });
      return next;
    });
  }, [daysPerWeek, periodsPerDay]);

  return {
    daysPerWeek, setDaysPerWeek,
    periodsPerDay, setPeriodsPerDay,
    teacherMaxLectures,
    setTeacherMax,
    syncTeachers,
    maxPerSection,
    toConstraints: () => ({ daysPerWeek, periodsPerDay, teacherMaxLectures }),
  };
}
