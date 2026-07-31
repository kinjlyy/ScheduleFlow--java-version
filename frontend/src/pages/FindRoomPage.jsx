// src/pages/FindRoomPage.jsx
import React, { useState, useEffect, useCallback } from 'react';
import { fetchRooms } from '../api/roomApi.js';
import { getActiveLectures, getActiveTimetable } from '../api/eventApi.js';
import styles from './FindRoomPage.module.css';

const DAYS = ['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'];

export default function FindRoomPage({ token, onBack }) {
  const [selectedDay, setSelectedDay] = useState('Monday');
  const [selectedPeriod, setSelectedPeriod] = useState(1);
  
  const [rooms, setRooms] = useState([]);
  const [activeLectures, setActiveLectures] = useState([]);
  const [activeTt, setActiveTt] = useState(null);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [reserveSuccess, setReserveSuccess] = useState(null);

  const loadInitialData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [roomList, lectures, tt] = await Promise.all([
        fetchRooms(token).catch(() => []),
        getActiveLectures(token).catch(() => []),
        getActiveTimetable(token).catch(() => null)
      ]);
      setRooms(roomList);
      setActiveLectures(lectures);
      setActiveTt(tt);
    } catch (err) {
      setError('Failed to fetch rooms or active timetable data: ' + err.message);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

  // Determine occupied room IDs or numbers for the selected day & period
  const occupiedRoomIdentifiers = new Set();
  activeLectures.forEach(lec => {
    // Check day match (case insensitive) and period match
    const lecDay = lec.day ? lec.day.charAt(0).toUpperCase() + lec.day.slice(1).toLowerCase() : '';
    const lecPeriod = lec.lectureSlot != null ? lec.lectureSlot : (lec.period != null ? lec.period : 0);
    
    if (lecDay === selectedDay && Number(lecPeriod) === Number(selectedPeriod)) {
      if (lec.roomId) occupiedRoomIdentifiers.add(String(lec.roomId));
      if (lec.roomNumber) occupiedRoomIdentifiers.add(String(lec.roomNumber));
    }
  });

  // Filter available rooms
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
      {/* Top Navigation */}
      <div className={styles.topbar}>
        <button className={styles.back_btn} onClick={onBack}>← Dashboard</button>
        <div>
          <h1 className={styles.title}>🔍 Find Free Room</h1>
          <p className={styles.subtitle}>Check real-time room availability based on active timetable schedule</p>
        </div>
      </div>

      {/* Filter Box */}
      <div className={styles.filter_card}>
        <h3 className={styles.filter_heading}>Select Schedule Time Slot</h3>
        <div className={styles.filter_grid}>
          <div className={styles.form_group}>
            <label>Day of the Week</label>
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
            <label>Period Slot (1 to 6)</label>
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
            ⚠️ No active timetable set. Displaying all registered rooms as available.
          </div>
        )}
      </div>

      {loading && <div className={styles.loading_box}>Loading room & timetable data…</div>}
      {error && <div className={styles.error_box}>⚠️ {error}</div>}
      {reserveSuccess && <div className={styles.success_box}>✅ {reserveSuccess}</div>}

      {/* Results View */}
      {!loading && (
        <div className={styles.results_container}>
          {/* Free Rooms Section */}
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
                  <div key={r.id} className={styles.room_card_free}>
                    <div className={styles.room_card_header}>
                      <span className={styles.room_num}>Room {r.roomNumber || r.id}</span>
                      <span className={styles.badge_free}>FREE</span>
                    </div>
                    <div className={styles.room_details}>
                      <div>🏛️ Type: <strong>{r.roomType || 'CLASSROOM'}</strong></div>
                      <div>👥 Capacity: <strong>{r.maximumCapacity || r.capacity || 'N/A'} seats</strong></div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Occupied Rooms Section */}
          <div className={styles.section_card}>
            <div className={styles.section_header}>
              <h2 className={styles.text_danger}>
                ❌ Occupied Rooms ({occupiedRooms.length})
              </h2>
              <span className={styles.subtext}>Booked by Lectures on {selectedDay}, Period {selectedPeriod}</span>
            </div>

            {occupiedRooms.length === 0 ? (
              <div className={styles.empty_box}>
                All rooms are free during this period slot!
              </div>
            ) : (
              <div className={styles.room_grid}>
                {occupiedRooms.map(r => {
                  const occupiedLec = activeLectures.find(l => {
                    const lDay = l.day ? l.day.charAt(0).toUpperCase() + l.day.slice(1).toLowerCase() : '';
                    const lPeriod = l.lectureSlot != null ? l.lectureSlot : (l.period != null ? l.period : 0);
                    return lDay === selectedDay && Number(lPeriod) === Number(selectedPeriod) &&
                      (String(l.roomId) === String(r.id) || String(l.roomNumber) === String(r.roomNumber));
                  });

                  return (
                    <div key={r.id} className={styles.room_card_busy}>
                      <div className={styles.room_card_header}>
                        <span className={styles.room_num}>Room {r.roomNumber || r.id}</span>
                        <span className={styles.badge_busy}>OCCUPIED</span>
                      </div>
                      <div className={styles.room_details}>
                        <div>🏛️ Type: <strong>{r.roomType || 'CLASSROOM'}</strong></div>
                        {occupiedLec && (
                          <div className={styles.occ_info}>
                            📖 Lecture: <strong>{occupiedLec.subjectId || occupiedLec.subject}</strong><br/>
                            👤 Teacher: <strong>{occupiedLec.teacherId || occupiedLec.teacher}</strong><br/>
                            👥 Section: <strong>{occupiedLec.sectionId || occupiedLec.section}</strong>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
