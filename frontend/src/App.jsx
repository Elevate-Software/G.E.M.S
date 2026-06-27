import { createSignal, onMount, Show, createEffect } from 'solid-js';
import { useNavigate, useLocation, A } from '@solidjs/router';
import { getUser, hasRole, isAuthenticated, clearAuth, mustChangePassword } from './lib/auth';
import { logout } from './lib/api';
import { getInitials, roleLabel } from './lib/utils';
import { useToast } from './components/ToastProvider';

export default function App(props) {
  const navigate = useNavigate();
  const location = useLocation();
  const showToast = useToast();
  
  const [sidebarOpen, setSidebarOpen] = createSignal(false);

  // Auth Guard
  createEffect(() => {
    if (!isAuthenticated() && location.pathname !== '/login' && location.pathname !== '/register' && location.pathname !== '/change-password') {
      navigate('/login');
    } else if (isAuthenticated() && location.pathname === '/') {
      navigate('/dashboard');
    } else if (isAuthenticated() && mustChangePassword() && location.pathname !== '/change-password') {
      navigate('/change-password');
    }
  });

  const handleLogout = async () => {
    try {
      await logout();
    } catch(e) {
      // ignore
    }
    clearAuth();
    showToast('Logged out successfully', 'success');
    navigate('/login');
  };

  const closeSidebar = () => setSidebarOpen(false);

  // Derived state to avoid reactivity issues on initial load if user is null
  const user = () => getUser();

  return (
    <Show when={isAuthenticated()}>
      <div class="app-layout">
        <div class="mobile-header">
          <button class="mobile-menu-btn" onClick={() => setSidebarOpen(!sidebarOpen())}>☰</button>
          <span class="sidebar-brand-name">G.E.M.S</span>
          <div style={{ width: '40px' }}></div>
        </div>

        <Show when={sidebarOpen()}>
          <div class="sidebar-overlay show" onClick={closeSidebar}></div>
        </Show>

        <aside class={`sidebar ${sidebarOpen() ? 'open' : ''}`}>
          <div class="sidebar-brand">
            <div class="sidebar-brand-icon">💎</div>
            <div class="sidebar-brand-text">
              <span class="sidebar-brand-name">G.E.M.S</span>
              <span class="sidebar-brand-sub">Campus Gate Control</span>
            </div>
          </div>

          <nav class="sidebar-nav">
            <A href="/dashboard" class="nav-link" activeClass="active" onClick={closeSidebar}>
              <span class="nav-link-icon">📊</span>
              Dashboard
            </A>

            <Show when={hasRole('SECURITY', 'ADMIN')}>
              <div class="sidebar-section-label">Operations</div>
              <A href="/scan" class="nav-link" activeClass="active" onClick={closeSidebar}>
                <span class="nav-link-icon">📡</span>
                Gate Scanner
              </A>
            </Show>

            <Show when={hasRole('ADMIN')}>
              <div class="sidebar-section-label">Management</div>
              <A href="/users" class="nav-link" activeClass="active" onClick={closeSidebar}>
                <span class="nav-link-icon">👥</span>
                Users
              </A>
            </Show>

            <div class="sidebar-section-label">Account</div>
            <A href="/profile" class="nav-link" activeClass="active" onClick={closeSidebar}>
              <span class="nav-link-icon">👤</span>
              My Profile
            </A>
          </nav>

          <div class="sidebar-footer">
            <div class="sidebar-user">
              <div class="avatar" style={{ background: 'var(--gradient-primary)' }}>
                {getInitials(user()?.fullName)}
              </div>
              <div class="sidebar-user-info">
                <div class="sidebar-user-name">{user()?.fullName || 'User'}</div>
                <div class="sidebar-user-role">{roleLabel(user()?.role)}</div>
              </div>
              <button class="btn btn-ghost btn-icon" onClick={handleLogout} title="Log out" style={{ "margin-left": "auto" }}>
                🚪
              </button>
            </div>
          </div>
        </aside>

        <main class="main-content">
          {props.children}
        </main>
      </div>
    </Show>
  );
}
