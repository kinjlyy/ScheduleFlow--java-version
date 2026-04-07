// src/hooks/useSections.js
import { useState, useCallback } from 'react';

let _idCounter = 0;
const nextId = () => `sec_${++_idCounter}`;

const EMPTY_SECTION = () => ({
  id: nextId(),
  name: '',
  capacity: '',
  subjects: [],
  teachers: [],
  // mapping: { [subject]: { teacher: '', lecturesPerWeek: 4 } }
  mapping: {},
});

export function useSections() {
  const [sections, setSections] = useState([EMPTY_SECTION()]);

  const addSection = useCallback(() => {
    setSections(prev => [...prev, EMPTY_SECTION()]);
  }, []);

  const removeSection = useCallback((id) => {
    setSections(prev => prev.filter(s => s.id !== id));
  }, []);

  const updateSection = useCallback((id, field, value) => {
    setSections(prev => prev.map(s => s.id === id ? { ...s, [field]: value } : s));
  }, []);

  // ── Subjects ──────────────────────────────────────────────────────────────
  const addSubject = useCallback((id, subject) => {
    const name = subject.trim();
    if (!name) return;
    setSections(prev => prev.map(s => {
      if (s.id !== id) return s;
      if (s.subjects.includes(name)) return s;
      return {
        ...s,
        subjects: [...s.subjects, name],
        mapping: { ...s.mapping, [name]: { teacher: '', lecturesPerWeek: 4 } },
      };
    }));
  }, []);

  const removeSubject = useCallback((id, subject) => {
    setSections(prev => prev.map(s => {
      if (s.id !== id) return s;
      const { [subject]: _, ...rest } = s.mapping;
      return { ...s, subjects: s.subjects.filter(sub => sub !== subject), mapping: rest };
    }));
  }, []);

  // ── Teachers ──────────────────────────────────────────────────────────────
  const addTeacher = useCallback((id, teacher) => {
    const name = teacher.trim();
    if (!name) return;
    setSections(prev => prev.map(s => {
      if (s.id !== id) return s;
      if (s.teachers.includes(name)) return s;
      return { ...s, teachers: [...s.teachers, name] };
    }));
  }, []);

  const removeTeacher = useCallback((id, teacher) => {
    setSections(prev => prev.map(s => {
      if (s.id !== id) return s;
      // Clear this teacher from any mappings
      const mapping = { ...s.mapping };
      Object.keys(mapping).forEach(subj => {
        if (mapping[subj].teacher === teacher) mapping[subj] = { ...mapping[subj], teacher: '' };
      });
      return { ...s, teachers: s.teachers.filter(t => t !== teacher), mapping };
    }));
  }, []);

  // ── Mapping ───────────────────────────────────────────────────────────────
  const updateMapping = useCallback((sectionId, subject, field, value) => {
    setSections(prev => prev.map(s => {
      if (s.id !== sectionId) return s;
      return {
        ...s,
        mapping: {
          ...s.mapping,
          [subject]: { ...s.mapping[subject], [field]: value },
        },
      };
    }));
  }, []);

  // ── Serialise for API ─────────────────────────────────────────────────────
  const toApiPayload = useCallback((constraints) => {
    return {
      sections: sections.map(s => ({
        id: s.id,
        name: s.name || s.id,
        capacity: Number(s.capacity) || 0,
        subjects: s.subjects,
        teachers: s.teachers,
        mappings: s.subjects.map(subj => ({
          subject: subj,
          teacher: s.mapping[subj]?.teacher || '',
          lecturesPerWeek: Number(s.mapping[subj]?.lecturesPerWeek) || 4,
        })),
      })),
      daysPerWeek: constraints.daysPerWeek,
      periodsPerDay: constraints.periodsPerDay,
      teacherMaxLectures: constraints.teacherMaxLectures,
    };
  }, [sections]);

  return {
    sections,
    addSection,
    removeSection,
    updateSection,
    addSubject,
    removeSubject,
    addTeacher,
    removeTeacher,
    updateMapping,
    toApiPayload,
  };
}
