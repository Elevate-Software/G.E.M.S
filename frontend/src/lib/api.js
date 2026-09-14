import { getToken, clearAuth } from './auth.js';

const BASE = import.meta.env.VITE_API_BASE_URL || '/api';

function buildUrl(path, params) {
  const fullPath = `${BASE}${path}`;
  const url = fullPath.startsWith('http')
    ? new URL(fullPath)
    : new URL(fullPath, window.location.origin);

  if (params) {
    Object.entries(params).forEach(([k, v]) => {
      if (v !== null && v !== undefined && v !== '') url.searchParams.set(k, v);
    });
  }

  return url;
}

async function request(path, { method = 'GET', body = null, params = null } = {}) {
  const url = buildUrl(path, params);

  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;

  const res = await fetch(url.toString(), {
    method,
    headers,
    body: body ? JSON.stringify(body) : null,
  });

  if (res.status === 401) {
    clearAuth();
    // In Solid Router we'd ideally use navigate('/login') but if outside component scope:
    window.location.href = '/login'; 
    throw new Error('Session expired. Please log in again.');
  }

  if (res.status === 204) return null;

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const msg = data?.message || data?.error || `Request failed (${res.status})`;
    throw new Error(msg);
  }

  if (data === null) {
    throw new Error(`Invalid response from server (${res.status})`);
  }

  return data;
}

/* ── Auth endpoints ── */
export function login(email, password) {
  return request('/auth/login', { method: 'POST', body: { email, password } });
}
export function register(payload) {
  return request('/auth/register', { method: 'POST', body: payload });
}
export function logout() {
  return request('/auth/logout', { method: 'POST' });
}

/* ── User endpoints ── */
export function getMe() {
  return request('/users/me');
}
export function getUsers(page = 0, size = 15) {
  return request('/users', { params: { page, size, sort: 'createdAt,desc' } });
}
export function getUserById(id) {
  return request(`/users/${id}`);
}
export function updateUserStatus(id, status) {
  return request(`/users/${id}/status`, { method: 'PATCH', body: { status } });
}
export function createUser(payload) {
  return request('/users', { method: 'POST', body: payload });
}
export function changePassword(payload) {
  return request('/auth/change-password', { method: 'POST', body: payload });
}

/* ── Scan endpoints ── */
export function scanCredential(token, gateId, direction) {
  return request('/scan', { method: 'POST', body: { token, gateId, direction } });
}

/* ── Credential endpoints ── */
export function generateCredential() {
  return request('/credentials/generate', { method: 'POST' });
}
export function getMyCredential() {
  return request('/credentials/my-credential');
}

/* ── Gate endpoints ── */
export function getGates() {
  return request('/gates');
}
