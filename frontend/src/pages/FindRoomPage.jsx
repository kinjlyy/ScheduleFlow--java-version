// src/pages/FindRoomPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { fetchRooms } from '../api/roomApi.js';
import { getActiveLectures, getActiveTimetable } from '../api/eventApi.js';
import styles from './FindRoomPage.module.css';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];
const DEFAULT_FALLBACK_ROOMS = [
  { id: 101, roomNumber: '101', roomType: 'CLASSROOM', maximumCapacity: 60 },
  { id: 102, roomNumber: '102', roomType: 'CLASSROOM', maximumCapacity: 60 },
  { id: 103, roomNumber: '103', roomType: 'CLASSROOM', maximumCapacity: 60 },
  { id: 104, roomNumber: '104', roomType: 'CLASSROOM', maximumCapacity: 60 },
  { id: 105, roomNumber: '105', roomType: 'CLASSROOM', maximumCapacity: 60 },
  { id: 201, roomNumber: 'Lab 1', roomType: 'LAB', maximumCapacity: 30 },
  { id: 202, roomNumber: 'Lab 2', roomType: 'LAB', maximumCapacity: 30 },
  { id: 301, roomNumber: 'Seminar Hall 1', roomType: 'SEMINAR_HALL', maximumCapacity: 120 },
];

export default function FindRoomPage({ token, onBack }) {
  const [selectedDay, setSelectedDay] = useState('Monday');
  const [selectedPeriod, setSelectedPeriod] = useState(1);
  
  const [rooms, setRooms] = useState([]);
  const [activeLectures, setActiveLectures] = useState([]);
  const [activeTt, setActiveTt] = useState(null);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadInitialData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [fetchedRoomList, lectures, tt] = await Promise.all([
        fetchRooms(token).catch(() => []),
        getActiveLectures(token).catch(() => []),
        getActiveTimetable(token).catch(() => null)
      ]);

      // Combine rooms from resource service + active timetable lectures + defaults
      const roomMap = new Map();

      // 1. Add rooms from Resource Service if available
      if (Array.isArray(fetchedRoomList)) {
        fetchedRoomList.forEach(r => {
          const key = String(r.roomNumber || r.id);
          roomMap.set(key, {
            id: r.id,
            roomNumber: String(r.roomNumber || r.id),
            roomType: r.roomType || 'CLASSROOM',
            maximumCapacity: r.maximumCapacity || r.capacity || 60
          });
        });
      }

      // 2. Add rooms found in active lectures
      if (Array.isArray(lectures)) {
        lectures.forEach(l => {
          if (l.roomNumber || l.roomId) {
            const num = String(l.roomNumber || l.roomId);
            if (!roomMap.has(num)) {
              roomMap.set(num, {
                id: l.roomId || num,
                roomNumber: num,
                roomType: l.lectureType === 'LAB' ? 'LAB' : 'CLASSROOM',
                maximumCapacity: 60
              });
            }
          }
        });
      }

      // 3. If still empty, add default institutional rooms
      if (roomMap.size === 0) {
        DEFAULT_FALLBACK_ROOMS.forEach(r => {
          roomMap.set(r.roomNumber, r);
        });
      }

      setRooms(Array.from(roomMap.values()));
      setActiveLectures(Array.isArray(lectures) ? lectures : []);
      setActiveTt(tt);
    } catch (err) {
      setError('Error fetching data: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

  // Determine occupied room identifiers for selected day and period
  const occupiedRoomIdentifiers = new Set();
  const occupiedLectureMap = new Map();

  activeLectures.forEach(lec => {
    const rawDay = lec.day ? String(lec.day).toUpperCase() : '';
    const targetDay = selectedDay.toUpperCase();

    // Check if day matches (e.g. MONDAY vs MONDAY or MON)
    const dayMatches = rawDay === targetDay || rawDay.startsWith(targetDay.slice(0, 3));

    // Lecture slot can be 1-indexed (1-6) or 0-indexed (0-5)
    const slot = lec.lectureSlot != null ? Number(lec.lectureSlot) : (lec.period != null ? Number(lec.period) : -1);
    const slotMatches = slot === Number(selectedPeriod) || (slot + 1) === Number(selectedPeriod);

    if (dayMatches && slotMatches) {
      if (lec.roomId) occupiedRoomIdentifiers.add(String(lec.roomId));
      if (lec.roomNumber) occupiedRoomIdentifiers.add(String(lec.roomNumber));
      
      const key = String(lec.roomNumber || lec.roomId);
      occupiedLectureMap.set(key, lec);
    }
  });

  // Filter free vs occupied rooms
  const freeRooms = rooms.filter(room => {
    const isByIdOccupied = occupiedRoomIdentifiers.has(String(room.id));
    const isByNumOccupied = room.roomNumber && occupiedRoomIdentifiers.has(String(room.roomNumber));
    return !isByIdOccupied && !isByNumOccupied;
  });

  const occupiedRooms = rooms.filter(room => {
    const isByIdOccupied = occupiedRoomIdentifiers.has(String(room.id));
    const isByNumOccupied = room.roomNumber && occupiedRoomIdentifiers.has(String(room.roomNumber));
    return isByIdOccupied || isByNumOccupied;
  });

  return (
    <div className={styles.root}>
      {/* Header */}
      <div className={styles.topbar}>
        <button className={styles.back_btn} onClick={onBack}>← Dashboard</button>
        <div>
          <h1 className={styles.title}>🔍 Find Free Room</h1>
          <p className={styles.subtitle}>Check real-time room availability based on active timetable schedule</p>
        </div>
      </div>

      {/* Selector Card */}
      <div className={styles.filter_card}>
        <h3 className={styles.filter_heading}>Select Time Slot</h3>
        <div className={styles.filter_grid}>
          <div className={styles.form_group}>
            <label>Day of Week</label>
            <select
              value={selectedDay}
              onChange={e => setSelectedDay(e.target.value)}
              className={styles.select_input}
            >
              {DAYS.map(day => (
                <option key={day} value={day}>{day}</option>
              ))}
            </select>
          </div>

          <div className={styles.form_group}>
            <label>Period Slot</label>
            <select
              value={selectedPeriod}
              onChange={e => setSelectedPeriod(Number(e.target.value))}
              className={styles.select_input}
            >
              {[1, 2, 3, 4, 5, 6].map(p => (
                <option key={p} value={p}>Period {p}</option>
              ))}
            </select>
          </div>
        </div>

        {activeTt ? (
          <div className={styles.active_tt_badge}>
            🟢 Checked against <strong>Active Timetable #{activeTt.id}</strong> ({activeTt.name || 'Current Version'})
          </div>
        ) : (
          <div className={styles.warn_tt_badge}>
            ℹ️ Checking room availability across all registered institution rooms.
          </div>
        )}
      </div>

      {loading && <div className={styles.loading_box}>Loading room & timetable schedules…</div>}
      {error && <div className={styles.error_box}>⚠️ {error}</div>}

      {/* Room Grid Section */}
      {!loading && (
        <div className={styles.results_container}>
          {/* Available Free Rooms */}
          <div className={styles.section_card}>
            <div className={styles.section_header}>
              <h2 className={styles.text_success}>
                ✅ Available Free Rooms ({freeRooms.length})
              </h2>
              <span className={styles.subtext}>Free on {selectedDay}, Period {selectedPeriod}</span>
            </div>

            {freeRooms.length === 0 ? (
              <div className={styles.empty_box}>
                No rooms available for {selectedDay}, Period {selectedPeriod}.
              </div>
            ) : (
              <div className={styles.room_grid}>
                {freeRooms.map(r => (
                  <div key={r.id || r.roomNumber} className={styles.room_card_free}>
                    <div className={styles.room_card_header}>
                      <span className={styles.room_num}>Room {r.roomNumber}</span>
                      <span className={styles.badge_free}>FREE</span>
                    </div>
                    <div className={styles.room_details}>
                      <div>🏛️ Type: <strong>{r.roomType || 'CLASSROOM'}</strong></div>
                      <div>👥 Capacity: <strong>{r.maximumCapacity || 60} seats</strong></div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Occupied Rooms */}
          {occupiedRooms.length > 0 && (
            <div className={styles.section_card}>
              <div className={styles.section_header}>
                <h2 className={styles.text_danger}>
                  ❌ Occupied Rooms ({occupiedRooms.length})
                </h2>
                <span className={styles.subtext}>Booked by Lectures on {selectedDay}, Period {selectedPeriod}</span>
              </div>

              <div className={styles.room_grid}>
                {occupiedRooms.map(r => {
                  const key = String(r.roomNumber || r.id);
                  const lec = occupiedLectureMap.get(key);
                  return (
                    <div key={r.id || r.roomNumber} className={styles.room_card_busy}>
                      <div className={styles.room_card_header}>
                        <span className={styles.room_num}>Room {r.roomNumber}</span>
                        <span className={styles.badge_busy}>OCCUPIED</span>
                      </div>
                      <div className={styles.room_details}>
                        <div>🏛️ Type: <strong>{r.roomType || 'CLASSROOM'}</strong></div>
                        {lec && (
                          <div className={styles.occ_info}>
                            📖 Subject: <strong>{lec.subjectId || lec.subject}</strong><br/>
                            👤 Teacher: <strong>{lec.teacherId || lec.teacher}</strong><br/>
                            👥 Section: <strong>{lec.sectionId || lec.section}</strong>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
