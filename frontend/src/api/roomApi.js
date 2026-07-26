// src/api/roomApi.js
// Communicates with the Java Spring Boot Room Controller

import { BASE_URL } from './timetableApi.js';

function getHeaders(token) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export async function fetchRooms(token) {
  const res = await fetch(`${BASE_URL}/rooms`, {
    headers: getHeaders(token),
  });
  if (!res.ok) throw new Error(`Failed to fetch rooms: ${res.statusText}`);
  return res.json();
}

export async function fetchRoomSummary(token) {
  const res = await fetch(`${BASE_URL}/rooms/summary`, {
    headers: getHeaders(token),
  });
  if (!res.ok) throw new Error(`Failed to fetch room summary: ${res.statusText}`);
  return res.json();
}

export async function fetchRoomsByCapacity(capacity, token) {
  const res = await fetch(`${BASE_URL}/rooms/capacity/${capacity}`, {
    headers: getHeaders(token),
  });
  if (!res.ok) throw new Error(`Failed to fetch rooms for capacity ${capacity}: ${res.statusText}`);
  return res.json();
}

export async function createRoom(roomData, token) {
  const res = await fetch(`${BASE_URL}/rooms`, {
    method: 'POST',
    headers: getHeaders(token),
    body: JSON.stringify(roomData),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || 'Failed to create room');
  }
  return res.json();
}

export async function updateRoom(id, roomData, token) {
  const res = await fetch(`${BASE_URL}/rooms/${id}`, {
    method: 'PUT',
    headers: getHeaders(token),
    body: JSON.stringify(roomData),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || 'Failed to update room');
  }
  return res.json();
}

export async function deleteRoom(id, token) {
  const res = await fetch(`${BASE_URL}/rooms/${id}`, {
    method: 'DELETE',
    headers: getHeaders(token),
  });
  if (!res.ok) {
    const errText = await res.text();
    throw new Error(errText || 'Failed to delete room');
  }
  return true;
}
