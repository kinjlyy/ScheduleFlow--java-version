// src/hooks/useSections.js
import { useState, useCallback } from 'react';

let _idCounter = 0;
const nextId = () => `sec_${++_idCounter}`;

const DEFAULT_MAPPING = () => ({
  teacher: '',
  lecturesPerWeek: 4,
  lectureType: 'THEORY',
  projectorRequired: false,
  preferredRoomType: 'ANY',
  movable: true,
});

const EMPTY_SECTION = () => ({
  id: nextId(),
  name: '',
  capacity: '',
  fixedRoomId: null,
  subjects: [],
  teachers: [],
  // mapping: { [subject]: { teacher: '', lecturesPerWeek: 4, lectureType: 'THEORY', ... } }
  mapping: {},
});

export function useSections() {
  const [sections, setSections] = useState([EMPTY_SECTION()]);

  const addSection = useCallback(() => {
    setSections(prev => [...prev, EMPTY_SECTION()]);
  }, []);

  const duplicateSection = useCallback((sourceId) => {
    setSections(prev => {
      const source = prev.find(s => s.id === sourceId);
      if (!source) return prev;

      const copy = {
        ...source,
        id: nextId(),
        name: source.name ? `${source.name} (Copy)` : '',
        mapping: JSON.parse(JSON.stringify(source.mapping)),
      };

      return [...prev, copy];
    });
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
        mapping: { ...s.mapping, [name]: DEFAULT_MAPPING() },
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
      const current = s.mapping[subject] || DEFAULT_MAPPING();
      return {
        ...s,
        mapping: {
          ...s.mapping,
          [subject]: { ...current, [field]: value },
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
        fixedRoomId: s.fixedRoomId ? Number(s.fixedRoomId) : null,
        subjects: s.subjects,
        teachers: s.teachers,
        mappings: s.subjects.map(subj => {
          const m = s.mapping[subj] || DEFAULT_MAPPING();
          return {
            subject: subj,
            teacher: m.teacher || '',
            lecturesPerWeek: Number(m.lecturesPerWeek) || 4,
            lectureType: m.lectureType || 'THEORY',
            projectorRequired: Boolean(m.projectorRequired),
            preferredRoomType: m.preferredRoomType || 'ANY',
            movable: m.movable !== undefined ? Boolean(m.movable) : true,
          };
        }),
      })),
      daysPerWeek: constraints.daysPerWeek,
      periodsPerDay: constraints.periodsPerDay,
      teacherMaxLectures: constraints.teacherMaxLectures,
      roomAllocationStrategy: constraints.roomAllocationStrategy || 'DYNAMIC_ALLOCATION',
      rooms: (constraints.rooms || []).map(r => ({
        id: r.id,
        roomNumber: String(r.roomNumber || r.id),
        maximumCapacity: Number(r.maximumCapacity) || 60,
        roomType: r.roomType || 'CLASSROOM',
        hasProjector: Boolean(r.hasProjector),
        hasAc: Boolean(r.hasAc),
        hasComputers: Boolean(r.hasComputers),
        active: r.active !== false,
      })),
    };
  }, [sections]);

  return {
    sections,
    addSection,
    duplicateSection,
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
