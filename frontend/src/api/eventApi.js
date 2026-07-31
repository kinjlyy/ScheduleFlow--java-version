// src/api/eventApi.js
// Complete API client for ScheduleFlow Event Service & Timetable Service

import { BASE_URL } from './timetableApi.js';

export const EVENT_BASE = `${BASE_URL}/events`;
export const TIMETABLE_BASE = `${BASE_URL}/timetables`;

function getHeaders(token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  return headers;
}

// ── Phase 7A: General Event Operations ──────────────────────────────────────

export async function createEvent(payload, token) {
  const res = await fetch(EVENT_BASE, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Failed to create event (${res.status})`);
  }
  return res.json();
}

export async function getAllEvents(token) {
  const res = await fetch(EVENT_BASE, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch events (${res.status})`);
  return res.json();
}

export async function getEventById(id, token) {
  const res = await fetch(`${EVENT_BASE}/${id}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch event #${id} (${res.status})`);
  return res.json();
}

export async function updateEvent(id, payload, token) {
  const res = await fetch(`${EVENT_BASE}/${id}`, {
    method: 'PUT',
    headers: getHeaders(token),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Failed to update event (${res.status})`);
  }
  return res.json();
}

export async function deleteEvent(id, token) {
  const res = await fetch(`${EVENT_BASE}/${id}`, {
    method: 'DELETE',
    headers: getHeaders(token),
  });
  if (!res.ok) throw new Error(`Failed to delete event (${res.status})`);
  return true;
}

export async function getEventsByDate(date, token) {
  const res = await fetch(`${EVENT_BASE}/by-date?date=${date}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch events for date ${date}`);
  return res.json();
}

export async function getEventsByStatus(status, token) {
  const res = await fetch(`${EVENT_BASE}/by-status?status=${status}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch events for status ${status}`);
  return res.json();
}

export async function getEventsByCategory(category, token) {
  const res = await fetch(`${EVENT_BASE}/by-category?category=${category}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch events for category ${category}`);
  return res.json();
}

// ── Phase 7B: Room Reservation & Availability ──────────────────────────────

export async function reserveRoom(payload, token) {
  const res = await fetch(`${EVENT_BASE}/reservations`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Room reservation failed (${res.status})`);
  }
  return res.json();
}

export async function cancelReservation(id, token) {
  const res = await fetch(`${EVENT_BASE}/reservations/${id}`, {
    method: 'DELETE',
    headers: getHeaders(token),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Failed to cancel reservation (${res.status})`);
  }
  return true;
}

export async function checkAvailability(date, startPeriod, endPeriod, token) {
  const query = `date=${date}&startPeriod=${startPeriod}&endPeriod=${endPeriod}`;
  const res = await fetch(`${EVENT_BASE}/availability?${query}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Availability check failed (${res.status})`);
  return res.json();
}

export async function getReservations({ date, locationId, status } = {}, token) {
  const params = new URLSearchParams();
  if (date) params.append('date', date);
  if (locationId) params.append('locationId', locationId);
  if (status) params.append('status', status);

  const query = params.toString() ? `?${params.toString()}` : '';
  const res = await fetch(`${EVENT_BASE}/reservations${query}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch reservations (${res.status})`);
  return res.json();
}

// ── Phase 7C: Academic Event Impact & Execution ────────────────────────────

export async function generateImpactAnalysis(payload, token) {
  const res = await fetch(`${EVENT_BASE}/impact-analysis`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Impact analysis failed (${res.status})`);
  }
  return res.json();
}

export async function generateExecutionPlan(payload, token) {
  const res = await fetch(`${EVENT_BASE}/execution-plan`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Execution plan failed (${res.status})`);
  }
  return res.json();
}

export async function executeStrategy(id, payload, token) {
  const res = await fetch(`${EVENT_BASE}/${id}/execute`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(payload),
  });
  if (!res.ok) {
    const err = await res.text();
    throw new Error(err || `Strategy execution failed (${res.status})`);
  }
  return res.json();
}

export async function getExecutionHistory(id, token) {
  const res = await fetch(`${EVENT_BASE}/${id}/execution`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch execution history for event #${id}`);
  return res.json();
}

// ── Timetable Service Operations ────────────────────────────────────────────

export async function getAllTimetables(token) {
  const res = await fetch(TIMETABLE_BASE, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch timetables (${res.status})`);
  return res.json();
}

export async function getActiveTimetable(token) {
  const res = await fetch(`${TIMETABLE_BASE}/active`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch active timetable (${res.status})`);
  return res.json();
}

export async function getTimetableById(id, token) {
  const res = await fetch(`${TIMETABLE_BASE}/${id}`, { headers: getHeaders(token) });
  if (!res.ok) throw new Error(`Failed to fetch timetable #${id} (${res.status})`);
  return res.json();
}
