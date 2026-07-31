// src/App.jsx
import React, { useState, useEffect, useCallback, useMemo } from 'react';
import LandingPage   from './pages/public/LandingPage.jsx';
import LoginPage     from './pages/public/LoginPage.jsx';
import DashboardHome from './pages/public/DashboardHome.jsx';
import Navbar        from './components/Navbar.jsx';
import Sidebar       from './components/Sidebar.jsx';
import SetupPage     from './pages/SetupPage.jsx';
import ConstraintsPage from './pages/ConstraintsPage.jsx';
import ReviewPage    from './pages/ReviewPage.jsx';
import ResultPage    from './pages/ResultPage.jsx';
import EventsPage    from './pages/EventsPage.jsx';
import MyTimetablesPage from './pages/MyTimetablesPage.jsx';
import { useSections }    from './hooks/useSections.js';
import { useConstraints } from './hooks/useConstraints.js';
import { generateTimetable } from './api/timetableApi.js';
import { fetchRooms, fetchRoomSummary, createRoom, updateRoom, deleteRoom } from './api/roomApi.js';
import styles from './App.module.css';

// Step order inside the timetable builder — constraints first
const STEP_ORDER = ['constraints', 'setup', 'review', 'result'];

export default function App() {
  // ── Top-level routing ───────────────────────────────────────────────────
  // 'landing' | 'login' | 'dashboard' | 'builder' | 'events' | 'my-timetables'
  const [appView, setAppView]   = useState(() => localStorage.getItem('token') ? 'dashboard' : 'landing');
  const [userName, setUserName] = useState(() => localStorage.getItem('userName') || '');
  const [token, setToken]       = useState(() => localStorage.getItem('token') || '');

  // ── Room Management State ────────────────────────────────────────────────
  const [manageRooms, setManageRooms] = useState(false);
  const [rooms, setRooms]             = useState([]);
  const [roomSummary, setRoomSummary] = useState(null);

  // ── Builder step ────────────────────────────────────────────────────────
  const [activePage, setActivePage] = useState('constraints');
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError]   = useState(null);

  const {
    sections, addSection, duplicateSection, removeSection, updateSection,
    addSubject, removeSubject, addTeacher, removeTeacher,
    updateMapping, toApiPayload,
  } = useSections();

  const {
    daysPerWeek, setDaysPerWeek,
    periodsPerDay, setPeriodsPerDay,
    teacherMaxLectures, setTeacherMax,
    roomAllocationStrategy, setRoomAllocationStrategy,
    syncTeachers, maxPerSection, toConstraints,
  } = useConstraints();

  // ── Load rooms from API ──────────────────────────────────────────────────
  const loadRoomsData = useCallback(async () => {
    try {
      const [roomsList, summary] = await Promise.all([
        fetchRooms(token),
        fetchRoomSummary(token),
      ]);
      setRooms(roomsList);
      setRoomSummary(summary);
    } catch (err) {
      console.warn('Backend rooms API unavailable, operating with local state:', err);
    }
  }, [token]);

  useEffect(() => {
    loadRoomsData();
  }, [loadRoomsData]);

  const handleAddRoom = async (roomData) => {
    try {
      const created = await createRoom(roomData, token);
      setRooms(prev => [...prev, created]);
      loadRoomsData();
    } catch (err) {
      console.error('Failed to create room in backend:', err);
      // Fallback local addition with warning log
      const fallbackRoom = { ...roomData, id: Date.now() };
      setRooms(prev => [...prev, fallbackRoom]);
    }
  };

  const handleUpdateRoom = async (id, roomData) => {
    try {
      const updated = await updateRoom(id, roomData, token);
      setRooms(prev => prev.map(r => r.id === id ? updated : r));
      loadRoomsData();
    } catch (err) {
      console.error('Failed to update room in backend:', err);
      setRooms(prev => prev.map(r => r.id === id ? { ...roomData, id } : r));
    }
  };

  const handleDeleteRoom = async (id) => {
    try {
      await deleteRoom(id, token);
      setRooms(prev => prev.filter(r => r.id !== id));
      loadRoomsData();
    } catch (err) {
      console.error('Failed to delete room in backend:', err);
      setRooms(prev => prev.filter(r => r.id !== id));
    }
  };

  useEffect(() => {
    const all = [...new Set(sections.flatMap(s => s.teachers))];
    syncTeachers(all, daysPerWeek, periodsPerDay);
  }, [sections]); // eslint-disable-line

  const allTeachers = useMemo(
    () => [...new Set(sections.flatMap(s => s.teachers))],
    [sections]
  );

  // ── Live validation ─────────────────────────────────────────────────────
  const validation = useMemo(() => {
    const sectionErrors = {};
    sections.forEach(sec => {
      const errs = { totalExceeds: false, totalUsed: 0, teacherUsed: {}, teacherExceeds: {} };
      sec.subjects.forEach(subj => {
        const m = sec.mapping[subj] || {};
        const lec = Number(m.lecturesPerWeek) || 0;
        const teacher = m.teacher || '';
        errs.totalUsed += lec;
        if (teacher) errs.teacherUsed[teacher] = (errs.teacherUsed[teacher] || 0) + lec;
      });
      if (errs.totalUsed > maxPerSection) errs.totalExceeds = true;
      Object.entries(errs.teacherUsed).forEach(([t, used]) => {
        const cap = teacherMaxLectures[t] ?? maxPerSection;
        if (used > cap) errs.teacherExceeds[t] = { used, cap };
      });
      sectionErrors[sec.id] = errs;
    });

    const globalTeacherUsed = {};
    sections.forEach(sec => sec.subjects.forEach(subj => {
      const m = sec.mapping[subj] || {};
      const lec = Number(m.lecturesPerWeek) || 0;
      const teacher = m.teacher || '';
      if (teacher) globalTeacherUsed[teacher] = (globalTeacherUsed[teacher] || 0) + lec;
    }));

    const globalTeacherExceeds = {};
    Object.entries(globalTeacherUsed).forEach(([t, used]) => {
      const cap = teacherMaxLectures[t] ?? maxPerSection;
      if (used > cap) globalTeacherExceeds[t] = { used, cap };
    });

    const hasErrors = Object.values(sectionErrors).some(
      e => e.totalExceeds || Object.keys(e.teacherExceeds).length > 0
    );
    return { sectionErrors, globalTeacherUsed, globalTeacherExceeds, hasErrors };
  }, [sections, maxPerSection, teacherMaxLectures]);

  // ── Builder step navigation ─────────────────────────────────────────────
  function goNext() {
    const idx = STEP_ORDER.indexOf(activePage);
    if (idx < STEP_ORDER.length - 1) {
      const next = STEP_ORDER[idx + 1];
      setActivePage(next);
      if (next === 'result') handleGenerate();
    }
  }
  function goPrev() {
    const idx = STEP_ORDER.indexOf(activePage);
    if (idx > 0) setActivePage(STEP_ORDER[idx - 1]);
  }

  // ── Generate ────────────────────────────────────────────────────────────
  const handleGenerate = useCallback(async () => {
    if (sections.length === 0) return;
    setLoading(true); setError(null); setResult(null);
    try {
      const payload = toApiPayload({ ...toConstraints(), rooms });
      const data = await generateTimetable(payload, token);
      setResult(data);
    } catch (err) {
      setError(err.message || 'Failed to connect to the backend.');
    } finally {
      setLoading(false);
    }
  }, [sections, rooms, toApiPayload, toConstraints, token]);

  // ── App-level navigation ────────────────────────────────────────────────
  function handleLogin(name) {
    const savedToken = localStorage.getItem('token');
    const savedName = localStorage.getItem('userName');
    setToken(savedToken);
    setUserName(savedName || name);
    setAppView('dashboard');
  }
  function handleLogout() {
    setUserName('');
    setToken('');
    localStorage.removeItem('token');
    localStorage.removeItem('userName');
    setAppView('landing');
  }
  function openBuilder() {
    setActivePage('constraints');
    setAppView('builder');
  }

  // ── Render ──────────────────────────────────────────────────────────────
  if (appView === 'landing') {
    return <LandingPage onGetStarted={() => setAppView('login')} />;
  }

  if (appView === 'login') {
    return <LoginPage onLogin={handleLogin} onBack={() => setAppView('landing')} />;
  }

  if (appView === 'dashboard') {
    return (
      <DashboardHome
        userName={userName}
        onNewTimetable={openBuilder}
        onManageEvents={() => setAppView('events')}
        onMyTimetables={() => setAppView('my-timetables')}
        onLogout={handleLogout}
      />
    );
  }

  if (appView === 'events') {
    return (
      <EventsPage
        token={token}
        onBack={() => setAppView('dashboard')}
        onTimetableRefreshed={() => {}}
      />
    );
  }

  if (appView === 'my-timetables') {
    return (
      <MyTimetablesPage
        token={token}
        onBack={() => setAppView('dashboard')}
        onNewTimetable={openBuilder}
      />
    );
  }

  // ── Timetable builder ────────────────────────────────────────────────────
  return (
    <div className={styles.app_root}>
      <Navbar
        userName={userName}
        onDashboard={() => setAppView('dashboard')}
        onLogout={handleLogout}
      />
      <div className={styles.layout}>
        <Sidebar
          activePage={activePage}
          onNavigate={setActivePage}
          sections={sections}
          onAddSection={() => { addSection(); setActivePage('setup'); }}
          hasValidationErrors={validation.hasErrors}
          onDashboard={() => setAppView('dashboard')}
        />

        <main className={styles.main}>
          {activePage === 'constraints' && (
            <ConstraintsPage
              daysPerWeek={daysPerWeek} setDaysPerWeek={setDaysPerWeek}
              periodsPerDay={periodsPerDay} setPeriodsPerDay={setPeriodsPerDay}
              maxPerSection={maxPerSection}
              teacherMaxLectures={teacherMaxLectures} setTeacherMax={setTeacherMax}
              allTeachers={allTeachers}
              roomAllocationStrategy={roomAllocationStrategy} setRoomAllocationStrategy={setRoomAllocationStrategy}
              manageRooms={manageRooms} setManageRooms={setManageRooms}
              rooms={rooms}
              roomSummary={roomSummary}
              onAddRoom={handleAddRoom}
              onUpdateRoom={handleUpdateRoom}
              onDeleteRoom={handleDeleteRoom}
              onNext={goNext}
            />
          )}
          {activePage === 'setup' && (
            <SetupPage
              sections={sections}
              rooms={rooms}
              onAddSection={addSection}
              onCopySection={duplicateSection}
              onRemoveSection={removeSection}
              onUpdateSection={updateSection}
              onAddSubject={addSubject}
              onRemoveSubject={removeSubject}
              onAddTeacher={addTeacher}
              onRemoveTeacher={removeTeacher}
              onUpdateMapping={updateMapping}
              maxPerSection={maxPerSection}
              teacherMaxLectures={teacherMaxLectures}
              validation={validation}
              globalTeacherUsed={validation.globalTeacherUsed}
              globalTeacherExceeds={validation.globalTeacherExceeds}
              onNext={goNext}
              onPrev={goPrev}
            />
          )}
          {activePage === 'review' && (
            <ReviewPage
              sections={sections}
              rooms={rooms}
              roomAllocationStrategy={roomAllocationStrategy}
              validation={validation}
              onNext={goNext} onPrev={goPrev}
              maxPerSection={maxPerSection}
            />
          )}
          {activePage === 'result' && (
            <ResultPage
              sections={sections}
              rooms={rooms}
              daysPerWeek={daysPerWeek} periodsPerDay={periodsPerDay}
              onGenerate={handleGenerate}
              loading={loading} result={result} error={error}
              onPrev={goPrev}
            />
          )}
        </main>
      </div>
    </div>
  );
}
