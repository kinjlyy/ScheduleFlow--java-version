// src/api/timetableApi.js
// Communicates with the Java Spring Boot backend

export const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

/**
 * POST /api/generate
 * @param {Object} payload  - { sections, daysPerWeek, periodsPerDay, teacherMaxLectures }
 * @returns {Promise<Object>} - TimetableResponseDTO
 */
export async function generateTimetable(payload, token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const res = await fetch(`${BASE_URL}/generate`, {
    method: 'POST',
    headers,
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(`Server error ${res.status}: ${errText}`);
  }
  return res.json();
}

/**
 * GET /api/health
 */
export async function checkHealth() {
  const res = await fetch(`${BASE_URL}/health`);
  return res.ok;
}
