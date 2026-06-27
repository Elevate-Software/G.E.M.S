import { createSignal } from 'solid-js';

const TOKEN_KEY = 'gems_token';
const USER_KEY  = 'gems_user';

// Create a reactive signal for the auth state
const [authState, setAuthState] = createSignal({
  token: localStorage.getItem(TOKEN_KEY),
  user: null
});

// Try parsing user from localStorage initially
try {
  const cached = localStorage.getItem(USER_KEY);
  if (cached) {
    setAuthState(prev => ({ ...prev, user: JSON.parse(cached) }));
  }
} catch (e) {
  // ignore
}

export function getToken() {
  return authState().token;
}

export function getUser() {
  return authState().user;
}

export function isAuthenticated() {
  return !!authState().token;
}

export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token);
  setAuthState(prev => ({ ...prev, token }));
}

export function setUser(user) {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
  setAuthState(prev => ({ ...prev, user }));
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  setAuthState({ token: null, user: null });
}

export function hasRole(...roles) {
  const user = getUser();
  return user?.role ? roles.includes(user.role) : false;
}

export function mustChangePassword() {
  const user = getUser();
  return user?.passwordChangeRequired || false;
}
