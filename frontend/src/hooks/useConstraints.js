// src/hooks/useConstraints.js
import { useState, useCallback } from 'react';

export function useConstraints() {
  const [daysPerWeek, setDaysPerWeekRaw] = useState(5);
  const [periodsPerDay, setPeriodsPerDayRaw] = useState(6);
  // teacher -> max lectures/week (defaults to maxPerSection when teacher first added)
  const [teacherMaxLectures, setTeacherMaxLectures] = useState({});

  const maxPerSection = daysPerWeek * periodsPerDay;

  // When days/periods change, update ALL existing teacher maxes to new max
  const setDaysPerWeek = useCallback((val) => {
    const v = Number(val);
    setDaysPerWeekRaw(v);
    setTeacherMaxLectures(prev => {
      const next = {};
      Object.keys(prev).forEach(t => { next[t] = v * periodsPerDay; });
      return next;
    });
  }, [periodsPerDay]);

  const setPeriodsPerDay = useCallback((val) => {
    const v = Number(val);
    setPeriodsPerDayRaw(v);
    setTeacherMaxLectures(prev => {
      const next = {};
      Object.keys(prev).forEach(t => { next[t] = daysPerWeek * v; });
      return next;
    });
  }, [daysPerWeek]);

  // Allow manual override of a single teacher's max
  const setTeacherMax = useCallback((teacher, value) => {
    setTeacherMaxLectures(prev => ({ ...prev, [teacher]: Number(value) }));
  }, []);

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
